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
import java.util.ArrayList;
import java.util.List;

/**
 * Workgroup class, used to parse .groupproj file and holds list of projects
 */
public class DelphiWorkgroup {

  private List<DelphiProject> projects = new ArrayList<>();

  /**
   * Default, empty C-tor
   */
  public DelphiWorkgroup() {
  }

  /**
   * C-tor, gets project list from xml file
   * 
   * @param xmlFile .groupproj XML file
   * @throws IOException If XML file not found
   */
  public DelphiWorkgroup(File xmlFile) throws IOException {
    if (xmlFile == null) {
      throw new IllegalArgumentException("No .groupproje file provided.");
    } else if (!xmlFile.exists()) {
      throw new IOException(".grupproj XML not found:" + xmlFile.getAbsolutePath());
    }

    parseFile(xmlFile);
  }

  private void parseFile(File xmlFile) {
    DelphiWorkgroupXmlParser parser = new DelphiWorkgroupXmlParser(xmlFile, this);
    parser.parse();
  }

  /**
   * @return list of projects
   */
  public List<DelphiProject> getProjects() {
    return projects;
  }

  /**
   * Adds a project to the workgroup project list
   * 
   * @param newProject Project to add
   */
  public void addProject(DelphiProject newProject) {
    projects.add(newProject);
  }

}
