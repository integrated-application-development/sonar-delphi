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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiSensorTest {
  private static final String BASE_PATH = "/org/sonar/plugins/delphi/projects/";
  private static final File BASE_DIR = DelphiUtils.getResource(BASE_PATH);
  private static final String BAD_SYNTAX = BASE_PATH + "BadSyntaxProject/BadSyntax.Pas";
  private static final String GLOBALS = BASE_PATH + "SimpleProject/Globals.Pas";

  private final DelphiMasterExecutor executor = mock(DelphiMasterExecutor.class);
  private final DefaultFileSystem fileSystem = new DefaultFileSystem(BASE_DIR);
  private final SensorContextTester context = SensorContextTester.create(fileSystem.baseDir());
  private final DelphiProjectHelper delphiProjectHelper = mock(DelphiProjectHelper.class);

  private DelphiSensor sensor;

  @Rule public ExpectedException exceptionCatcher = ExpectedException.none();

  @Before
  public void setupSensor() {
    sensor = new DelphiSensor(delphiProjectHelper, executor);
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(true);
  }

  @Test
  public void testToString() {
    final String toString = sensor.toString();
    assertThat(toString, is("DelphiSensor"));
  }

  @Test
  public void testDescribe() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(DelphiLanguage.KEY);
    verify(mockDescriptor).name("DelphiSensor");
  }

  @Test
  public void testSensorShouldNotRethrowOtherExceptions() {
    setupProject(GLOBALS);

    final RuntimeException expectedException = new RuntimeException();
    willThrow(expectedException)
        .given(executor)
        .execute(any(SensorContext.class), any(DelphiFile.class));

    exceptionCatcher.expect(is(expectedException));

    sensor.execute(context);
  }

  @Test
  public void testParsingExceptionShouldAddError() {
    setupProject(BAD_SYNTAX);

    sensor.execute(context);

    assertThat(sensor.getErrors(), hasSize(1));
  }

  @Test
  public void testWhenShouldExecuteOnProjectReturnsFalseThenExecutorIsNotCalled() {
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(false);

    sensor.execute(context);

    verify(executor, never()).execute(any(), any());
  }

  private void setupProject(String path) {
    File srcFile = DelphiUtils.getResource(path);

    DelphiProject project = new DelphiProject("Test");
    project.addFile(srcFile.getPath());

    InputFile inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(srcFile.toURI());

    when(delphiProjectHelper.getProjects()).thenReturn(Collections.singletonList(project));
    when(delphiProjectHelper.getFile(anyString())).thenReturn(inputFile);
  }
}
