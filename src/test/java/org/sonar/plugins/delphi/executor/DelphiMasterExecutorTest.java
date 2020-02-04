package org.sonar.plugins.delphi.executor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

public class DelphiMasterExecutorTest {
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/grammar/GrammarTest.pas";

  @Test
  public void testRegularExceptionShouldNotAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    DelphiInputFile file = DelphiTestFileBuilder.fromResource(TEST_FILE).delphiFile();

    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);

    doThrow(new RuntimeException("Test")).when(brokenExecutor).execute(context, file);
    executor.execute(context, file);

    verify(workingExecutor, times(1)).execute(context, file);
  }

  @Test
  public void testFatalExecutorErrorShouldAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    DelphiInputFile file = DelphiTestFileBuilder.fromResource(TEST_FILE).delphiFile();

    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);

    doThrow(new FatalExecutorError("Test", new RuntimeException("This was a test.")))
        .when(brokenExecutor)
        .execute(context, file);

    assertThatThrownBy(() -> executor.execute(context, file))
        .isInstanceOf(FatalExecutorError.class);

    verify(workingExecutor, never()).execute(context, file);
  }
}
