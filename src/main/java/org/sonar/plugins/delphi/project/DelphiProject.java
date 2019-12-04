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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/** DelphiLanguage project class, it holds values parsed from *.dproj file. */
public class DelphiProject {
  private static final Logger LOG = Loggers.get(DelphiProject.class);

  private String name = "";
  private Set<String> definitions = new HashSet<>();
  private Set<String> unitScopeNames = new HashSet<>();
  private List<File> sourceFiles = new ArrayList<>();
  private List<Path> searchPath = new ArrayList<>();

  /**
   * C-tor, initializes project with name and empty files and definitions
   *
   * @param projName Project name
   */
  public DelphiProject(String projName) {
    name = projName;
  }

  /**
   * C-tor, initializes project with data loaded from xml file
   *
   * @param xmlFile XML file to parse
   * @throws IOException If file not found
   */
  public DelphiProject(@NotNull File xmlFile) throws IOException {
    parseFile(xmlFile);
  }

  /**
   * Parses xml file to gather data
   *
   * @param xmlFile File to parse
   * @throws IOException If file is not found
   */
  private void parseFile(File xmlFile) throws IOException {
    DelphiProjectXmlParser parser = new DelphiProjectXmlParser(xmlFile, this);
    parser.parse();
  }

  /**
   * Adds a source file to project
   *
   * @param file Source file to add
   */
  public void addSourceFile(File file) {
    if (!file.exists()) {
      LOG.warn("Could not add file to project: {}", file.getAbsolutePath());
    }

    if (DelphiUtils.acceptFile(file.getAbsolutePath())) {
      sourceFiles.add(file);
    }
  }

  /**
   * Adds a project preprocessor definition
   *
   * @param definition Preprocessor definition
   */
  public void addDefinition(String definition) {
    if (!StringUtils.isEmpty(definition)) {
      definitions.add(definition);
    }
  }

  /**
   * Adds a list of project preprocessor definitions
   *
   * @param definitions List of preprocessor definitions
   */
  public void addDefinitions(List<String> definitions) {
    this.definitions.addAll(definitions);
  }

  /**
   * adds directory where to search for include files
   *
   * @param directory directory with includes
   */
  public void addSearchPathDirectory(Path directory) {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      LOG.warn("Invalid search path directory: " + directory);
      return;
    }
    searchPath.add(directory);
  }

  public String getName() {
    return name;
  }

  public Set<String> getDefinitions() {
    return definitions;
  }

  public Set<String> getUnitScopeNames() {
    return unitScopeNames;
  }

  public List<File> getSourceFiles() {
    return sourceFiles;
  }

  public List<Path> getSearchPath() {
    return searchPath;
  }

  public void setName(String value) {
    name = value;
  }

  public void setDefinitions(Set<String> defs) {
    this.definitions = defs;
  }

  public void addUnitScopeName(String unitScopeName) {
    this.unitScopeNames.add(unitScopeName);
  }

  public void addUnitScopeNames(List<String> unitScopeNames) {
    this.unitScopeNames.addAll(unitScopeNames);
  }

  public void setSearchPath(List<Path> searchPath) {
    this.searchPath = searchPath;
  }

  public void setSourceFiles(List<File> sourceFiles) {
    this.sourceFiles = sourceFiles;
  }
}
