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

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.PredefinedConditionals;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.enviroment.EnvironmentProjVariableProvider;
import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.utils.CharsetUtils;
import au.com.integradev.delphi.utils.CharsetUtils.UnsupportedCodePageException;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.plugins.communitydelphi.api.type.CodePages;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public class DelphiProjectHelper {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiProjectHelper.class);
  private static final String SOURCE_ENCODING_KEY = "sonar.sourceEncoding";

  private final Configuration settings;
  private final FileSystem fs;
  private final EnvironmentVariableProvider environmentVariableProvider;
  private final Supplier<EnvironmentVariableProvider> effectiveEnvironmentVariableProvider;
  private final List<DelphiProject> projects;
  private final Toolchain toolchain;
  private final CompilerVersion compilerVersion;
  private final List<Path> searchDirectories;
  private final List<Path> debugSourceDirectories;
  private final List<Path> libraryPathDirectories;
  private final List<Path> browsingPathDirectories;
  private final List<Path> referencedFiles;
  private final Set<String> conditionalDefines;
  private final Set<String> unitScopeNames;
  private final Map<String, String> unitAliases;
  private Charset ansiCharset;
  private boolean indexedProjects;

  /**
   * Constructor
   *
   * @param settings Project settings
   * @param fs Sonar FileSystem
   */
  public DelphiProjectHelper(
      Configuration settings,
      FileSystem fs,
      EnvironmentVariableProvider environmentVariableProvider) {
    this.settings = settings;
    this.fs = fs;
    this.environmentVariableProvider = environmentVariableProvider;
    this.projects = new ArrayList<>();
    this.toolchain = getToolchainFromSettings();
    this.compilerVersion = getCompilerVersionFromSettings();
    this.searchDirectories = getSearchDirectoriesFromSettings();
    this.debugSourceDirectories = new ArrayList<>();
    this.libraryPathDirectories = new ArrayList<>();
    this.browsingPathDirectories = new ArrayList<>();
    this.referencedFiles = new ArrayList<>();
    this.conditionalDefines = getPredefinedConditionalDefines();
    this.unitScopeNames = getSetFromSettings(DelphiProperties.UNIT_SCOPE_NAMES_KEY);
    this.unitAliases = getUnitAliasesFromSettings();
    this.ansiCharset = null;
    this.effectiveEnvironmentVariableProvider =
        () ->
            new EnvironmentProjVariableProvider(environmentProjPath(), environmentVariableProvider);
  }

  private Set<String> getSetFromSettings(String key) {
    return Arrays.stream(nullToEmpty(settings.getStringArray(key))).collect(Collectors.toSet());
  }

  private Toolchain getToolchainFromSettings() {
    return EnumUtils.getEnumIgnoreCase(
        Toolchain.class,
        settings.get(DelphiProperties.COMPILER_TOOLCHAIN_KEY).orElse(null),
        DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT);
  }

  private CompilerVersion getCompilerVersionFromSettings() {
    String versionSymbol =
        settings
            .get(DelphiProperties.COMPILER_VERSION_KEY)
            .orElse(DelphiProperties.COMPILER_VERSION_DEFAULT.symbol());

    try {
      return CompilerVersion.fromVersionSymbol(versionSymbol);
    } catch (CompilerVersion.FormatException e) {
      LOG.warn(
          "Defaulting to compiler version \"{}\" because the provided one was invalid: \"{}\"",
          DelphiProperties.COMPILER_VERSION_DEFAULT,
          versionSymbol);
      LOG.debug("Exception: ", e);
      return DelphiProperties.COMPILER_VERSION_DEFAULT;
    }
  }

  private List<Path> getSearchDirectoriesFromSettings() {
    String[] searchDirectoriesSetting =
        nullToEmpty(settings.getStringArray(DelphiProperties.SEARCH_PATH_KEY));
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
    String[] aliases = nullToEmpty(settings.getStringArray(DelphiProperties.UNIT_ALIASES_KEY));
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

  private Integer getCodePageFromSettings() {
    String codePage = settings.get(DelphiProperties.CODE_PAGE_KEY).orElse(null);
    if (StringUtils.isBlank(codePage)) {
      return null;
    }

    try {
      return Integer.parseInt(codePage.trim());
    } catch (NumberFormatException e) {
      LOG.warn("Ignoring invalid {} value: {}", DelphiProperties.CODE_PAGE_KEY, codePage);
      return null;
    }
  }

  private Charset resolveAnsiCharset() {
    Integer configuredCodePage = getCodePageFromSettings();
    if (configuredCodePage != null) {
      return charsetForCodePage(configuredCodePage);
    }

    List<Integer> codePages =
        projects.stream()
            .map(DelphiProject::getCodePage)
            .map(codePage -> codePage == null ? CodePages.CP_ACP : codePage)
            .distinct()
            .sorted()
            .collect(Collectors.toUnmodifiableList());

    List<Integer> explicitCodePages =
        codePages.stream()
            .filter(codePage -> !codePage.equals(CodePages.CP_ACP))
            .collect(Collectors.toUnmodifiableList());

    boolean useAcp =
        codePages.isEmpty()
            || codePages.stream().anyMatch(codePage -> codePage.equals(CodePages.CP_ACP));

    boolean conflict = false;
    Charset result = CharsetUtils.nativeCharset();

    if (explicitCodePages.size() > 1) {
      conflict = true;
    } else if (explicitCodePages.size() == 1) {
      Charset charset = charsetForCodePage(explicitCodePages.get(0));
      if (useAcp) {
        conflict = !charset.equals(result);
      } else {
        result = charset;
      }
    }

    if (conflict) {
      LOG.warn(
          "Conflicting DCC_CodePage values found in dproj files: {}. Falling back to the system"
              + " encoding. Set {} to choose the code page used for analysis.",
          codePages,
          DelphiProperties.CODE_PAGE_KEY);
    }

    return result;
  }

  private Charset charsetForCodePage(int codePage) {
    try {
      return CharsetUtils.charsetForCodePage(codePage);
    } catch (UnsupportedCodePageException e) {
      LOG.warn(
          "Ignoring unsupported code page value: {}. Falling back to the system encoding.",
          codePage);
      return CharsetUtils.nativeCharset();
    }
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
      libraryPathDirectories.addAll(project.getLibraryPathDirectories());
      browsingPathDirectories.addAll(project.getBrowsingPathDirectories());
      conditionalDefines.addAll(project.getConditionalDefines());
      referencedFiles.addAll(project.getSourceFiles());
      unitScopeNames.addAll(project.getUnitScopeNames());
      unitAliases.putAll(project.getUnitAliases());
    }

    conditionalDefines.addAll(getSetFromSettings(DelphiProperties.CONDITIONAL_DEFINES_KEY));
    conditionalDefines.removeAll(getSetFromSettings(DelphiProperties.CONDITIONAL_UNDEFINES_KEY));
    ansiCharset = resolveAnsiCharset();

    indexedProjects = true;
  }

  @VisibleForTesting
  Path environmentProjPath() {
    Path bdsPath = installationPath();
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
    var state = new MSBuildParser(dprojFile, effectiveEnvironmentVariableProvider.get()).parse();
    DelphiProject newProject = new DelphiProjectFactory().createProject(state);
    projects.add(newProject);
  }

  private void indexProjectGroup(Path projectGroup) {
    var state = new MSBuildParser(projectGroup, effectiveEnvironmentVariableProvider.get()).parse();
    projects.addAll(
        DelphiMSBuildUtils.getProjects(state, effectiveEnvironmentVariableProvider.get()));
  }

  /**
   * Returns a path to the Delphi installation folder, as specified in settings
   *
   * @return Path to the installation folder
   */
  public Path installationPath() {
    String path =
        settings
            .get(DelphiProperties.INSTALLATION_PATH_KEY)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Property '"
                            + DelphiProperties.INSTALLATION_PATH_KEY
                            + "' must be supplied."));

    return Path.of(path);
  }

  /**
   * Returns a path to the Delphi standard library, based on the installation path specified in
   * settings.
   *
   * @return Path to standard library
   */
  public Path standardLibraryPath() {
    return installationPath().resolve("source");
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
   * @return List of debug source directories
   */
  public List<Path> getDebugSourceDirectories() {
    indexProjects();
    return debugSourceDirectories;
  }

  /**
   * Gets the library path directories specified in project files
   *
   * @return List of library path directories
   */
  public List<Path> getLibraryPathDirectories() {
    indexProjects();
    return libraryPathDirectories;
  }

  /**
   * Gets the browsing path directories specified in project files
   *
   * @return List of browsing path directories
   */
  public List<Path> getBrowsingPathDirectories() {
    indexProjects();
    return browsingPathDirectories;
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

  /**
   * Gets the charset for the ANSI code page specified in settings and project files
   *
   * @return ANSI charset
   */
  public Charset getAnsiCharset() {
    indexProjects();
    return ansiCharset;
  }

  public List<Path> getReferencedFiles() {
    indexProjects();
    return referencedFiles;
  }

  public Iterable<InputFile> inputFiles() {
    FilePredicates p = fs.predicates();
    return fs.inputFiles(p.and(p.hasLanguage(Delphi.KEY)));
  }

  public InputFile getFile(String path) {
    return fs.inputFile(fs.predicates().hasURI(Paths.get(path).toUri()));
  }

  public Charset getCharset() {
    if (fs != null && settings.get(SOURCE_ENCODING_KEY).isPresent()) {
      return fs.encoding();
    }
    return getAnsiCharset();
  }
}
