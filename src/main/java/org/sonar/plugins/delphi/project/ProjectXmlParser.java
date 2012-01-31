/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import org.apache.xerces.parsers.SAXParser;
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
   * @param xml
   *          Xml file to parse
   * @param delphiProject
   *          DelphiProject class to modify
   */
  public ProjectXmlParser(File xml, DelphiProject delphiProject) {
    fileName = DelphiUtils.normalizeFileName(xml.getAbsolutePath());
    currentDir = fileName.substring(0, fileName.lastIndexOf('/'));
    project = delphiProject;
  }

  /**
   * Parses the document
   * @throws IOException 
   * @throws SAXException 
   */
  public void parse() throws SAXException, IOException {
      SAXParser parser = new SAXParser();
      parser.setContentHandler(this);
      parser.setErrorHandler(this);
      parser.parse(fileName);
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    if (isReading) {
      readData = new String(ch.clone(), start, length);
    }
  }

  @Override
  public void startElement(String uri, String localName, String rawName, Attributes attributes) throws SAXException {
    isReading = false;

    if (rawName.equals("DCCReference")) { // new source file
      String path = DelphiUtils.resolveBacktracePath(currentDir, attributes.getValue("Include"));
      try {
        project.addFile(path);
      } catch (IOException e) {
        DelphiUtils.LOG.error(e.getMessage());
        DelphiUtils.getDebugLog().println(e.getMessage());
        throw new SAXException(e);
      }
    } else if (rawName.equals("VersionInfoKeys")) { // project name

      String name = attributes.getValue("Name");
      if (name != null && name.equals("ProductName")) {
        isReading = true;
      }
    } else if (rawName.equals("DCC_UnitSearchPath")) {
      isReading = true;
    } else if (rawName.equals("DCC_Define")) {
      isReading = true;
    }

  }

  @Override
  public void endElement(String uri, String localName, String rawName) throws SAXException {
    if ( !isReading) {
      return;
    }

    if (rawName.equals("VersionInfoKeys")) { // add project name
      project.setName(readData);
    }

    else if (rawName.equals("DCC_Define")) { // add define
      String[] defines = readData.split(";");
      for (String define : defines) {
        if (define.startsWith("$")) {
          continue;
        } else if (define.equals("DEBUG")) {
          continue;
        }
        project.addDefinition(define);
      }
    }

    else if (rawName.equals("DCC_UnitSearchPath")) { // add include directories

      String[] paths = readData.split(";");
      for (String path : paths) {
        if (path.startsWith("$")) {
          continue;
        }
        path = DelphiUtils.resolveBacktracePath(currentDir, path);
        try {
          project.addIncludeDirectory(path);
        } catch (IOException e) {
          DelphiUtils.LOG.error(e.getMessage());
          DelphiUtils.getDebugLog().println(e.getMessage());
          throw new SAXException(e);
        }
      }
    }

  }

}
