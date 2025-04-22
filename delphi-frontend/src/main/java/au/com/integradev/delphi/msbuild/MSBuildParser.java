/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.msbuild;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.msbuild.condition.ConditionEvaluator;
import au.com.integradev.delphi.msbuild.expression.ExpressionEvaluator;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MSBuildParser {
  private static final Logger LOG = LoggerFactory.getLogger(MSBuildParser.class);

  private final Path thisFilePath;
  private final Path projectPath;
  private final EnvironmentVariableProvider environmentVariableProvider;

  private MSBuildState state;
  private ExpressionEvaluator expressionEvaluator;

  public MSBuildParser(Path path, EnvironmentVariableProvider environmentVariableProvider) {
    // The top file in the import tree is the project file
    this(path, path, environmentVariableProvider);
  }

  private MSBuildParser(
      Path path, Path projectPath, EnvironmentVariableProvider environmentVariableProvider) {
    this.thisFilePath = path;
    this.projectPath = projectPath;
    this.environmentVariableProvider = environmentVariableProvider;
  }

  public MSBuildState parse() {
    return parse(new MSBuildState(thisFilePath, projectPath, environmentVariableProvider));
  }

  public MSBuildState parse(MSBuildState initialState) {
    this.state = initialState;
    this.expressionEvaluator = new ExpressionEvaluator(this.state);

    final SAXBuilder builder = new SAXBuilder();
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    final Document dom;
    try {
      dom = builder.build(thisFilePath.toFile());
      dom.getRootElement().getChildren().stream()
          .filter(this::isConditionMet)
          .forEach(this::parseTopLevelElement);
    } catch (JDOMException | IOException e) {
      LOG.error("Error while parsing {}: ", thisFilePath.toAbsolutePath(), e);
    }

    return state;
  }

  private void parseTopLevelElement(Element element) {
    switch (element.getName()) {
      case "PropertyGroup":
        parsePropertyGroup(element);
        break;
      case "ItemGroup":
        parseItemGroup(element);
        break;
      case "Import":
        parseImport(element);
        break;
      default:
        break;
    }
  }

  private void parsePropertyGroup(Element element) {
    element.getChildren().stream().filter(this::isConditionMet).forEach(this::parseProperty);
  }

  private void parseProperty(Element element) {
    state.setProperty(element.getName(), expressionEvaluator.eval(element.getValue()));
  }

  private void parseItemGroup(Element element) {
    element.getChildren().stream().filter(this::isConditionMet).forEach(this::parseItem);
  }

  private void parseItem(Element element) {
    String identitiesStr = expressionEvaluator.eval(element.getAttributeValue("Include"));

    if (identitiesStr == null) {
      // Items inside targets *can* omit an Include if they are updating metadata or removing
      // specific items, but
      // we can safely assume that all items outside of targets with no Include are no-ops.
      return;
    }

    if (identitiesStr.chars().anyMatch(c -> c == '*' || c == '?')) {
      // MSBuild glob patterns are implemented differently from Java globs and are not common in
      // Delphi dprojs.
      // That being said, there's no particular reason we can't add support in the future, see:
      // https://learn.microsoft.com/en-us/visualstudio/msbuild/msbuild-items?view=vs-2022#use-wildcards-to-specify-items
      LOG.debug(
          "{} glob patterns are not supported, interpreting literally: {}",
          element.getName(),
          identitiesStr);
    }

    var identityGlobs = Splitter.on(';').trimResults().omitEmptyStrings().split(identitiesStr);

    for (String identity : identityGlobs) {
      // We have to evaluate each separately because built-in metadata may be different for each
      parseSingleItem(element, identity);
    }
  }

  private void parseSingleItem(Element element, String identity) {
    var protoItem =
        new MSBuildItem(identity, thisFilePath.getParent().toString(), Collections.emptyMap());
    var map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    // In item metadata, successive definitions can refer to previous ones
    var batchedExpressionEvaluator =
        new ExpressionEvaluator(
            state,
            metadataName -> {
              // We need the proto item for built-in metadata (FullPath, Extension, etc.)
              var metadata = protoItem.getMetadata(metadataName);
              if (metadata.isEmpty()) {
                metadata = map.getOrDefault(metadataName, "");
              }
              return metadata;
            });

    // Item metadata can be defined as attributes
    element
        .getAttributes()
        .forEach(attr -> map.put(attr.getName(), batchedExpressionEvaluator.eval(attr.getValue())));

    // Item metadata can be defined as child nodes
    element.getChildren().stream()
        .filter(this::isConditionMet)
        .forEach(
            metadata ->
                map.put(metadata.getName(), batchedExpressionEvaluator.eval(metadata.getValue())));

    state.addItem(
        element.getName(), new MSBuildItem(identity, thisFilePath.getParent().toString(), map));
  }

  private void parseImport(Element element) {
    if (!isConditionMet(element)) {
      return;
    }

    Optional<Path> importPath = resolvePathsFromElementAttribute(element, "Project").findFirst();

    if (importPath.isEmpty()) {
      return;
    } else if (!Files.exists(importPath.get())) {
      LOG.warn("Could not resolve imported file: {}", importPath.get());
      return;
    }

    var importState = state.deriveState(importPath.get());
    new MSBuildParser(importPath.get(), projectPath, environmentVariableProvider)
        .parse(importState);
    state.absorbState(importState);
  }

  private boolean isConditionMet(Element element) {
    ConditionEvaluator evaluator = new ConditionEvaluator(state);
    return evaluator.evaluate(element.getAttributeValue("Condition"));
  }

  private Stream<Path> resolvePathsFromElementAttribute(Element element, String attribute) {
    String include = expressionEvaluator.eval(element.getAttributeValue(attribute));
    if (include != null) {
      return Arrays.stream(StringUtils.split(include, ';'))
          .map(value -> resolvePath(value, element.getName()))
          .filter(Objects::nonNull);
    }
    return Stream.empty();
  }

  private Path resolvePath(String path, String elementName) {
    path = DelphiUtils.normalizeFileName(expressionEvaluator.eval(path));
    try {
      return DelphiUtils.resolvePathFromBaseDir(evaluationDirectory(), Path.of(path));
    } catch (InvalidPathException e) {
      LOG.warn("Path specified by {} is invalid: {}", elementName, path);
      LOG.debug("Exception:", e);
    }
    return null;
  }

  private Path evaluationDirectory() {
    return thisFilePath.getParent();
  }
}
