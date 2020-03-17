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

import static org.sonar.plugins.delphi.utils.DelphiUtils.normalizeFileName;
import static org.sonar.plugins.delphi.utils.DelphiUtils.resolvePathFromBaseDir;

import java.io.IOException;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Parser for Delphi *.groupproj XML file */
class DelphiGroupProjXmlParser extends DefaultHandler {
  private static final Logger LOG = Loggers.get(DelphiGroupProjXmlParser.class);
  private final String filename;
  private final DelphiGroupProj workGroup;
  private final Path baseDir;

  /**
   * C-tor
   *
   * @param xmlFile .groupproj XML file
   * @param delphiWorkGroup Workgroup to modify
   */
  DelphiGroupProjXmlParser(Path xmlFile, DelphiGroupProj delphiWorkGroup) {
    workGroup = delphiWorkGroup;
    filename = normalizeFileName(xmlFile.toAbsolutePath().toString());
    baseDir = xmlFile.getParent();
  }

  /** Parse provided .groupproj XML file */
  void parse() {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      SAXParser parser = factory.newSAXParser();
      parser.parse(filename, this);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOG.error("{}: Error parsing workgroup file: ", filename, e);
    }
  }

  @Override
  public void startElement(String uri, String localName, String rawName, Attributes attributes) {
    if ("Projects".equals(rawName)) {
      Path projectPath = resolvePathFromBaseDir(baseDir, Path.of(attributes.getValue("Include")));
      DelphiProject project = DelphiProject.parse(projectPath);
      workGroup.addProject(project);
    }
  }
}
