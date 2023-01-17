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
package au.com.integradev.delphi.msbuild;

import static au.com.integradev.delphi.utils.DelphiUtils.inputFilesToPaths;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

import au.com.integradev.delphi.DelphiPlugin;
import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.PredefinedConditionals;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.core.DelphiLanguage;
import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public class DelphiProjectHelper {
  private static final Logger LOG = Loggers.get(DelphiProjectHelper.class);
  private final Configuration settings;
  private final FileSystem fs;
  private final EnvironmentVariableProvider environmentVariableProvider;
  private final List<DelphiProject> projects;
  private final Toolchain toolchain;
  private final CompilerVersion compilerVersion;
  private final List<Path> searchDirectories;
  private final List<Path> debugSourceDirectories;
  private final Set<String> conditionalDefines;
  private final Set<String> unitScopeNames;
  private final Map<String, String> unitAliases;
  private boolean indexedProjects;

  /**
   * Constructor
   *
   * @param settings Project settings
   * @param fs Sonar FileSystem
   */
  public DelphiProjectHelper(
      @NotNull Configuration settings,
      @NotNull FileSystem fs,
      @NotNull EnvironmentVariableProvider environmentVariableProvider) {
    this.settings = settings;
    this.fs = fs;
    this.environmentVariableProvider = environmentVariableProvider;
    this.projects = new ArrayList<>();
    this.toolchain = getToolchainFromSettings();
    this.compilerVersion = getCompilerVersionFromSettings();
    this.searchDirectories = getSearchDirectoriesFromSettings();
    this.debugSourceDirectories = new ArrayList<>();
    this.conditionalDefines = getPredefinedConditionalDefines();
    this.unitScopeNames = getSetFromSettings(DelphiPlugin.UNIT_SCOPE_NAMES_KEY);
    this.unitAliases = getUnitAliasesFromSettings();
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
    if (indexedProjects) {
      return;
    }

    FilePredicates p = fs.predicates();
    Iterable<InputFile> dprojFiles = fs.inputFiles(p.and(p.hasExtension("dproj")));
    Iterable<InputFile> gprojFiles = fs.inputFiles(p.and(p.hasExtension("groupproj")));

    if (dprojFiles == null) {
      dprojFiles = Collections.emptyList();
    }

    if (gprojFiles == null) {
      gprojFiles = Collections.emptyList();
    }

    LOG.info(
        "Indexing {} dproj file(s) and {} groupproj file(s)...",
        Iterables.size(dprojFiles),
        Iterables.size(gprojFiles));

    inputFilesToPaths(dprojFiles).forEach(this::indexProject);
    inputFilesToPaths(gprojFiles).forEach(this::indexProjectGroup);

    for (DelphiProject project : projects) {
      searchDirectories.addAll(project.getSearchDirectories());
      debugSourceDirectories.addAll(project.getDebugSourceDirectories());
      conditionalDefines.addAll(project.getConditionalDefines());
      unitScopeNames.addAll(project.getUnitScopeNames());
      unitAliases.putAll(project.getUnitAliases());
    }

    conditionalDefines.addAll(getSetFromSettings(DelphiPlugin.CONDITIONAL_DEFINES_KEY));
    conditionalDefines.removeAll(getSetFromSettings(DelphiPlugin.CONDITIONAL_UNDEFINES_KEY));

    indexedProjects = true;
  }

  @VisibleForTesting
  Path environmentProjPath() {
    Path bdsPath = bdsPath();
    if (bdsPath.getNameCount() < 3) {
      return null;
    }

    String appdata = environmentVariableProvider.getenv("APPDATA");
    if (appdata == null) {
      return null;
    }

    Path appdataPath = Path.of(appdata);
    String companyName = bdsPath.getParent().getParent().getFileName().toString();
    String productVersion = bdsPath.getFileName().toString();

    return appdataPath
        .resolve(companyName)
        .resolve("BDS")
        .resolve(productVersion)
        .resolve("environment.proj");
  }

  private void indexProject(Path dprojFile) {
    DelphiProjectParser parser =
        new DelphiProjectParser(dprojFile, environmentVariableProvider, environmentProjPath());
    DelphiProject newProject = parser.parse();
    projects.add(newProject);
  }

  private void indexProjectGroup(Path projectGroup) {
    DelphiProjectGroupParser parser =
        new DelphiProjectGroupParser(
            projectGroup, environmentVariableProvider, environmentProjPath());
    projects.addAll(parser.parse());
  }

  /**
   * Returns a path to the Delphi BDS folder, as specified in settings
   *
   * @return Path to the BDS folder
   */
  public Path bdsPath() {
    String path =
        settings
            .get(DelphiPlugin.BDS_PATH_KEY)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Property '" + DelphiPlugin.BDS_PATH_KEY + "' must be supplied."));

    return Path.of(path);
  }

  /**
   * Returns a path to the Delphi standard library, based on the BDS path specified in settings.
   *
   * @return Path to standard library
   */
  public Path standardLibraryPath() {
    return bdsPath().resolve("source");
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
   * Gets the search directories specified in settings and project files
   *
   * @return List of search path directories
   */
  public List<Path> getSearchDirectories() {
    indexProjects();
    return searchDirectories;
  }

  /**
   * Gets the debug source directories specified in project files
   *
   * @return List of debug source directorie
   */
  public List<Path> getDebugSourceDirectories() {
    indexProjects();
    return debugSourceDirectories;
  }

  /**
   * Gets the set of conditional defines specified in settings and project files
   *
   * @return set of conditional defines
   */
  public Set<String> getConditionalDefines() {
    indexProjects();
    return conditionalDefines;
  }

  /**
   * Gets the set of unit scope names specified in settings and project files
   *
   * @return set of unit scope names
   */
  public Set<String> getUnitScopeNames() {
    indexProjects();
    return unitScopeNames;
  }

  /**
   * Gets the map of unit aliases specified in settings and project files
   *
   * @return map of unit aliases
   */
  public Map<String, String> getUnitAliases() {
    indexProjects();
    return unitAliases;
  }

  public Iterable<InputFile> mainFiles() {
    FilePredicates p = fs.predicates();
    return fs.inputFiles(p.and(p.hasLanguage(DelphiLanguage.KEY), p.hasType(InputFile.Type.MAIN)));
  }

  public boolean shouldExecuteOnProject() {
    return fs.hasFiles(fs.predicates().hasLanguage(DelphiLanguage.KEY));
  }

  public InputFile getFile(String path) {
    return fs.inputFile(fs.predicates().hasPath(path));
  }

  public InputFile getFile(File file) {
    return getFile(file.getPath());
  }

  public InputFile getFileFromBasename(String basename) {
    return fs.inputFile(fs.predicates().hasFilename(basename));
  }

  public String encoding() {
    return fs != null ? fs.encoding().name() : Charset.defaultCharset().name();
  }

  public String testSuiteType() {
    return settings.get(DelphiPlugin.TEST_SUITE_TYPE_KEY).orElse("");
  }
}
