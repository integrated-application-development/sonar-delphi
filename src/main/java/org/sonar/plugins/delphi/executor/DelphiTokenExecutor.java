package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public abstract class DelphiTokenExecutor implements Executor {

  @Override
  public void execute(Context context, DelphiInputFile delphiFile) {
    onFile(context.sensorContext(), delphiFile);
    for (DelphiToken token : delphiFile.getTokens()) {
      handleToken(token);
    }
    save();
  }

  protected abstract void onFile(SensorContext context, DelphiInputFile file);

  protected abstract void handleToken(DelphiToken token);

  protected abstract void save();
}
