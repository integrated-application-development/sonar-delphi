package org.sonar.plugins.delphi.executor;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiFile;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;

public abstract class DelphiTokenExecutor implements Executor {
  private static final Logger LOG = Loggers.get(DelphiTokenExecutor.class);

  @Override
  public void execute(SensorContext context, DelphiFile delphiFile) {
    try {
      onFile(context, delphiFile);
      for (DelphiToken token : delphiFile.getTokens()) {
        handleToken(token);
      }
    } catch (IllegalStateException e) {
      LOG.error("Tokenization failure on: {}", delphiFile.getSourceCodeFile().getAbsolutePath(), e);
      return;
    }
    save();
  }

  protected abstract void onFile(SensorContext context, DelphiFile file);

  protected abstract void handleToken(DelphiToken token);

  protected abstract void save();
}
