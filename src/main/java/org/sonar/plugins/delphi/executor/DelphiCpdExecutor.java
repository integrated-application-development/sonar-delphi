package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public class DelphiCpdExecutor extends DelphiTokenExecutor {
  private NewCpdTokens cpdTokens;

  @Override
  public void onFile(SensorContext context, DelphiInputFile delphiFile) {
    cpdTokens = context.newCpdTokens().onFile(delphiFile.getInputFile());
  }

  @Override
  public void handleToken(DelphiToken token) {
    if (token.isWhitespace() || token.isComment()) {
      return;
    }

    cpdTokens.addToken(
        token.getBeginLine(),
        token.getBeginColumn(),
        token.getEndLine(),
        token.getEndColumn(),
        token.getNormalizedImage());
  }

  @Override
  public void save() {
    cpdTokens.save();
  }
}
