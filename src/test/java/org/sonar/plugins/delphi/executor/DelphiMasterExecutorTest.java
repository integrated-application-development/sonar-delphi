package org.sonar.plugins.delphi.executor;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.delphi.DelphiFile;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

public class DelphiMasterExecutorTest {
  private static final String TEST_FILE =
      "/org/sonar/plugins/delphi/grammar/SuperfluousSemicolons.pas";

  @Test
  public void testExceptionsShouldNotAbortExecution() {
    SensorContext context = mock(SensorContext.class);
    DelphiFile file = DelphiTestFileBuilder.fromResource(TEST_FILE).delphiFile();

    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);

    doThrow(new RuntimeException("Test")).when(brokenExecutor).execute(context, file);
    executor.execute(context, file);

    verify(workingExecutor, times(1)).execute(context, file);
  }
}
