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
import org.apache.commons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/** DelphiLanguage project class, it holds values parsed from *.dproj file. */
public class DelphiProject {
  private static final Logger LOG = Loggers.get(DelphiProject.class);

  private String name = "";
  private List<String> definitions = new ArrayList<>();
  private List<File> files = new ArrayList<>();
  private List<File> includeDirectories = new ArrayList<>();
  private File file;

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
   * @param xml XML file to parse
   * @throws IllegalArgumentException If xml is null
   * @throws IOException If file not found
   */
  public DelphiProject(File xml) throws IOException {
    if (xml == null) {
      throw new IllegalArgumentException("No xml file passed into DelphiProject constructor");
    }

    parseFile(xml);
  }

  /**
   * Adds a source file to project
   *
   * @param path File path
   */
  public void addFile(String path) {
    File newFile = new File(path);
    if (!newFile.exists()) {
      LOG.warn("Could not add file to project: {}", newFile.getAbsolutePath());
    }

    if (DelphiUtils.acceptFile(newFile.getAbsolutePath())) {
      files.add(newFile);
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
   * @throws RuntimeException if directory is invalid
   */
  public void addIncludeDirectory(String directory) {
    if (!StringUtils.isEmpty(directory)) {
      File dir = new File(directory);

      if (!dir.exists() || !dir.isDirectory()) {
        throw new RuntimeException("Invalid include directory: " + dir.getAbsolutePath());
      }

      includeDirectories.add(dir);
    }
  }

  /**
   * Parses xml file to gather data
   *
   * @param xml File to parse
   * @throws IllegalArgumentException If file == null
   * @throws IOException If file is not found
   */
  private void parseFile(File xml) throws IOException {
    file = xml;
    ProjectXmlParser parser = new ProjectXmlParser(file, this);
    parser.parse();
  }

  public String getName() {
    return name;
  }

  public List<String> getDefinitions() {
    return definitions;
  }

  public List<File> getSourceFiles() {
    return files;
  }

  public List<File> getIncludeDirectories() {
    return includeDirectories;
  }

  public File getXmlFile() {
    return file;
  }

  public void setName(String value) {
    name = value;
  }

  public void setDefinitions(List<String> defs) {
    this.definitions = defs;
  }

  public void setIncludeDirectories(List<File> includes) {
    this.includeDirectories = includes;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setSourceFiles(List<File> list) {
    this.files = list;
  }
}
