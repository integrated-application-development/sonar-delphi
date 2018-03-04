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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputDir;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.metrics.DeadCodeMetrics;
import org.sonar.plugins.delphi.debug.ProjectMetricsXMLParser;
import org.sonar.plugins.delphi.metrics.ComplexityMetrics;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class DelphiSensorTest {

  private DelphiSensor sensor = null;
  private File baseDir = null;
  private DelphiProjectHelper delphiProjectHelper;
  private ActiveRules activeRules;
  private SensorContextTester context;

  private final String moduleKey = "";
  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
  private final DelphiProject delphiProject = new DelphiProject("Default Project");

  private String getRelativePath(File prefix, String fullPath)
  {
    String result = fullPath.substring(prefix.getAbsolutePath().length() + 1);
    return result;
  }

  @Before
  public void init() throws IOException {

    baseDir = DelphiUtils.getResource(ROOT_NAME);

    // get all directories
    File[] dirs = baseDir.listFiles(DelphiUtils.getDirectoryFilter());

    List<File> sourceDirs = new ArrayList<>(dirs.length);
    List<File> sourceFiles = new ArrayList<>();

    context = SensorContextTester.create(baseDir);
    delphiProjectHelper = new DelphiProjectHelper(context.config(), context.fileSystem());

    sourceDirs.add(baseDir); // include baseDir
    DefaultInputDir inputBaseDir = new DefaultInputDir(moduleKey, "");
    inputBaseDir.setModuleBaseDir(baseDir.toPath());

    context.fileSystem().add(inputBaseDir);
    for (File source : baseDir.listFiles(DelphiUtils.getFileFilter())) {

      InputFile baseInputFile = TestInputFileBuilder.create(moduleKey, baseDir, source)
          .setLanguage(DelphiLanguage.KEY)
          .setType(InputFile.Type.MAIN)
          .setContents(DelphiUtils.readFileContent(source, delphiProjectHelper.encoding()))
          .build();

      sourceFiles.add(source);
      context.fileSystem().add(baseInputFile);
    }

    // get all source files from all directories
    for (File directory : dirs) {

      File[] files = directory.listFiles(DelphiUtils.getFileFilter());
      for (File sourceFile : files) {
        DefaultInputFile inputFile = TestInputFileBuilder.create(moduleKey, baseDir, sourceFile)
            .setLanguage(DelphiLanguage.KEY)
            .setType(InputFile.Type.MAIN)
            .setContents(DelphiUtils.readFileContent(sourceFile, delphiProjectHelper.encoding()))
            .build();

        context.fileSystem().add(inputFile);
        sourceFiles.add(sourceFile);
      }
      DefaultInputDir inputDir = new DefaultInputDir(moduleKey, getRelativePath(baseDir,directory.getPath()));
      inputDir.setModuleBaseDir(baseDir.toPath());
      context.fileSystem().add(inputDir);
      // put all directories to list
      sourceDirs.add(directory);
    }

    delphiProject.setSourceFiles(sourceFiles);

    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    NewActiveRule rule = rulesBuilder.create(ComplexityMetrics.RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY);
    rule.setParam("Threshold", "3").setLanguage(DelphiLanguage.KEY).activate();
    rulesBuilder.create(DeadCodeMetrics.RULE_KEY_UNUSED_FUNCTION).setLanguage(DelphiLanguage.KEY).activate();
    activeRules = rulesBuilder.build();

    sensor = new DelphiSensor(delphiProjectHelper, activeRules, context);
  }

  @Test
  public void describeTest() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    sensor.describe(sensorDescriptor);
    assertEquals("Combined LCOV and LOC sensor", sensorDescriptor.name());
    String [] expected = {"delph"};
    assertArrayEquals(expected, sensorDescriptor.languages().toArray());
  }

  @Test
  public void executeTest() {
    sensor.execute(context);

    assertEquals(12, context.allIssues().size());

    // create a map of expected values for each file
    Map<String, Map<String,String>> expectedValues = new HashMap<>();

    // xml file for expected metrics for files
    ProjectMetricsXMLParser xmlParser = new ProjectMetricsXMLParser(
        new File(baseDir.getAbsolutePath() + File.separator + "values.xml"));
    for (String fileName : xmlParser.getFileNames()) {
      expectedValues.put(fileName.toLowerCase(), xmlParser.getFileValues(fileName));
    }

    for (InputFile file : context.fileSystem().inputFiles()) {
      String fileName = file.filename().toLowerCase();
      if (expectedValues.containsKey(fileName) && DelphiUtils.acceptFile(fileName)) {
        Map<String, String> fileExpectedValues = expectedValues.get(fileName);
        Collection<Measure> measures = context.measures(file.key());
        for (Measure measure : measures) {
          String metricName = measure.metric().key();
          String expectedValue = fileExpectedValues.get(metricName);
          String currentValue = measure.value().toString();
          assertEquals(file.toString() + "@" + metricName, expectedValue, currentValue);
        }
      }
    }
  }

  @Test
  public void analyseWithEmptySourceFiles() {
    delphiProject.getSourceFiles().clear();
//    sensor.execute(context);
  }

  @Test
  public void analyseWithBadSourceFileSyntax() {
    delphiProject.getSourceFiles().clear();
    delphiProject.getSourceFiles().add(new File(baseDir + "/Globals.pas"));
    delphiProject.getSourceFiles().add(new File(baseDir + "/../BadSyntax.pas"));
//    sensor.execute(context);

    //assertThat("processed files", sensor.getProcessedFilesCount(), is(1));
    //assertThat("units", sensor.getUnits(), hasSize(1));
    //assertThat("file classes", sensor.getFileClasses().size(), is(1));
    //assertThat("file functions", sensor.getFileFunctions().size(), is(1));
  }

}
