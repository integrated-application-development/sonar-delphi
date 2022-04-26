package org.sonar.plugins.delphi.msbuild;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.msbuild.DelphiMSBuildParser.Result;

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
