package org.sonar.plugins.delphi.enviroment;

import java.util.Map;
import org.sonar.api.scanner.ScannerSide;

@ScannerSide
public interface EnvironmentVariableProvider {
  Map<String, String> getenv();

  String getenv(String name);
}
