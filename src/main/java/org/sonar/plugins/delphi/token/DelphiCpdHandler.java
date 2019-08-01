package org.sonar.plugins.delphi.token;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;

public class DelphiCpdHandler implements DelphiTokenHandler {
  private NewCpdTokens cpdTokens;

  @Override
  public void onFile(SensorContext context, InputFile inputFile) {
    cpdTokens = context.newCpdTokens().onFile(inputFile);
  }

  @Override
  public void handleToken(DelphiToken token) {
    if (token.isWhitespace() || token.isComment()) {
      return;
    }

    cpdTokens.addToken(
        token.getStartLine(),
        token.getStartColumn(),
        token.getEndLine(),
        token.getEndColumn(),
        token.getImage());
  }

  @Override
  public void saveResults() {
    cpdTokens.save();
  }
}
