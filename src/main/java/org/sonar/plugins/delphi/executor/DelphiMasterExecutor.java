package org.sonar.plugins.delphi.executor;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiFile;

public class DelphiMasterExecutor implements Executor {
  private static final Logger LOG = Loggers.get(DelphiMasterExecutor.class);
  private List<Executor> executors;

  public DelphiMasterExecutor(Executor... allExecutors) {
    executors = Arrays.asList(allExecutors);
  }

  @Override
  public void setup() {
    for (Executor executor : executors) {
      executor.setup();
    }
  }

  @Override
  public void execute(SensorContext context, DelphiFile file) {
    for (Executor executor : executors) {
      try {
        executor.execute(context, file);
      } catch (Exception e) {
        String executorName = executor.getClass().getSimpleName();
        String fileName = file.getSourceCodeFile().getName();
        LOG.error("Error occurred while running {} on file: {}", executorName, fileName, e);
        LOG.info("Continuing with next executor.");
      }
    }
  }

  @Override
  public void complete() {
    for (Executor executor : executors) {
      executor.complete();
    }
  }
}
