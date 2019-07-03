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

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.sonar.api.internal.google.common.base.Splitter;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for parsing .dproj xml file
 */
public class ProjectXmlParser extends DefaultHandler {

  private String fileName;
  private String currentDir;
  private DelphiProject project;
  private boolean isReading;
  private String readData;

  /**
   * C-tor
   *
   * @param xml Xml file to parse
   * @param delphiProject DelphiProject class to modify
   */
  public ProjectXmlParser(File xml, DelphiProject delphiProject) {
    fileName = DelphiUtils.normalizeFileName(xml.getAbsolutePath());
    currentDir = fileName.substring(0, fileName.lastIndexOf('/'));
    project = delphiProject;
  }

  /**
   * Parses the document
   * @throws IOException If the SAXParser indicates a problem with the character stream
   */
  public void parse() throws IOException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      SAXParser parser = factory.newSAXParser();
      parser.parse(fileName, this);
    } catch (ParserConfigurationException | SAXException | RuntimeException e) {
      DelphiPlugin.LOG.error("{}: Error while parsing project file: ", fileName, e);
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
      case "DCC_UnitSearchPath":
        isReading = true;
        break;
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
      case "DCC_UnitSearchPath":
        handleUnitSearchPathEnd();
        break;
      case "DCC_Define":
        handleDefineEnd();
        break;
      default:
        // Do nothing
    }
  }

  private void handleDCCReferenceStart(Attributes attributes) {
    String path = DelphiUtils.resolveBacktracePath(currentDir, attributes.getValue("Include"));
    project.addFile(path);
  }

  private void handleVersionInfoKeysStart(Attributes attributes) {
    String name = attributes.getValue("Name");
    if ("ProductName".equals(name)) {
      isReading = true;
    }
  }


  private void handleVersionInfoKeysEnd() {
    project.setName(readData);
  }

  private void handleDefineEnd() {
    Iterable<String> defines = Splitter.on(';').split(readData);
    for (String define : defines) {
      if (define.startsWith("$") || "DEBUG".equals(define)) {
        continue;
      }
      project.addDefinition(define);
    }
  }

  private void handleUnitSearchPathEnd() {
    Iterable<String> paths = Splitter.on(';').split(readData);
    for (String path : paths) {
      if (path.startsWith("$")) {
        continue;
      }
      path = DelphiUtils.resolveBacktracePath(currentDir, path);
      project.addIncludeDirectory(path);
    }
  }
}
