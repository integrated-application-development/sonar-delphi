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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/** DelphiLanguage project class, it holds data parsed from a *.dproj file. */
public class DelphiProject {
  private static final Logger LOG = Loggers.get(DelphiProject.class);

  private String name = "";
  private Set<String> definitions = new HashSet<>();
  private final Set<String> unitScopeNames = new HashSet<>();
  private final List<Path> sourceFiles = new ArrayList<>();
  private final List<Path> searchDirectories = new ArrayList<>();
  private final Map<String, String> unitAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private DelphiProject() {
    // Hide default constructor
  }

  /**
   * Parses xml file to gather data
   *
   * @param xmlFile File to parse
   * @return DelphiProject object
   */
  public static DelphiProject parse(Path xmlFile) {
    LOG.debug("Indexing project file: {}", xmlFile.toAbsolutePath());
    DelphiProject project = new DelphiProject();
    DelphiProjectXmlParser parser = new DelphiProjectXmlParser(xmlFile, project);
    parser.parse();
    return project;
  }

  public static DelphiProject create(String name) {
    DelphiProject project = new DelphiProject();
    project.setName(name);
    return project;
  }

  /**
   * Adds a source file to project
   *
   * @param sourceFile Source file to add
   */
  public void addSourceFile(Path sourceFile) {
    if (DelphiUtils.acceptFile(sourceFile)) {
      if (!Files.exists(sourceFile) || !Files.isRegularFile(sourceFile)) {
        LOG.warn("Could not add file to project: {}", sourceFile.toAbsolutePath().toString());
        return;
      }
      sourceFiles.add(sourceFile);
    }
  }

  /**
   * Adds a project preprocessor definition
   *
   * @param definition Preprocessor definition
   */
  public void addDefinition(String definition) {
    definitions.add(definition);
  }

  public void addUnitAlias(String unitAlias, String unitName) {
    unitAliases.put(unitAlias, unitName);
  }

  /**
   * adds directory where to search for include files
   *
   * @param directory directory with includes
   */
  public void addSearchDirectory(Path directory) {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      LOG.warn("Invalid search path directory: {}", directory);
      return;
    }
    searchDirectories.add(directory);
  }

  public void setName(String value) {
    name = value;
  }

  public void addUnitScopeName(String unitScopeName) {
    this.unitScopeNames.add(unitScopeName);
  }

  public String getName() {
    return name;
  }

  public Set<String> getConditionalDefines() {
    return definitions;
  }

  public Set<String> getUnitScopeNames() {
    return unitScopeNames;
  }

  public List<Path> getSourceFiles() {
    return sourceFiles;
  }

  public List<Path> getSearchDirectories() {
    return searchDirectories;
  }

  public Map<String, String> getUnitAliases() {
    return unitAliases;
  }
}
