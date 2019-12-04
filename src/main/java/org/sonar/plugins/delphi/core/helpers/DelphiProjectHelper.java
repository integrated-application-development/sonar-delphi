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
package org.sonar.plugins.delphi.core.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.project.DelphiWorkgroup;
import org.sonar.plugins.delphi.utils.DelphiUtils;

@ScannerSide
public class DelphiProjectHelper {
  private static final Logger LOG = Loggers.get(DelphiProjectHelper.class);
  public static final String DEFAULT_PROJECT_NAME = "Default Project";

  private final Configuration settings;
  private final FileSystem fs;
  private final List<File> excludedDirectories;

  private List<DelphiProject> projects;

  /**
   * ctor used by Sonar
   *
   * @param settings Project settings
   * @param fs Sonar FileSystem
   */
  public DelphiProjectHelper(@NotNull Configuration settings, @NotNull FileSystem fs) {
    this.settings = settings;
    this.fs = fs;
    this.excludedDirectories = detectExcludedDirectories();
  }

  /**
   * Returns a path to the Delphi standard library
   *
   * @return Path to standard library
   */
  public Path standardLibraryPath() {
    String path =
        settings
            .get(DelphiPlugin.STANDARD_LIBRARY_KEY)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Property '" + DelphiPlugin.STANDARD_LIBRARY_KEY + "' must be supplied."));

    return Path.of(path);
  }

  /**
   * Gets the directories specified in the search path
   *
   * @return List of search path directories
   */
  List<Path> getSearchPath() {
    List<Path> result = new ArrayList<>();

    String[] includedDirs = settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY);
    if (includedDirs == null || includedDirs.length == 0) {
      LOG.info("No search path specified in project configuration.");
      return result;
    }

