/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.surefire;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.surefire.api.SurefireUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurefireSensorTest {

  private static final String PROJECT_DIR = "/org/sonar/plugins/delphi/UnitTest";
  private static final String PROJECT_TEST_DIR = "/org/sonar/plugins/delphi/UnitTest/tests";
  private static final String SUREFIRE_REPORT_DIR = "./reports";

  private Project project;
  private Settings settings;
  private DelphiProjectHelper delphiProjectHelper;

  @Before
  public void setup() throws FileNotFoundException
  {
    List<File> testDirs = new ArrayList<File>();
    testDirs.add(DelphiUtils.getResource(PROJECT_TEST_DIR));

    project = mock(Project.class);
    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

    when(delphiProjectHelper.baseDir()).thenReturn(new File(getClass().getResource(PROJECT_DIR).getFile()));
    when(delphiProjectHelper.findTestFileInDirectories(anyString())).thenAnswer(new Answer<InputFile>() {
      @Override
      public InputFile answer(InvocationOnMock invocation) throws Throwable {
        String file = (String) invocation.getArguments()[0];
        return new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",(new File(file)).getPath());
      }
    });

    settings = new Settings();
  }

  @Test
  public void shouldExecuteOnProjectTest() {
    assertTrue(new SurefireSensor(settings, delphiProjectHelper)
      .shouldExecuteOnProject(project));
  }

  @Test
  public void analyzeTest() {
    settings.setProperty(SurefireUtils.SUREFIRE_REPORTS_PATH_PROPERTY, SUREFIRE_REPORT_DIR);
    DebugSensorContext context = new DebugSensorContext();
    SurefireSensor sensor = new SurefireSensor(settings, delphiProjectHelper);
    //sensor.analyse(project, context);

    //assertEquals(18, context.getMeasuresKeys().size());
  }

  @Test
  public void analyzeTestUsingDefaultSurefireReportsPath() {
    DebugSensorContext context = new DebugSensorContext();
    SurefireSensor sensor = new SurefireSensor(settings, delphiProjectHelper);
    //sensor.analyse(project, context);

    //assertEquals(24, context.getMeasuresKeys().size());
  }

}
