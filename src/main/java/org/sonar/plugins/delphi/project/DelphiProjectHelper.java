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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.compiler.CompilerVersion;
import org.sonar.plugins.delphi.compiler.PredefinedConditionals;
import org.sonar.plugins.delphi.compiler.Toolchain;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.utils.DelphiUtils;

@ScannerSide
public class DelphiProjectHelper {
  private static final Logger LOG = Loggers.get(DelphiProjectHelper.class);
  private final Configuration settings;
  private final FileSystem fs;
  private final List<DelphiProject> projects;
  private final Toolchain toolchain;
  private final CompilerVersion compilerVersion;
  private final List<Path> searchDirectories;
  private final Set<String> conditionalDefines;
  private final Set<String> unitScopeNames;
  private final Map<String, String> unitAliases;

  /**
   * Constructor
   *
   * @param settings Project settings
   * @param fs Sonar FileSystem
   */
  public DelphiProjectHelper(@NotNull Configuration settings, @NotNull FileSystem fs) {
    this.settings = settings;
    this.fs = fs;
    this.projects = new ArrayList<>();
    this.toolchain = getToolchainFromSettings();
    this.compilerVersion = getCompilerVersionFromSettings();
    this.searchDirectories = getSearchDirectoriesFromSettings();
    this.conditionalDefines = getPredefinedConditionalDefines();
    this.unitScopeNames = getSetFromSettings(DelphiPlugin.UNIT_SCOPE_NAMES_KEY);
    this.unitAliases = getUnitAliasesFromSettings();

    this.indexProjects();

    for (DelphiProject project : projects) {
      this.searchDirectories.addAll(project.getSearchDirectories());
      this.conditionalDefines.addAll(project.getConditionalDefines());
      this.unitScopeNames.addAll(project.getUnitScopeNames());
      this.unitAliases.putAll(project.getUnitAliases());
    }

    this.conditionalDefines.addAll(getSetFromSettings(DelphiPlugin.CONDITIONAL_DEFINES_KEY));
    this.conditionalDefines.removeAll(getSetFromSettings(DelphiPlugin.CONDITIONAL_UNDEFINES_KEY));
  }

  private Set<String> getSetFromSettings(String key) {
    return Arrays.stream(nullToEmpty(settings.getStringArray(key))).collect(Collectors.toSet());
  }

  private Toolchain getToolchainFromSettings() {
    return EnumUtils.getEnumIgnoreCase(
        Toolchain.class,
        settings.get(DelphiPlugin.COMPILER_TOOLCHAIN_KEY).orElse(null),
        DelphiPlugin.COMPILER_TOOLCHAIN_DEFAULT);
  }

  private CompilerVersion getCompilerVersionFromSettings() {
    String versionSymbol =
        settings
            .get(DelphiPlugin.COMPILER_VERSION_KEY)
            .orElse(DelphiPlugin.COMPILER_VERSION_DEFAULT.symbol());

    try {
      return CompilerVersion.fromVersionSymbol(versionSymbol);
    } catch (CompilerVersion.FormatException e) {
      LOG.warn(
          "Defaulting to compiler version \"{}\" because the provided one was invalid: \"{}\"",
          DelphiPlugin.COMPILER_VERSION_DEFAULT,
          versionSymbol);
      LOG.debug("Exception: ", e);
      return DelphiPlugin.COMPILER_VERSION_DEFAULT;
    }
  }

  private List<Path> getSearchDirectoriesFromSettings() {
    String[] searchDirectoriesSetting =
        nullToEmpty(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY));
    List<Path> result = new ArrayList<>();

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
        result.add(included.toPath());
      }
    }
    return result;
  }

  private Map<String, String> getUnitAliasesFromSettings() {
    String[] aliases = nullToEmpty(settings.getStringArray(DelphiPlugin.UNIT_ALIASES_KEY));
    Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Arrays.stream(aliases)
        .forEach(
            item -> {
              if (StringUtils.countMatches(item, '=') != 1) {
                LOG.warn("Invalid unit alias syntax: '{}'", item);
                return;
              }
              int equalIndex = item.indexOf('=');
              String unitAlias = item.substring(0, equalIndex).trim();
              String unitName = item.substring(equalIndex + 1).trim();
              result.put(unitAlias, unitName);
            });

    return result;
  }

  private Set<String> getPredefinedConditionalDefines() {
    return PredefinedConditionals.getConditionalDefines(toolchain, compilerVersion);
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
   * Returns a path to the Delphi standard library, as specified in settings
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
   * Gets the search directories specified in settings and project files
   *
   * @return List of search path directories
   */
  public List<Path> getSearchDirectories() {
    return searchDirectories;
  }

  /**
   * Get the compiler version
   *
   * @return the compiler version
   */
  public CompilerVersion getCompilerVersion() {
    return compilerVersion;
  }

  /**
   * Get the compiler toolchain
   *
   * @return the compiler toolchain
   */
  public Toolchain getToolchain() {
    return toolchain;
  }

  /**
   * Gets the set of conditional defines specified in settings and project files
   *
   * @return set of conditional defines
   */
  public Set<String> getConditionalDefines() {
    return conditionalDefines;
  }

  /**
   * Gets the set of unit scope names specified in settings and project files
   *
   * @return set of unit scope names
   */
  public Set<String> getUnitScopeNames() {
    return unitScopeNames;
  }

  /**
   * Gets the map of unit aliases specified in settings and project files
   *
   * @return map of unit aliases
   */
  public Map<String, String> getUnitAliases() {
    return unitAliases;
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

  public String encoding() {
    return fs != null ? fs.encoding().name() : Charset.defaultCharset().name();
  }

  public String testSuiteType() {
    return settings.get(DelphiPlugin.TEST_SUITE_TYPE_KEY).orElse("");
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
