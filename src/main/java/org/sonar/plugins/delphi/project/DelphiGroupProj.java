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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/** Groupproj class, used to parse .groupproj file and holds list of projects */
public class DelphiGroupProj {
  private static final Logger LOG = Loggers.get(DelphiGroupProj.class);

  private final List<DelphiProject> projects = new ArrayList<>();

  private DelphiGroupProj() {
    // Hide default constructor
  }

  /**
   * Parses xml file to gather data
   *
   * @param xmlFile File to parse
   * @return DelphiGroupProj object
   */
  public static DelphiGroupProj parse(Path xmlFile) {
    LOG.debug("Indexing workgroup file: {}", xmlFile.toAbsolutePath());
    DelphiGroupProj groupProj = new DelphiGroupProj();
    DelphiGroupProjXmlParser parser = new DelphiGroupProjXmlParser(xmlFile, groupProj);
    parser.parse();
    return groupProj;
  }

  /**
   * Returns a list of Delphi projects
   *
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
