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

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DelphiProjectParser {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiProjectParser.class);

  private final Path dproj;
  private final EnvironmentVariableProvider environmentVariableProvider;
  private final Path environmentProj;

  public DelphiProjectParser(
      Path dproj, EnvironmentVariableProvider environmentVariableProvider, Path environmentProj) {
    this.dproj = dproj;
    this.environmentVariableProvider = environmentVariableProvider;
    this.environmentProj = environmentProj;
  }

  public DelphiProject parse() {
    var parser = new DelphiMSBuildParser(dproj, environmentVariableProvider, environmentProj);
    DelphiMSBuildParser.Result result = parser.parse();

    Path dprojDirectory = dproj.getParent();

    DelphiProjectImpl project = new DelphiProjectImpl();
    project.setDefinitions(createDefinitions(result.getProperties()));
    project.setUnitScopeNames(createUnitScopeNames(result.getProperties()));
    project.setSearchDirectories(createSearchDirectories(dprojDirectory, result.getProperties()));
    project.setDebugSourceDirectories(createDebugSourceDirectories(result.getProperties()));
    project.setUnitAliases(createUnitAliases(result.getProperties()));
    project.setSourceFiles(result.getSourceFiles());

    return project;
  }

  private static Set<String> createDefinitions(ProjectProperties properties) {
    return Set.copyOf(propertyList(properties.get("DCC_Define")));
  }

  private static Set<String> createUnitScopeNames(ProjectProperties properties) {
    return Set.copyOf(propertyList(properties.get("DCC_Namespace")));
  }

  private List<Path> createSearchDirectories(Path dprojDirectory, ProjectProperties properties) {
    List<Path> explicitPaths = createPathList(properties, "DCC_UnitSearchPath");

    List<Path> allPaths = new ArrayList<>(explicitPaths.size() + 1);
    allPaths.add(dprojDirectory);
    allPaths.addAll(explicitPaths);
    return Collections.unmodifiableList(allPaths);
  }

  private List<Path> createDebugSourceDirectories(ProjectProperties properties) {
    return createPathList(properties, "Debugger_DebugSourcePath");
  }

  private List<Path> createPathList(ProjectProperties properties, String propertyName) {
    return propertyList(properties.get(propertyName)).stream()
        .map(DelphiUtils::normalizeFileName)
        .map(this::resolvePath)
        .filter(
            directory -> {
              if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                LOG.warn("Invalid {} directory: {}", propertyName, directory);
                return false;
              }
              return true;
            })
        .collect(Collectors.toUnmodifiableList());
  }

  private static Map<String, String> createUnitAliases(ProjectProperties properties) {
    Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    propertyList(properties.get("DCC_UnitAlias"))
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

  private Path resolvePath(String pathString) {
    return DelphiUtils.resolvePathFromBaseDir(evaluationDirectory(), Path.of(pathString));
  }

  private Path evaluationDirectory() {
    return dproj.getParent();
  }

  private static class DelphiProjectImpl implements DelphiProject {
    private Set<String> definitions = Collections.emptySet();
    private Set<String> unitScopeNames = Collections.emptySet();
    private List<Path> sourceFiles = Collections.emptyList();
    private List<Path> searchDirectories = Collections.emptyList();
    private List<Path> debugSourceDirectories = Collections.emptyList();
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
    public Map<String, String> getUnitAliases() {
      return unitAliases;
    }
  }
}
