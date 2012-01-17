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

import org.apache.xerces.parsers.SAXParser;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for Delphi *.groupproj XML file
 * 
 */
public class DelphiWorkgroupXmlParser extends DefaultHandler {

  private File xml;
  private DelphiWorkgroup workGroup;
  private String currentDir;

  /**
   * C-tor
   * 
   * @param xmlFile
   *          .groupproj XML file
   * @param _workGroup
   *          Workgroup to modify
   */
  public DelphiWorkgroupXmlParser(File xmlFile, DelphiWorkgroup delphiWorkGroup) {
    xml = xmlFile;
    workGroup = delphiWorkGroup;
    currentDir = xml.getAbsolutePath().substring(0, xml.getAbsolutePath().lastIndexOf("\\"));
  }

  /**
   * Parse provided .groupproj XML file
   */
  public void parse() {
    try {
      SAXParser parser = new SAXParser();
      parser.setContentHandler(this);
      parser.setErrorHandler(this);
      parser.parse(xml.getAbsolutePath());
    } catch (Exception ex) {
      DelphiUtils.getDebugLog().println(ex.getMessage());
    }
  }

  @Override
  public void startElement(String uri, String localName, String rawName, Attributes attributes) throws SAXException {
    if (rawName.equals("Projects")) { // new .dproj file
      String projectPath = DelphiUtils.resolveBacktracePath(currentDir, attributes.getValue("Include"));
      workGroup.addProject(new DelphiProject(new File(projectPath)));
    }
  }

}
