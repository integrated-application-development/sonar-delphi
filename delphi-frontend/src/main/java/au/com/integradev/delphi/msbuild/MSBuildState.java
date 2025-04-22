/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import au.com.integradev.delphi.msbuild.expression.MSBuildWellKnownPropertyHelper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class MSBuildState {
  private final Map<String, String> propertyMap;
  private final Map<String, List<MSBuildItem>> itemMap;
  private final Path thisFilePath;
  private final Path projectPath;
  private final MSBuildWellKnownPropertyHelper wellKnownProperties;

  public MSBuildState(
      Path thisFilePath,
      Path projectPath,
      Map<String, String> propertyMap,
      Map<String, List<MSBuildItem>> itemMap) {
    this.thisFilePath = thisFilePath;
    this.projectPath = projectPath;

    this.propertyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.propertyMap.putAll(propertyMap);

    this.itemMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.itemMap.putAll(itemMap);

    if (thisFilePath != null && projectPath != null) {
      this.wellKnownProperties =
          new MSBuildWellKnownPropertyHelper(thisFilePath.toString(), projectPath.toString());
    } else {
      // If this state isn't attached to any concrete project file, we can't calculate props
      this.wellKnownProperties = null;
    }
  }

  public MSBuildState(
      Path thisFilePath,
      Path projectPath,
      EnvironmentVariableProvider environmentVariableProvider) {
    this(thisFilePath, projectPath, environmentVariableProvider.getenv(), Collections.emptyMap());
  }

  public Path getThisFilePath() {
    return thisFilePath;
  }

  public MSBuildState deriveState(Path thisFilePath) {
    return new MSBuildState(thisFilePath, projectPath, propertyMap, itemMap);
  }

  public void absorbState(MSBuildState other) {
    propertyMap.putAll(other.propertyMap);
    itemMap.putAll(other.itemMap);
  }

  public String getProperty(String name) {
    var value = propertyMap.get(name);
    if (value == null && wellKnownProperties != null) {
      value = wellKnownProperties.getProperty(name);
    }
    return value == null ? "" : value;
  }

  public void setProperty(String name, String value) {
    propertyMap.put(name, value);
  }

  public void addItem(String name, MSBuildItem value) {
    if (itemMap.containsKey(name)) {
      itemMap.get(name).add(value);
    } else {
      itemMap.put(name, new ArrayList<>(Collections.singletonList(value)));
    }
  }

  public void addItems(String name, List<MSBuildItem> value) {
    if (itemMap.containsKey(name)) {
      itemMap.get(name).addAll(value);
    } else {
      itemMap.put(name, new ArrayList<>(value));
    }
  }

  public List<MSBuildItem> getItems(String name) {
    return itemMap.getOrDefault(name, Collections.emptyList());
  }

  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(propertyMap);
  }
}
