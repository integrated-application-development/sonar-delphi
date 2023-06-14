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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.plugins.communitydelphi.api.FatalAnalysisError;

class DelphiMasterExecutorTest {
  private static final File BASE_DIR =
      DelphiUtils.getResource("/au/com/integradev/delphi/grammar/");
  private static final File TEST_FILE = DelphiUtils.getResource(BASE_DIR + "GrammarTest.pas");

  private static DelphiInputFile createTestInputFile() {
    try {
      return DelphiInputFile.from(
          TestInputFileBuilder.create("moduleKey", BASE_DIR, TEST_FILE)
              .setContents(FileUtils.readFileToString(TEST_FILE, UTF_8.name()))
              .setLanguage(DelphiLanguage.KEY)
              .setType(InputFile.Type.MAIN)
              .build(),
          mockConfig());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  void testRegularExceptionShouldNotAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);
    DelphiInputFile testInputFile = createTestInputFile();

    doThrow(new RuntimeException("Test")).when(brokenExecutor).execute(context, testInputFile);
    executor.execute(context, testInputFile);

    verify(workingExecutor, times(1)).execute(context, testInputFile);
  }

  @Test
  void testFatalExecutorErrorShouldAbortExecution() {
    ExecutorContext context = mock(ExecutorContext.class);
    Executor brokenExecutor = mock(Executor.class);
    Executor workingExecutor = mock(Executor.class);
    DelphiMasterExecutor executor = new DelphiMasterExecutor(brokenExecutor, workingExecutor);
    DelphiInputFile testInputFile = createTestInputFile();

    doThrow(new FatalAnalysisError("Test", new RuntimeException("This was a test.")))
        .when(brokenExecutor)
        .execute(context, testInputFile);

    assertThatThrownBy(() -> executor.execute(context, testInputFile))
        .isInstanceOf(FatalAnalysisError.class);

    verify(workingExecutor, never()).execute(context, testInputFile);
  }

  @Test
  void testDependenciesShouldBeExecutedInOrder() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    Executor executor = mock(DelphiChecksExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);
    masterExecutor.execute(mock(Context.class), createTestInputFile());

    InOrder inOrder = inOrder(dependency, executor);
    inOrder.verify(dependency).execute(any(), any());
    inOrder.verify(executor).execute(any(), any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void testUnresolvedDependencyShouldSkipExecutor() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    Executor executor = mock(DelphiChecksExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor);
    masterExecutor.execute(mock(Context.class), createTestInputFile());

    verify(executor, never()).execute(any(), any());
  }

  @Test
  void testFailedDependencyExecutionShouldSkipExecutor() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    doThrow(new RuntimeException("Test")).when(dependency).execute(any(), any());

    Executor executor = mock(DelphiChecksExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);
    masterExecutor.execute(mock(Context.class), createTestInputFile());

    verify(executor, never()).execute(any(), any());
  }

  @Test
  void testFatalErrorInDependencyExecutionShouldAbortExecution() {
    Executor dependency = mock(DelphiSymbolTableExecutor.class);
    doThrow(new FatalAnalysisError("Test", new RuntimeException("Test")))
        .when(dependency)
        .execute(any(), any());

    Executor executor = mock(DelphiChecksExecutor.class);
    when(executor.dependencies()).thenReturn(Set.of(dependency.getClass()));

    DelphiMasterExecutor masterExecutor = new DelphiMasterExecutor(executor, dependency);

    assertThatThrownBy(() -> masterExecutor.execute(mock(Context.class), createTestInputFile()))
        .isInstanceOf(FatalAnalysisError.class);

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
