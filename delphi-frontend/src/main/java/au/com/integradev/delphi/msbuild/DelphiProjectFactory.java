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

import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DelphiProjectFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiProjectFactory.class);

  public DelphiProject createProject(MSBuildState state) {
    var sourceFiles = DelphiMSBuildUtils.getSourceFiles(state);

    Path dproj = state.getThisFilePath();
    Path projectDirectory = dproj.getParent();

    DelphiProjectImpl project = new DelphiProjectImpl();
    project.setDefinitions(createDefinitions(state));
    project.setUnitScopeNames(createUnitScopeNames(state));
    project.setSearchDirectories(
        createSearchDirectories(projectDirectory, state, projectDirectory));
    project.setDebugSourceDirectories(createDebugSourceDirectories(state, projectDirectory));
    project.setLibraryPath(createLibraryPathDirectories(state, projectDirectory));
    project.setBrowsingPath(createBrowsingPathDirectories(state, projectDirectory));
    project.setUnitAliases(createUnitAliases(state));
    project.setSourceFiles(sourceFiles);

    return project;
  }

  private static Set<String> createDefinitions(MSBuildState state) {
    return Set.copyOf(propertyList(state.getProperty("DCC_Define")));
  }

  private static Set<String> createUnitScopeNames(MSBuildState state) {
    return Set.copyOf(propertyList(state.getProperty("DCC_Namespace")));
  }

  private List<Path> createSearchDirectories(
      Path dprojDirectory, MSBuildState state, Path baseDir) {
    List<Path> result = new ArrayList<>();

    result.add(dprojDirectory);
    result.addAll(createPathList(state, "DCC_UnitSearchPath", baseDir));

    return Collections.unmodifiableList(result);
  }

  private List<Path> createDebugSourceDirectories(MSBuildState state, Path baseDir) {
    return createPathList(state, "Debugger_DebugSourcePath", baseDir);
  }

  private List<Path> createLibraryPathDirectories(MSBuildState state, Path baseDir) {
    List<Path> result = new ArrayList<>();

    result.addAll(createPathList(state, "DelphiLibraryPath", baseDir, false));
    result.addAll(createPathList(state, "DelphiTranslatedLibraryPath", baseDir, false));

    return Collections.unmodifiableList(result);
  }

  private List<Path> createBrowsingPathDirectories(MSBuildState state, Path baseDir) {
    return createPathList(state, "DelphiBrowsingPath", baseDir, false);
  }

  private List<Path> createPathList(MSBuildState state, String propertyName, Path baseDir) {
    return createPathList(state, propertyName, baseDir, true);
  }

  private List<Path> createPathList(
      MSBuildState state, String propertyName, Path baseDir, boolean emitWarnings) {
    List<Path> result = new ArrayList<>();
    propertyList(state.getProperty(propertyName))
        .forEach(
            pathString -> {
              Path path = resolveDirectory(pathString, baseDir);
              if (path != null) {
                result.add(path);
              } else if (emitWarnings) {
                LOG.warn("Invalid {} directory: {}", propertyName, pathString);
              }
            });
    return Collections.unmodifiableList(result);
  }

  @Nullable
  private Path resolveDirectory(String pathString, Path baseDir) {
    try {
      pathString = DelphiUtils.normalizeFileName(pathString);
      Path path = DelphiUtils.resolvePathFromBaseDir(baseDir, Path.of(pathString));
      if (Files.isDirectory(path)) {
        return path;
      }
    } catch (InvalidPathException e) {
      LOG.debug("Invalid path string", e);
    }
    return null;
  }

  private static Map<String, String> createUnitAliases(MSBuildState state) {
    Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    propertyList(state.getProperty("DCC_UnitAlias"))
        .forEach(
            item -> {
              if (StringUtils.countMatches(item, '=') != 1) {
                LOG.warn("Invalid unit alias syntax: '{}'", item);
                return;
              }
              int equalIndex = item.indexOf('=');
              String unitAlias = item.substring(0, equalIndex);
              String unitName = item.substring(equalIndex + 1);
              result.put(unitAlias, unitName);
            });
    return Collections.unmodifiableMap(result);
  }

  private static List<String> propertyList(String value) {
    if (value == null) {
      return Collections.emptyList();
    }
    return Splitter.on(';').omitEmptyStrings().splitToList(value);
  }

  private static class DelphiProjectImpl implements DelphiProject {
    private Set<String> definitions = Collections.emptySet();
    private Set<String> unitScopeNames = Collections.emptySet();
    private List<Path> sourceFiles = Collections.emptyList();
    private List<Path> searchDirectories = Collections.emptyList();
    private List<Path> debugSourceDirectories = Collections.emptyList();
    private List<Path> libraryPathDirectories = Collections.emptyList();
    private List<Path> browsingPathDirectories = Collections.emptyList();
    private Map<String, String> unitAliases = Collections.emptyMap();

    private void setDefinitions(Set<String> definitions) {
      this.definitions = ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, definitions);
    }

    private void setUnitScopeNames(Set<String> unitScopeNames) {
      this.unitScopeNames =
          ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, unitScopeNames);
    }

    private void setSourceFiles(List<Path> sourceFiles) {
      this.sourceFiles = List.copyOf(sourceFiles);
    }

    private void setSearchDirectories(List<Path> searchDirectories) {
      this.searchDirectories = List.copyOf(searchDirectories);
    }

    private void setDebugSourceDirectories(List<Path> debugSourceDirectories) {
      this.debugSourceDirectories = List.copyOf(debugSourceDirectories);
    }

    private void setLibraryPath(List<Path> libraryPathDirectories) {
      this.libraryPathDirectories = List.copyOf(libraryPathDirectories);
    }

    private void setBrowsingPath(List<Path> browsingPathDirectories) {
      this.browsingPathDirectories = List.copyOf(browsingPathDirectories);
    }

    private void setUnitAliases(Map<String, String> unitAliases) {
      this.unitAliases = ImmutableSortedMap.copyOf(unitAliases, String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public Set<String> getConditionalDefines() {
      return definitions;
    }

    @Override
    public Set<String> getUnitScopeNames() {
      return unitScopeNames;
    }

    @Override
    public List<Path> getSourceFiles() {
      return sourceFiles;
    }

    @Override
    public List<Path> getSearchDirectories() {
      return searchDirectories;
    }

    @Override
    public List<Path> getDebugSourceDirectories() {
      return debugSourceDirectories;
    }

    @Override
    public List<Path> getLibraryPathDirectories() {
      return libraryPathDirectories;
    }

    @Override
    public List<Path> getBrowsingPathDirectories() {
      return browsingPathDirectories;
    }

    @Override
    public Map<String, String> getUnitAliases() {
      return unitAliases;
    }
  }
}
