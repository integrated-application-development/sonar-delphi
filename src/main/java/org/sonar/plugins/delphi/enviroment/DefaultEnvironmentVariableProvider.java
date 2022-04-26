package org.sonar.plugins.delphi.enviroment;

import java.util.Map;

public class DefaultEnvironmentVariableProvider implements EnvironmentVariableProvider {
  @Override
  public Map<String, String> getenv() {
    return System.getenv();
  }

  @Override
  public String getenv(String name) {
    return System.getenv(name);
  }
}
