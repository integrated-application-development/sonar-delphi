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
   */
  public void parse() {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      SAXParser parser = factory.newSAXParser();
      parser.parse(fileName, this);
    } catch (ParserConfigurationException | SAXException | IOException e) {
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
  public void startElement(String uri, String localName, String rawName, Attributes attributes)
      throws SAXException {
    isReading = false;

    if ("DCCReference".equals(rawName)) {
      // new source file
      String path = DelphiUtils.resolveBacktracePath(currentDir, attributes.getValue("Include"));
      try {
        project.addFile(path);
      } catch (RuntimeException e) {
        throw new SAXException(e);
      }
    } else if ("VersionInfoKeys".equals(rawName)) {
      // project name
      String name = attributes.getValue("Name");
      if (name != null && "ProductName".equals(name)) {
        isReading = true;
      }
    } else if ("DCC_UnitSearchPath".equals(rawName)) {
      isReading = true;
    } else if ("DCC_Define".equals(rawName)) {
      isReading = true;
    }

  }

  @Override
  public void endElement(String uri, String localName, String rawName) throws SAXException {
    if (!isReading) {
      return;
    }

    if ("VersionInfoKeys".equals(rawName)) {
      // add project name
      project.setName(readData);
    } else if ("DCC_Define".equals(rawName)) {
      // add define
      Iterable<String> defines = Splitter.on(';').split(readData);
      for (String define : defines) {
        if (define.startsWith("$") || "DEBUG".equals(define)) {
          continue;
        }
        project.addDefinition(define);
      }
    } else if ("DCC_UnitSearchPath".equals(rawName)) {
      // add include directories
      Iterable<String> paths = Splitter.on(';').split(readData);
      for (String path : paths) {
        if (path.startsWith("$")) {
          continue;
        }
        path = DelphiUtils.resolveBacktracePath(currentDir, path);
        try {
          project.addIncludeDirectory(path);
        } catch (RuntimeException e) {
          throw new SAXException(e);
        }
      }
    }

  }

}