    for (String path : includedDirs) {
      if (StringUtils.isBlank(path)) {
        continue;
      }
      File included = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), path.trim());

      if (!included.exists()) {
        LOG.warn("{} {}", "Search path directory does not exist: ", included.getAbsolutePath());
      } else if (!included.isDirectory()) {
        LOG.warn("{} {}", "Search path item is not a directory: ", included.getAbsolutePath());
      } else {
        result.add(included.toPath());
      }
    }

    return result;
  }

  /**
   * Gets the list of excluded directories
   *
   * @return List of excluded directories
   */
  public List<File> getExcludedDirectories() {
    return this.excludedDirectories;
  }

  private List<File> detectExcludedDirectories() {
    List<File> result = new ArrayList<>();
    String[] excludedNames = settings.getStringArray(DelphiPlugin.EXCLUDED_DIRECTORIES_KEY);

    if (excludedNames != null && excludedNames.length > 0) {
      for (String path : excludedNames) {
        if (StringUtils.isEmpty(path)) {
          continue;
        }
        File excluded =
            DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), path.trim());
        result.add(excluded);
        if (!excluded.exists()) {
          LOG.warn("{} {}", "Exclude directory does not exist: ", excluded.getAbsolutePath());
        }
      }
    } else {
      LOG.info("No exclude directories found in project configuration.");
    }
    return result;
  }

  /*
   * Gets the list of conditional defines specified in settings
   *
   * @returns List of conditional defines
   */
  private List<String> getConditionalDefines() {
    String[] conditionalDefines = settings.getStringArray(DelphiPlugin.CONDITIONAL_DEFINES_KEY);
    return Arrays.asList(ArrayUtils.nullToEmpty(conditionalDefines));
  }

  /*
   * Gets the list of unit scope names specified in settings
   *
   * @returns List of unit scope names
   */
  private List<String> getUnitScopeNames() {
    String[] unitScopeNames = settings.getStringArray(DelphiPlugin.UNIT_SCOPE_NAMES_KEY);
    return Arrays.asList(ArrayUtils.nullToEmpty(unitScopeNames));
  }

  List<File> inputFilesToFiles(List<InputFile> inputFiles) {
    List<File> result = new ArrayList<>();
    for (InputFile inputFile : inputFiles) {
      String absolutePath = DelphiUtils.uriToAbsolutePath(inputFile.uri());
      result.add(new File(absolutePath));
    }
    return result;
  }

  /** @return List of DelphiLanguage projects */
  public List<DelphiProject> getProjects() {
    if (projects == null) {
      projects = getProjectsFromSettings();

      if (projects.isEmpty()) {
        projects = getDefaultProject();
      }

      for (DelphiProject delphiProject : projects) {
        delphiProject.addDefinitions(getConditionalDefines());
        delphiProject.addUnitScopeNames(getUnitScopeNames());
      }
    }

    return projects;
  }

  /**
   * Creates a list of DelphiLanguage projects based on the settings file If the .gproj and .dproj
   * paths are undefined, an empty list is returned
   *
   * @return List of DelphiProjects.
   */
  private List<DelphiProject> getProjectsFromSettings() {
    Optional<String> gprojPath = settings.get(DelphiPlugin.WORKGROUP_FILE_KEY);
    Optional<String> dprojPath = settings.get(DelphiPlugin.PROJECT_FILE_KEY);

    if (gprojPath.isPresent()) {
      return getWorkgroupProjects(gprojPath.get());
    } else if (dprojPath.isPresent()) {
      return getDprojProject(dprojPath.get());
    }

    return Collections.emptyList();
  }

  /**
   * Creates a list of DelphiLanguage projects from a workgroup file
   *
   * @param gprojPath Path to the .gproj file
   * @return List of DelphiProjects
   */
  private List<DelphiProject> getWorkgroupProjects(String gprojPath) {
    try {
      File gprojFile = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), gprojPath);
      LOG.debug("{} {}", ".groupproj file found: ", gprojPath);
      DelphiWorkgroup workGroup = new DelphiWorkgroup(gprojFile);
      return workGroup.getProjects();
    } catch (IOException e) {
      LOG.error("Failed to create Delphi Workgroup: ", e);
      LOG.error("Skipping .groupproj reading, default configuration assumed.");
    }

    return Collections.emptyList();
  }

  /**
   * Creates a single-element list of DelphiLanguage projects from a dproj file
   *
   * @param dprojPath Path to the .dproj file
   * @return List of DelphiProjects
   */
  private List<DelphiProject> getDprojProject(String dprojPath) {
    try {
      File dprojFile = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), dprojPath);
      LOG.info("{} {}", ".dproj file found: ", dprojPath);
      DelphiProject newProject = new DelphiProject(dprojFile);
      return Collections.singletonList(newProject);
    } catch (IOException e) {
      LOG.error("Failed to create Delphi Project: ", e);
      LOG.error("Skipping .dproj reading, default configuration assumed.");
    }

    return Collections.emptyList();
  }

  /**
   * Creates a single-element list of DelphiLanguage projects, assuming a default configuration
   *
   * @return List of DelphiProjects
   */
  private List<DelphiProject> getDefaultProject() {
    DelphiProject newProject = new DelphiProject(DEFAULT_PROJECT_NAME);
    newProject.setSearchPath(getSearchPath());
    newProject.setSourceFiles(inputFilesToFiles(mainFiles()));
    return Collections.singletonList(newProject);
  }

  private List<InputFile> mainFiles() {
    FilePredicates p = fs.predicates();
    Iterable<InputFile> inputFiles =
        fs.inputFiles(p.and(p.hasLanguage(DelphiLanguage.KEY), p.hasType(InputFile.Type.MAIN)));
    List<InputFile> list = new ArrayList<>();
    inputFiles.forEach(list::add);
    return list;
  }

  private List<InputFile> testFiles() {
    FilePredicates p = fs.predicates();
    Iterable<InputFile> inputFiles =
        fs.inputFiles(p.and(p.hasLanguage(DelphiLanguage.KEY), p.hasType(InputFile.Type.TEST)));
    List<InputFile> list = new ArrayList<>();
    inputFiles.forEach(list::add);
    return list;
  }

  public boolean shouldExecuteOnProject() {
    return fs.hasFiles(fs.predicates().hasLanguage(DelphiLanguage.KEY));
  }

  public InputFile getFile(String path) {
    return getFile(new File(path));
  }

  public InputFile getFile(File file) {
    return fs.inputFile(fs.predicates().is(file));
  }

  public InputFile findFileInDirectories(String fileName) {
    for (InputFile inputFile : mainFiles()) {
      if (inputFile.filename().equalsIgnoreCase(fileName)) {
        return inputFile;
      }
    }
    return null;
  }

  public InputFile findTestFileInDirectories(String fileName) {
    String unitFileName = normalize(fileName);
    for (InputFile inputFile : testFiles()) {
      if (inputFile.filename().equalsIgnoreCase(unitFileName)) {
        return inputFile;
      }
    }
    return null;
  }

  private String normalize(String fileName) {
    if (!fileName.contains(".")) {
      return fileName + "." + DelphiLanguage.FILE_SOURCE_CODE_SUFFIX;
    }
    return fileName;
  }

  public File workDir() {
    return fs.workDir();
  }

  public String encoding() {
    return fs != null ? fs.encoding().name() : Charset.defaultCharset().name();
  }

  public String testTypeRegex() {
    return settings.get(DelphiPlugin.TEST_TYPE_REGEX_KEY).orElse("(?!)");
  }

  /**
   * Is file in excluded list?
   *
   * @param fileName File to check
   * @return True if file is excluded, false otherwise
   */
  public boolean isExcluded(String fileName) {
    if (excludedDirectories == null) {
      return false;
    }
    for (File excludedDir : excludedDirectories) {
      String normalizedFileName = DelphiUtils.normalizeFileName(fileName.toLowerCase());
      String excludedDirNormalizedPath =
          DelphiUtils.normalizeFileName(excludedDir.getAbsolutePath().toLowerCase());
      if (normalizedFileName.startsWith(excludedDirNormalizedPath)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is file excluded?
   *
   * @param delphiFile File to check
   * @return True if file is excluded, false otherwise
   */
  public boolean isExcluded(File delphiFile) {
    return isExcluded(delphiFile.getAbsolutePath());
  }

  /**
   * Gets code coverage excluded directories
   *
   * @return List of excluded directories, empty list if none
   */
  public List<File> getCodeCoverageExcludedDirectories() {
    List<File> list = new ArrayList<>();

    String[] sources = settings.getStringArray(DelphiPlugin.CC_EXCLUDED_KEY);
    if (sources == null || sources.length == 0) {
      return list;
    }
    for (String path : sources) {
      if (StringUtils.isEmpty(path)) {
        continue;
      }
      File excluded = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), path.trim());
      if (!excluded.exists()) {
        LOG.warn(
            "{} {}", "Excluded code coverage path does not exist: ", excluded.getAbsolutePath());
      } else if (!excluded.isDirectory()) {
        LOG.warn(
            "{} {}",
            "Excluded code coverage path is not a directory: ",
            excluded.getAbsolutePath());
      } else {
        list.add(excluded);
      }
    }
    return list;
  }
}
