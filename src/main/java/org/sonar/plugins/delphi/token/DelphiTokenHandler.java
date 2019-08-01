package org.sonar.plugins.delphi.token;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public interface DelphiTokenHandler {
  void onFile(SensorContext context, InputFile inputFile);

  void handleToken(DelphiToken delphiToken);

  void saveResults();
}
