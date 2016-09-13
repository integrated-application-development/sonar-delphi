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
package org.sonar.plugins.delphi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.debug.ProjectMetricsXMLParser;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DelphiSensorTest {

  private Project project = null;
  private DelphiSensor sensor = null;
  private File baseDir = null;
  private Map<String, Integer> keyMetricIndex = null;
  private DelphiProjectHelper delphiProjectHelper;
  private ActiveRules activeRules;
  private ResourcePerspectives perspectives;

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
  private final DelphiProject delphiProject = new DelphiProject("Default Project");

  @Before
  public void init() {

    project = mock(Project.class);


    baseDir = DelphiUtils.getResource(ROOT_NAME);
    File reportDir = new File(baseDir.getAbsolutePath() + "/reports");

    File[] dirs = baseDir.listFiles(DelphiUtils.getDirectoryFilter()); // get
                                                                       // all
                                                                       // directories

    List<File> sourceDirs = new ArrayList<File>(dirs.length);
    List<InputFile> sourceFiles = new ArrayList<InputFile>();

    sourceDirs.add(baseDir); // include baseDir
    for (File source : baseDir.listFiles(DelphiUtils.getFileFilter())) {
      sourceFiles.add(new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",source.getPath()).setModuleBaseDir(Paths.get(ROOT_NAME)));
    }

    for (File directory : dirs) { // get all source files from all
                                  // directories
      File[] files = directory.listFiles(DelphiUtils.getFileFilter());
      for (File sourceFile : files) {
        sourceFiles.add(new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",sourceFile.getPath()).setModuleBaseDir(Paths.get(ROOT_NAME)));
      }
      sourceDirs.add(directory); // put all directories to list
    }



    perspectives = mock(ResourcePerspectives.class);

    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();
    DelphiTestUtils.mockGetFileFromString(delphiProjectHelper);

    delphiProject.setSourceFiles(sourceFiles);

    when(delphiProjectHelper.getWorkgroupProjects()).thenReturn(Arrays.asList(delphiProject));
    when(delphiProjectHelper.getDirectory(Matchers.any(File.class), Matchers.any(Project.class))).thenCallRealMethod();

    activeRules = mock(ActiveRules.class);
    ActiveRule activeRule = mock(ActiveRule.class);
    when(activeRules.find(Matchers.any(RuleKey.class))).thenReturn(activeRule);
    when(activeRule.param("Threshold")).thenReturn("3");

    sensor = new DelphiSensor(delphiProjectHelper, activeRules, perspectives);
  }

  @Test
  public void shouldExecuteOnProject() {
    assertTrue(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void analyseTest() {
    createKeyMetricIndexMap();

    // xml file for expected metrics for files
    ProjectMetricsXMLParser xmlParser = new ProjectMetricsXMLParser(new File(baseDir.getAbsolutePath() + File.separator + "values.xml"));

    DebugSensorContext context = new DebugSensorContext();
//    sensor.analyse(project, context); // analysing project

    // create a map of expected values for each file
    Map<String, Double[]> expectedValues = new HashMap<String, Double[]>();
    for (String fileName : xmlParser.getFileNames()) {
      expectedValues.put(fileName.toLowerCase(), xmlParser.getFileValues(fileName));
    }

    for (String key : context.getMeasuresKeys()) {
      Measure<?> measure = context.getMeasure(key);

      // get file name
      String fileKey = key.substring(0, key.lastIndexOf(':')).toLowerCase();

      // get metric key
      String metricKey = key.substring(key.lastIndexOf(':') + 1, key.length());

      if (!expectedValues.containsKey(fileKey) && DelphiUtils.acceptFile(fileKey)) {
        fail("Measure key: " + key + " Unexpected file: " + fileKey);
      } else {
        // Skip directories
        continue;
      }

      if (keyMetricIndex.get(metricKey) == null) {
        continue;
      }

      double currentValue = measure.getValue();
      double expectedValue = expectedValues.get(fileKey)[keyMetricIndex.get(metricKey)];

      assertEquals(fileKey + "@" + metricKey, expectedValue, currentValue, 0.0);
    }
  }

  private void createKeyMetricIndexMap() {
    keyMetricIndex = new HashMap<String, Integer>();
    keyMetricIndex.put("complexity", 0);
    keyMetricIndex.put("functions", 1);
    keyMetricIndex.put("function_complexity", 2);
    keyMetricIndex.put("classes", 3);
    keyMetricIndex.put("lines", 4);
    keyMetricIndex.put("comment_lines", 5);
    keyMetricIndex.put("accessors", 6);
    keyMetricIndex.put("public_undocumented_api", 7);
    keyMetricIndex.put("ncloc", 8);
    keyMetricIndex.put("files", 9);
    keyMetricIndex.put("package.files", 10);
    keyMetricIndex.put("package.packages", 11);
    keyMetricIndex.put("class_complexity", 12);
    keyMetricIndex.put("noc", 13);
    keyMetricIndex.put("statements", 14);
    keyMetricIndex.put("public_api", 15);
    keyMetricIndex.put("comment_blank_lines", 16);
  }

  @Test
  public void analyseFileOnRootDir() {
    createKeyMetricIndexMap();

    ProjectMetricsXMLParser xmlParser = new ProjectMetricsXMLParser(new File(baseDir.getAbsolutePath()
      + File.separator + "values.xml")); // xml file for
    // expected
    // metrics for
    // files
    DebugSensorContext context = new DebugSensorContext(); // new debug
                                                           // context for
                                                           // debug
                                                           // information
//    sensor.analyse(project, context); // analysing project

    Map<String, Double[]> expectedValues = new HashMap<String, Double[]>(); // create
                                                                            // a
                                                                            // map
                                                                            // of
                                                                            // expected
                                                                            // values
                                                                            // for
                                                                            // each
                                                                            // file
    for (String fileName : xmlParser.getFileNames()) {
      expectedValues.put(fileName, xmlParser.getFileValues(fileName));
    }

    for (String key : context.getMeasuresKeys()) { // check each measure if
                                                   // it is correct
      String fileKey = key.substring(0, key.lastIndexOf(':')); // get file
                                                               // name
      String metricKey = key.substring(key.lastIndexOf(':') + 1, key.length()); // get
                                                                                // metric
                                                                                // key

      if (!expectedValues.containsKey(fileKey)) {
        continue; // skip [default] package
      }
      if (keyMetricIndex.get(metricKey) == null) {
        continue;
      }

      Measure<?> measure = context.getMeasure(key);
      double currentValue = measure.getValue();
      double expectedValue = expectedValues.get(fileKey)[keyMetricIndex.get(metricKey)];

      assertEquals(fileKey + "@" + metricKey, expectedValue, currentValue, 0.0);
    }
  }

  @Test
  public void analyseWithEmptySourceFiles() {
    delphiProject.getSourceFiles().clear();
    DebugSensorContext context = new DebugSensorContext();
    sensor.analyse(project, context);
  }

  @Test
  public void analyseWithBadSourceFileSintax() {
    delphiProject.getSourceFiles().clear();
    delphiProject.getSourceFiles().add(new File(baseDir + "/Globals.pas"));
    delphiProject.getSourceFiles().add(new File(baseDir + "/../BadSyntax.pas"));
    DebugSensorContext context = new DebugSensorContext();
//    sensor.analyse(project, context);

    //assertThat("processed files", sensor.getProcessedFilesCount(), is(1));
    //assertThat("units", sensor.getUnits(), hasSize(1));
    //assertThat("file classes", sensor.getFileClasses().size(), is(1));
    //assertThat("file functions", sensor.getFileFunctions().size(), is(1));
  }

}
