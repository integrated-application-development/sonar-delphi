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

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;
import static org.sonar.plugins.delphi.utils.DelphiUtils.inputFilesToPaths;

import com.google.common.collect.Iterables;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
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
import org.sonar.plugins.delphi.utils.DelphiUtils;

@ScannerSide
public class DelphiProjectHelper {
  private static final Logger LOG = Loggers.get(DelphiProjectHelper.class);
  private final Configuration settings;
  private final FileSystem fs;
  private final List<Path> searchDirectories;
  private final Set<String> conditionalDefines;
  private final Set<String> unitScopeNames;
  private final List<DelphiProject> projects;

  /**
   * ctor used by Sonar
   *
   * @param settings Project settings
   * @param fs Sonar FileSystem
   */
  public DelphiProjectHelper(@NotNull Configuration settings, @NotNull FileSystem fs) {
    this.settings = settings;
    this.fs = fs;
    this.projects = new ArrayList<>();

    String[] searchDirectoriesSetting =
        nullToEmpty(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY));
    searchDirectories = new ArrayList<>();

    String[] defineSetting = settings.getStringArray(DelphiPlugin.CONDITIONAL_DEFINES_KEY);
    conditionalDefines = Arrays.stream(nullToEmpty(defineSetting)).collect(Collectors.toSet());

    String[] scopeNamesSettings = settings.getStringArray(DelphiPlugin.UNIT_SCOPE_NAMES_KEY);
    unitScopeNames = Arrays.stream(nullToEmpty(scopeNamesSettings)).collect(Collectors.toSet());

    for (String path : searchDirectoriesSetting) {
      if (StringUtils.isBlank(path)) {
        continue;
      }
      File included = DelphiUtils.resolveAbsolutePath(fs.baseDir().getAbsolutePath(), path.trim());

      if (!included.exists()) {
        LOG.warn("{} {}", "Search path directory does not exist: ", included.getAbsolutePath());
      } else if (!included.isDirectory()) {
        LOG.warn("{} {}", "Search path item is not a directory: ", included.getAbsolutePath());
      } else {
        searchDirectories.add(included.toPath());
      }
    }

    this.indexProjects();

    for (DelphiProject project : projects) {
      searchDirectories.addAll(project.getSearchDirectories());
      conditionalDefines.addAll(project.getConditionalDefines());
      unitScopeNames.addAll(project.getUnitScopeNames());
    }
  }

  private void indexProjects() {
    FilePredicates p = fs.predicates();
    Iterable<InputFile> dprojFiles = fs.inputFiles(p.and(p.hasExtension("dproj")));
    Iterable<InputFile> gprojFiles = fs.inputFiles(p.and(p.hasExtension("groupproj")));

    LOG.info(
        "Indexing {} dproj file(s) and {} groupproj file(s)...",
        Iterables.size(dprojFiles),
        Iterables.size(gprojFiles));

    inputFilesToPaths(dprojFiles).forEach(this::indexDprojProject);
    inputFilesToPaths(gprojFiles).forEach(this::indexGroupprojProject);
  }

  private void indexDprojProject(Path dprojFile) {
    DelphiProject newProject = DelphiProject.parse(dprojFile);
    projects.add(newProject);
  }

  private void indexGroupprojProject(Path gprojFile) {
    DelphiGroupProj workGroup = DelphiGroupProj.parse(gprojFile);
    projects.addAll(workGroup.getProjects());
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
  public List<Path> getSearchDirectories() {
    return searchDirectories;
  }

  /*
   * Gets the set of conditional defines specified in settings
   *
   * @returns set of conditional defines
   */
  public Set<String> getConditionalDefines() {
    return conditionalDefines;
  }

  /*
   * Gets the set of unit scope names specified in settings
   *
   * @returns set of unit scope names
   */
  public Set<String> getUnitScopeNames() {
    return unitScopeNames;
  }

  public Iterable<InputFile> mainFiles() {
    FilePredicates p = fs.predicates();
    return fs.inputFiles(p.and(p.hasLanguage(DelphiLanguage.KEY), p.hasType(InputFile.Type.MAIN)));
  }

  private Iterable<InputFile> testFiles() {
    FilePredicates p = fs.predicates();
    return fs.inputFiles(p.and(p.hasLanguage(DelphiLanguage.KEY), p.hasType(InputFile.Type.TEST)));
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
