/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.executor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.core.DelphiLanguage;
import au.com.integradev.delphi.executor.Executor.Context;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

class DelphiMasterExecutorTest {
  private static final File BASE_DIR =
      DelphiUtils.getResource("/au/com/integradev/delphi/grammar/");
  private static final File TEST_FILE = DelphiUtils.getResource(BASE_DIR + "GrammarTest.pas");
  private static final DelphiInputFile TEST_INPUT_FILE =
      DelphiInputFile.from(
          TestInputFileBuilder.create("moduleKey", BASE_DIR, TEST_FILE)
              .setContents(DelphiUtils.readFileContent(TEST_FILE, UTF_8.name()))
              .setLanguage(DelphiLanguage.KEY)
              .setType(InputFile.Type.MAIN)
              .build(),
          mockConfig());

  @Test
  void testRegularExceptionShouldNotAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);

    doThrow(new RuntimeException("Test")).when(brokenExecutor).execute(context, TEST_INPUT_FILE);
    executor.execute(context, TEST_INPUT_FILE);

    verify(workingExecutor, times(1)).execute(context, TEST_INPUT_FILE);
  }

  @Test
  void testFatalExecutorErrorShouldAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);

    doThrow(new FatalExecutorError("Test", new RuntimeException("This was a test.")))
        .when(brokenExecutor)
        .execute(context, TEST_INPUT_FILE);

    assertThatThrownBy(() -> executor.execute(context, TEST_INPUT_FILE))
        .isInstanceOf(FatalExecutorError.class);

    verify(workingExecutor, never()).execute(context, TEST_INPUT_FILE);
  }

  @Test
  void testDependenciesShouldBeExecutedInOrder() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);
    masterExecutor.execute(mock(Context.class), TEST_INPUT_FILE);

    InOrder inOrder = inOrder(dependency, executor);
    inOrder.verify(dependency).execute(any(), any());
    inOrder.verify(executor).execute(any(), any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void testUnresolvedDependencyShouldSkipExecutor() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor);
    masterExecutor.execute(mock(Context.class), TEST_INPUT_FILE);

    verify(executor, never()).execute(any(), any());
  }

  @Test
  void testFailedDependencyExecutionShouldSkipExecutor() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    doThrow(new RuntimeException("Test")).when(dependency).execute(any(), any());

    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);
    masterExecutor.execute(mock(Context.class), TEST_INPUT_FILE);

    verify(executor, never()).execute(any(), any());
  }

  @Test
  void testFatalErrorInDependencyExecutionShouldAbortExecution() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    doThrow(new FatalExecutorError("Test", new RuntimeException("Test")))
        .when(dependency)
        .execute(any(), any());

    Executor executor = mock(DelphiPmdExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);

    assertThatThrownBy(() -> masterExecutor.execute(mock(Context.class), TEST_INPUT_FILE))
        .isInstanceOf(FatalExecutorError.class);

    verify(executor, never()).execute(any(), any());
  }

  private static DelphiFileConfig mockConfig() {
    TypeFactory typeFactory =
        new TypeFactory(
            DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT, DelphiProperties.COMPILER_VERSION_DEFAULT);
    DelphiFileConfig mock = mock(DelphiFileConfig.class);
    when(mock.getEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    when(mock.getTypeFactory()).thenReturn(typeFactory);
    when(mock.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(mock.getDefinitions()).thenReturn(Collections.emptySet());
    return mock;
  }
}