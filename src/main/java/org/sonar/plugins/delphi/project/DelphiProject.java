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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DelphiLanguage project class, it holds values parsed from *.dproj file.
 */
public class DelphiProject {

  private String name = "";
  private List<String> definitions = new ArrayList<String>();
  private List<File> files = new ArrayList<File>();
  private List<File> includeDirectories = new ArrayList<File>();
  private File file = null;

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
   */
  public DelphiProject(File xml) {
    try {
      parseFile(xml);
    } catch (IOException e) {
      DelphiUtils.LOG.error("Could not find .dproj file: " + xml.getAbsolutePath());
    } catch (IllegalArgumentException e) {
      DelphiUtils.LOG.error("No .dproj file to parse. (null)");
    } catch (XMLStreamException e) {
      DelphiUtils.LOG.error(".dproj xml error: " + e.getMessage());
    } catch (SAXException e) {
      DelphiUtils.LOG.error(".dproj xml error: " + e.getMessage());
    }
  }

  /**
   * Adds a source file to project
   * 
   * @param path File path
   * @throws IOException If file not found
   */
  public void addFile(String path) throws IOException {
    File newFile = new File(path);
    if (!newFile.exists()) {
      throw new IOException("Could not add file to project: " + newFile.getAbsolutePath());
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
   * adds directory where to search for include files
   * 
   * @param directory directory with includes
   * @throws IOException if directory is invalid
   */
  public void addIncludeDirectory(String directory) throws IOException {
    if (!StringUtils.isEmpty(directory)) {
      File dir = new File(directory);

      if (!dir.exists() || !dir.isDirectory()) {
        throw new IOException("Invalid include directory: " + dir.getAbsolutePath());
      }

      includeDirectories.add(dir);
    }
  }

  /**
   * Parses xml file to gather data
   * 
   * @param xml File to parse
   * @throws IOException If file not found
   * @throws SAXException when parsing error occurs
   * @throws IllegalArgumentException If file == null
   */
  private void parseFile(File xml) throws IOException, XMLStreamException, SAXException {
    if (xml == null) {
      throw new IllegalArgumentException("No xml file passed");
    } else if (!xml.exists()) {
      throw new IOException("Project file not found");
    }

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

  public void setSourceFiles(List<InputFile> list) {
    List<File> files = new ArrayList<File>();
    for (InputFile inputFile : list) {
      files.add(inputFile.file());
    }
    this.files = files;
  }

}
