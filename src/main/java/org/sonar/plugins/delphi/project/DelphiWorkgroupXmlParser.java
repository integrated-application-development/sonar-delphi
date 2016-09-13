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

import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

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
   * @param xmlFile .groupproj XML file
   * @param delphiWorkGroup Workgroup to modify
   */
  public DelphiWorkgroupXmlParser(File xmlFile, DelphiWorkgroup delphiWorkGroup) {
    xml = xmlFile;
    workGroup = delphiWorkGroup;
    currentDir = DelphiUtils.normalizeFileName(xml.getAbsolutePath());
    currentDir = currentDir.substring(0, currentDir.lastIndexOf('/'));
  }

  /**
   * Parse provided .groupproj XML file
   */
  public void parse() {
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(xml.getAbsolutePath(), this);
    } catch (Exception ex) {
      DelphiUtils.LOG.error(ex.getMessage());
    }
  }

  @Override
  public void startElement(String uri, String localName, String rawName, Attributes attributes) throws SAXException {
    if ("Projects".equals(rawName)) {
      // new .dproj file
      String projectPath = DelphiUtils.resolveBacktracePath(currentDir, attributes.getValue("Include"));
      workGroup.addProject(new DelphiProject(new File(projectPath)));
    }
  }

}
