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
import au.com.integradev.delphi.msbuild.DelphiMSBuildParser.Result;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

public final class ProjectProperties {
  private final Map<String, String> propertyMap;

  private ProjectProperties(Map<String, String> propertyMap) {
    this.propertyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.propertyMap.putAll(propertyMap);
  }

  public static ProjectProperties create(
      EnvironmentVariableProvider environmentVariableProvider, Path environmentProj) {
    if (environmentProj != null && Files.exists(environmentProj)) {
      var parser = new DelphiMSBuildParser(environmentProj, environmentVariableProvider, null);
      Result result = parser.parse();
      return result.getProperties();
    } else {
      return new ProjectProperties(environmentVariableProvider.getenv());
    }
  }

  public ProjectProperties copy() {
    return new ProjectProperties(propertyMap);
  }

  public String get(String name) {
    return propertyMap.get(name);
  }

  public void set(String name, String value) {
    propertyMap.put(name, value);
  }

  public StringSubstitutor substitutor() {
    StringLookup lookup = new PropertyMapLookup(propertyMap);
    return new StringSubstitutor(lookup)
        .setVariablePrefix("$(")
        .setVariableSuffix(")")
        .setEscapeChar(Character.MIN_VALUE)
        .setValueDelimiterMatcher(null)
        .setDisableSubstitutionInValues(true);
  }

  private static class PropertyMapLookup implements StringLookup {
    private final Map<String, String> valueMap;

    PropertyMapLookup(Map<String, String> valueMap) {
      this.valueMap = valueMap;
    }

    @Override
    public String lookup(String key) {
      return valueMap.getOrDefault(key, "");
    }
  }
}
