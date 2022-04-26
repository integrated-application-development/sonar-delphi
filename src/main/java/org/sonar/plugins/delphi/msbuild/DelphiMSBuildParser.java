package org.sonar.plugins.delphi.msbuild;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.msbuild.condition.ConditionEvaluator;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiMSBuildParser {
  private static final Logger LOG = Loggers.get(DelphiMSBuildParser.class);

  private final Path path;
  private final EnvironmentVariableProvider environmentVariableProvider;
  private final Path environmentProj;

  private ProjectProperties properties;
  private List<Path> sourceFiles;
  private List<DelphiProject> projects;

  public DelphiMSBuildParser(
      Path path, EnvironmentVariableProvider environmentVariableProvider, Path environmentProj) {
    this.path = path;
    this.environmentVariableProvider = environmentVariableProvider;
    this.environmentProj = environmentProj;
  }

  public Result parse() {
    this.properties = createProperties();
    this.sourceFiles = new ArrayList<>();
    this.projects = new ArrayList<>();

    final SAXBuilder builder = new SAXBuilder();
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    final Document dom;
    try {
      dom = builder.build(path.toFile());
      dom.getRootElement().getChildren().stream()
          .filter(this::isConditionMet)
          .forEach(this::parseTopLevelElement);
    } catch (JDOMException | IOException e) {
      LOG.error("Error while parsing {}: ", path.toAbsolutePath().toString(), e);
    }

    return new Result(properties, sourceFiles, projects);
  }

  protected ProjectProperties createProperties() {
    return ProjectProperties.create(environmentVariableProvider, environmentProj);
  }

  private void parseTopLevelElement(Element element) {
    parsePropertyGroup(element);
    parseItemGroup(element);
    parseImport(element);
  }

  private void parsePropertyGroup(Element element) {
    if (!element.getName().equals("PropertyGroup")) {
      return;
    }

    element.getChildren().stream().filter(this::isConditionMet).forEach(this::parseProperty);
  }

  private void parseProperty(Element element) {
    properties.set(element.getName(), properties.substitutor().replace(element.getValue()));
  }

  private void parseItemGroup(Element element) {
    if (!element.getName().equals("ItemGroup")) {
      return;
    }

    element.getChildren().stream()
        .filter(this::isConditionMet)
        .forEach(this::parseItemGroupElement);
  }

  private void parseItemGroupElement(Element element) {
    parseDCCReference(element);
    parseProjects(element);
  }

  private void parseDCCReference(Element element) {
    String name = element.getName();
    if (!name.equals("DCCReference")) {
      return;
    }

    Path importPath = resolvePathFromElementAttribute(element, "Include");
    if (importPath == null) {
      return;
    }

    String extension = FilenameUtils.getExtension(importPath.toString());
    if (!DelphiLanguage.FILE_SOURCE_CODE_SUFFIX.equalsIgnoreCase(extension)) {
      return;
    }

    if (!(Files.exists(importPath) && Files.isRegularFile(importPath))) {
      LOG.warn("File specified by {} does not exist: {}", name, importPath.toString());
      return;
    }

    sourceFiles.add(importPath);
  }

  private void parseProjects(Element element) {
    String name = element.getName();
    if (!name.equals("Projects")) {
      return;
    }

    Path importPath = resolvePathFromElementAttribute(element, "Include");
    if (importPath == null) {
      return;
    }

    if (!(Files.exists(importPath) && Files.isRegularFile(importPath))) {
      LOG.warn("File specified by {} does not exist: {}", name, importPath.toString());
      return;
    }

    DelphiProjectParser parser =
        new DelphiProjectParser(importPath, environmentVariableProvider, environmentProj);
    projects.add(parser.parse());
  }

  private void parseImport(Element element) {
    if (!element.getName().equals("Import")) {
      return;
    }

    Path importPath = resolvePathFromElementAttribute(element, "Project");
    if (importPath == null) {
      return;
    }

    DelphiOptionSetParser parser =
        new DelphiOptionSetParser(
            importPath, environmentVariableProvider, environmentProj, properties);
    Result result = parser.parse();
    this.properties = result.getProperties();
    this.sourceFiles.addAll(result.getSourceFiles());
  }

  private boolean isConditionMet(Element element) {
    ConditionEvaluator evaluator = new ConditionEvaluator(properties, evaluationDirectory());
    return evaluator.evaluate(element.getAttributeValue("Condition"));
  }

  private Path resolvePathFromElementAttribute(Element element, String attribute) {
    String include = element.getAttributeValue(attribute);
    if (include != null) {
      include = properties.substitutor().replace(include);
      try {
        return DelphiUtils.resolvePathFromBaseDir(evaluationDirectory(), Path.of(include));
      } catch (InvalidPathException e) {
        LOG.warn("Path specified by {} is invalid: {}", element.getName(), include);
        LOG.debug("Exception:", e);
      }
    }
    return null;
  }

  private Path evaluationDirectory() {
    return path.getParent();
  }

  public static class Result {
    private final ProjectProperties properties;
    private final List<Path> sourceFiles;
    private final List<DelphiProject> projects;

    Result(ProjectProperties properties, List<Path> sourceFiles, List<DelphiProject> projects) {
      this.properties = properties;
      this.sourceFiles = sourceFiles;
      this.projects = projects;
    }

    public ProjectProperties getProperties() {
      return properties;
    }

    public List<Path> getSourceFiles() {
      return sourceFiles;
    }

    public List<DelphiProject> getProjects() {
      return projects;
    }
  }
}
