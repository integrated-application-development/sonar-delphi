/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.CoreProperties;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Project.AnalysisType;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.delphi.debug.DebugConfiguration;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class SurefireSensorTest {

  private static final String PROJECT_DIR = "/org/sonar/plugins/delphi/SimpleDelphiProject";
  private static final String PROJECT_TEST_DIR = "/org/sonar/plugins/delphi/SimpleDelphiProject/tests";
  private static final String SUREFIRE_REPORT_DIR = "./reports";
  
  private Project project;
  private DebugConfiguration configuration;
  
  @Before
  public void setup()
  {
    List<File> testDirs = new ArrayList<File>();
    testDirs.add( DelphiUtils.getResource(PROJECT_TEST_DIR) );
    
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getBasedir()).thenReturn( DelphiUtils.getResource(PROJECT_DIR) );
    when(fileSystem.getTestDirs()).thenReturn(testDirs);
    
    project = mock(Project.class);
    when(project.getLanguageKey()).thenReturn("delph");
    when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);    
    when(project.getFileSystem()).thenReturn(fileSystem);
    
    configuration = new DebugConfiguration();
    configuration.addProperty(CoreProperties.SUREFIRE_REPORTS_PATH_PROPERTY, SUREFIRE_REPORT_DIR);
  }
  
  @Test
  public void shouldExecuteOnProjectTest() {
    assertTrue(new SurefireSensor(configuration).shouldExecuteOnProject(project));
  }
  
  @Test
  public void analyzeTest() {
    DebugSensorContext context = new DebugSensorContext();
    SurefireSensor sensor = new SurefireSensor(configuration);
    sensor.analyse(project, context);
    assertEquals(12, context.getMeasuresKeys().size());
  }
  
}
