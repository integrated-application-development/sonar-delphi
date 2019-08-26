package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.plugins.delphi.DelphiFile;

@ScannerSide
public interface Executor {
  default void setup() {}

  void execute(SensorContext context, DelphiFile delphiFile);

  default void complete() {}
}
