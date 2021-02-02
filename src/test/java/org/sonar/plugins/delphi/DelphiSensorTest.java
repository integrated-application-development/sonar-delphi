/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.delphi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.executor.ExecutorContext;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.project.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiSensorTest {
  private static final String STANDARD_LIBRARY = "/org/sonar/plugins/delphi/standardLibrary";
  private static final String BASE_PATH = "/org/sonar/plugins/delphi/projects/";
  private static final File BASE_DIR = DelphiUtils.getResource(BASE_PATH);
  private static final String BAD_SYNTAX = BASE_PATH + "BadSyntaxProject/BadSyntax.Pas";
  private static final String GLOBALS = BASE_PATH + "SimpleProject/Globals.Pas";

  private final DelphiMasterExecutor executor = mock(DelphiMasterExecutor.class);
  private final DefaultFileSystem fileSystem = new DefaultFileSystem(BASE_DIR);
  private final SensorContextTester context = SensorContextTester.create(fileSystem.baseDir());
  private final DelphiProjectHelper delphiProjectHelper = mock(DelphiProjectHelper.class);

  private DelphiSensor sensor;

  @BeforeEach
  void setupSensor() {
    sensor = new DelphiSensor(delphiProjectHelper, executor);
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(true);
    when(delphiProjectHelper.getToolchain()).thenReturn(DelphiPlugin.COMPILER_TOOLCHAIN_DEFAULT);
    when(delphiProjectHelper.getCompilerVersion())
        .thenReturn(DelphiPlugin.COMPILER_VERSION_DEFAULT);
  }

  @Test
  void testToString() {
    final String toString = sensor.toString();
    assertThat(toString).isEqualTo("DelphiSensor");
  }

  @Test
  void testDescribe() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(DelphiLanguage.KEY);
    verify(mockDescriptor).name("DelphiSensor");
  }

  @Test
  void testSensorShouldNotRethrowOtherExceptions() {
    setupProject(GLOBALS);

    final RuntimeException expectedException = new RuntimeException();
    willThrow(expectedException)
        .given(executor)
        .execute(any(ExecutorContext.class), any(DelphiInputFile.class));

    assertThatThrownBy(() -> sensor.execute(context)).isEqualTo(expectedException);
  }

  @Test
  void testParsingExceptionShouldAddError() {
    setupProject(BAD_SYNTAX);

    sensor.execute(context);

    assertThat(sensor.getErrors()).hasSize(1);
  }

  @Test
  void testWhenShouldExecuteOnProjectReturnsFalseThenExecutorIsNotCalled() {
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(false);

    sensor.execute(context);

    verify(executor, never()).execute(any(), any());
  }

  private void setupProject(String path) {
    File sourceFile = DelphiUtils.getResource(path);

    InputFile inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(sourceFile.toURI());

    when(delphiProjectHelper.mainFiles()).thenReturn(List.of(inputFile));
    when(delphiProjectHelper.getFile(anyString())).thenReturn(inputFile);
    when(delphiProjectHelper.standardLibraryPath())
        .thenReturn(DelphiUtils.getResource(STANDARD_LIBRARY).toPath());
  }
}
