package org.sonar.plugins.delphi.executor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;

public class DelphiMasterExecutor implements Executor {
  private static final Logger LOG = Loggers.get(DelphiMasterExecutor.class);
  private final List<Executor> executors;

  /**
   * If you create a new executor, add it to this list. Unspecified executors will execute first.
   */
  private static final List<Class<? extends Executor>> EXECUTOR_ORDER =
      List.of(
          DelphiCpdExecutor.class,
          DelphiHighlightExecutor.class,
          DelphiMetricsExecutor.class,
          DelphiSymbolTableExecutor.class,
          DelphiPmdExecutor.class);

  public DelphiMasterExecutor(Executor... allExecutors) {
    executors = Arrays.asList(allExecutors);
    executors.sort(Comparator.comparingInt(a -> EXECUTOR_ORDER.indexOf(a.getClass())));
  }

  @Override
  public void setup() {
    for (Executor executor : executors) {
      executor.setup();
    }
  }

  @Override
  public void execute(Context context, DelphiInputFile file) {
    for (Executor executor : executors) {
      try {
        executor.execute(context, file);
      } catch (FatalExecutorError e) {
        throw e;
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
