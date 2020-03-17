/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.project;

import static org.sonar.plugins.delphi.utils.DelphiUtils.resolvePathFromBaseDir;

import com.google.common.base.Splitter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Class for parsing .dproj xml file */
public class DelphiProjectXmlParser extends DefaultHandler {
  private static final Logger LOG = Loggers.get(DelphiProjectXmlParser.class);

  private final String fileName;
  private final Path baseDir;
  private final DelphiProject project;
  private boolean isReading;
  private String readData;

  /**
   * C-tor
   *
   * @param xml Xml file to parse
   * @param delphiProject DelphiProject class to modify
   */
  DelphiProjectXmlParser(Path xml, DelphiProject delphiProject) {
    fileName = xml.toAbsolutePath().toString();
    baseDir = xml.getParent();
    project = delphiProject;
  }

  /** Parses the document */
  void parse() {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      SAXParser parser = factory.newSAXParser();
      parser.parse(fileName, this);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOG.error("{}: Error while parsing project file: ", fileName, e);
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    if (isReading) {
      readData = new String(ch.clone(), start, length);
    }
  }

  @Override
  public void startElement(String uri, String localName, String rawName, Attributes attributes) {
    isReading = false;
    switch (rawName) {
      case "DCCReference":
        handleDCCReferenceStart(attributes);
        break;
      case "VersionInfoKeys":
        handleVersionInfoKeysStart(attributes);
        break;
      case "Import":
        handleImport(attributes);
        break;
      case "DCC_UnitSearchPath":
      case "DCC_UnitAlias":
      case "DCC_Namespace":
      case "DCC_Define":
        isReading = true;
        break;
      default:
        // Do nothing
    }
  }

  @Override
  public void endElement(String uri, String localName, String rawName) {
    if (!isReading) {
      return;
    }

    switch (rawName) {
      case "VersionInfoKeys":
        handleVersionInfoKeysEnd();
        break;
      case "DCC_UnitAlias":
        handleUnitAliasEnd();
        break;
      case "DCC_UnitSearchPath":
        handleUnitSearchPathEnd();
        break;
      case "DCC_Namespace":
        handleNamespaceEnd();
        break;
      case "DCC_Define":
        handleDefineEnd();
        break;
      default:
        // Do nothing
    }
  }

  private void handleDCCReferenceStart(Attributes attributes) {
    Path sourceFile = resolvePathFromBaseDir(baseDir, Path.of(attributes.getValue("Include")));
    project.addSourceFile(sourceFile);
  }

  private void handleVersionInfoKeysStart(Attributes attributes) {
    String name = attributes.getValue("Name");
    if ("ProductName".equals(name)) {
      isReading = true;
    }
  }

  private void handleImport(Attributes attributes) {
    String importPath = attributes.getValue("Project");
    if (importPath.contains("$(")) {
      return;
    }

    Path path = resolvePathFromBaseDir(baseDir, Path.of(importPath));
    DelphiProjectXmlParser parser = new DelphiProjectXmlParser(path, project);
    parser.parse();
  }

  private void handleVersionInfoKeysEnd() {
    project.setName(readData);
  }

  private void handleUnitAliasEnd() {
    parseListData(readData)
        .forEach(
            item -> {
              if (StringUtils.countMatches(item, '=') != 1) {
                LOG.warn("Invalid unit alias syntax: '{}'", item);
                return;
              }
              int equalIndex = item.indexOf('=');
              String unitAlias = item.substring(0, equalIndex);
              String unitName = item.substring(equalIndex + 1);
              project.addUnitAlias(unitAlias, unitName);
            });
  }

  private void handleUnitSearchPathEnd() {
    parseListData(readData)
        .forEach(
            path -> {
              Path searchPathDirectory = resolvePathFromBaseDir(baseDir, Path.of(path));
              project.addSearchDirectory(searchPathDirectory);
            });
  }

  private void handleNamespaceEnd() {
    parseListData(readData).forEach(project::addUnitScopeName);
  }

  private void handleDefineEnd() {
    parseListData(readData).forEach(project::addDefinition);
  }

  private static List<String> parseListData(String data) {
    List<String> result = Lists.newArrayList(Splitter.on(';').split(data));
    result.removeIf(item -> item.startsWith("$"));
    return result;
  }
}
