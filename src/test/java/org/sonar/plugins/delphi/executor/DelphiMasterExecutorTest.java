package org.sonar.plugins.delphi.executor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Test;
import org.mockito.InOrder;
import org.sonar.plugins.delphi.executor.Executor.Context;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

public class DelphiMasterExecutorTest {
  private static final DelphiInputFile TEST_FILE =
      DelphiTestFileBuilder.fromResource("/org/sonar/plugins/delphi/grammar/GrammarTest.pas")
          .delphiFile();

  @Test
  public void testRegularExceptionShouldNotAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);

    doThrow(new RuntimeException("Test")).when(brokenExecutor).execute(context, TEST_FILE);
    executor.execute(context, TEST_FILE);

    verify(workingExecutor, times(1)).execute(context, TEST_FILE);
  }

  @Test
  public void testFatalExecutorErrorShouldAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);

    doThrow(new FatalExecutorError("Test", new RuntimeException("This was a test.")))
        .when(brokenExecutor)
        .execute(context, TEST_FILE);

    assertThatThrownBy(() -> executor.execute(context, TEST_FILE))
        .isInstanceOf(FatalExecutorError.class);

    verify(workingExecutor, never()).execute(context, TEST_FILE);
  }

  @Test
  public void testDependenciesShouldBeExecutedInOrder() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);
    masterExecutor.execute(mock(Context.class), TEST_FILE);

    InOrder inOrder = inOrder(dependency, executor);
    inOrder.verify(dependency).execute(any(), any());
    inOrder.verify(executor).execute(any(), any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testUnresolvedDependencyShouldSkipExecutor() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor);
    masterExecutor.execute(mock(Context.class), TEST_FILE);

    verify(executor, never()).execute(any(), any());
  }

  @Test
  public void testFailedDependencyExecutionShouldSkipExecutor() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    doThrow(new RuntimeException("Test")).when(dependency).execute(any(), any());

    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);
    masterExecutor.execute(mock(Context.class), TEST_FILE);

    verify(executor, never()).execute(any(), any());
  }

  @Test
  public void testFatalErrorInDependencyExecutionShouldAbortExecution() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    doThrow(new FatalExecutorError("Test", new RuntimeException("Test")))
        .when(dependency)
        .execute(any(), any());

    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);

    assertThatThrownBy(() -> masterExecutor.execute(mock(Context.class), TEST_FILE))
        .isInstanceOf(FatalExecutorError.class);

    verify(executor, never()).execute(any(), any());
  }
}
