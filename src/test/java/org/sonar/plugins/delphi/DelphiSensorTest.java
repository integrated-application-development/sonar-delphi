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
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.ProjectMetricsXMLParser;
import org.sonar.plugins.delphi.metrics.ComplexityMetrics;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class DelphiSensorTest {

  private DelphiSensor sensor = null;
  private File baseDir = null;
  private DelphiProjectHelper delphiProjectHelper;
  private ActiveRules activeRules;
  private SensorContextTester context;

  private final String moduleKey = "ROOT_KEY_CHANGE_AT_SONARAPI_5";
  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
  private final DelphiProject delphiProject = new DelphiProject("Default Project");

  private String getRelativePath(File prefix, String fullPath)
  {
    String result = fullPath.substring(prefix.getAbsolutePath().length() + 1);
    return result;
  }

  @Before
  public void init() {

    baseDir = DelphiUtils.getResource(ROOT_NAME);
    File reportDir = new File(baseDir.getAbsolutePath() + "/reports");

    // get all directories
    File[] dirs = baseDir.listFiles(DelphiUtils.getDirectoryFilter());

    List<File> sourceDirs = new ArrayList<>(dirs.length);
    List<InputFile> sourceFiles = new ArrayList<>();

    context = SensorContextTester.create(baseDir);

    sourceDirs.add(baseDir); // include baseDir
    DefaultInputDir inputBaseDir = new DefaultInputDir(moduleKey, "");
    context.fileSystem().add(inputBaseDir);

    for (File source : baseDir.listFiles(DelphiUtils.getFileFilter())) {
      DefaultInputFile baseInputFile = new DefaultInputFile(moduleKey,
          getRelativePath(baseDir,source.getPath()))
          .setModuleBaseDir(Paths.get(ROOT_NAME))
          .setLanguage(DelphiLanguage.KEY)
          .setType(InputFile.Type.MAIN)
          .initMetadata(new FileMetadata().readMetadata(source, Charset.defaultCharset()));

      sourceFiles.add(baseInputFile);
      context.fileSystem().add(baseInputFile);
    }

    // get all source files from all directories
    for (File directory : dirs) {

      File[] files = directory.listFiles(DelphiUtils.getFileFilter());
      for (File sourceFile : files) {

        DefaultInputFile inputFile = new DefaultInputFile(moduleKey,
            getRelativePath(baseDir,sourceFile.getPath()))
            .setModuleBaseDir(Paths.get(ROOT_NAME))
            .setLanguage(DelphiLanguage.KEY)
            .setType(InputFile.Type.MAIN)
            .initMetadata(new FileMetadata().readMetadata(sourceFile, Charset.defaultCharset()));

        context.fileSystem().add(inputFile);
        sourceFiles.add(inputFile);
      }
      DefaultInputDir inputDir = new DefaultInputDir(moduleKey, getRelativePath(baseDir,directory.getPath()));
      context.fileSystem().add(inputDir);
      // put all directories to list
      sourceDirs.add(directory);
    }

    delphiProjectHelper = new DelphiProjectHelper(context.settings(), context.fileSystem());

    delphiProject.setSourceFiles(sourceFiles);

    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    NewActiveRule rule = rulesBuilder.create(ComplexityMetrics.RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY);
    rule.setParam("Threshold", "3").setLanguage(DelphiLanguage.KEY).activate();
    activeRules = rulesBuilder.build();

    sensor = new DelphiSensor(delphiProjectHelper, activeRules, context);
  }

  @Test
  public void describeTest() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("Combined LCOV and LOC sensor");
    assertThat(sensorDescriptor.languages()).containsOnly("delph");
  }

  @Test
  public void executeTest() {
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(0);

    // create a map of expected values for each file
    Map<String, Map<String,Double>> expectedValues = new HashMap<>();

    // xml file for expected metrics for files
    ProjectMetricsXMLParser xmlParser = new ProjectMetricsXMLParser(
        new File(baseDir.getAbsolutePath() + File.separator + "values.xml"));
    for (String fileName : xmlParser.getFileNames()) {
      expectedValues.put(fileName.toLowerCase(), xmlParser.getFileValues(fileName));
    }

    for (InputFile file : context.fileSystem().inputFiles()) {
      String relativePath = file.relativePath();
      String fileName = Paths.get(relativePath).getFileName().toString().toLowerCase();
      if (expectedValues.containsKey(fileName) && DelphiUtils.acceptFile(fileName)) {
        Map<String, Double> fileExpectedValues = expectedValues.get(fileName);
        Collection<Measure> measures = context.measures(moduleKey + ":" + relativePath);
        for (Measure measure : measures) {
          String metricName = measure.metric().key();
          Double expectedValue = fileExpectedValues.get(metricName);
          Double currentValue = Double.valueOf(measure.value().toString());
          assertEquals(fileName + "@" + metricName, expectedValue, currentValue, 0.0);
        }
      }
    }
  }

  @Test
  public void analyseWithEmptySourceFiles() {
    delphiProject.getSourceFiles().clear();
//    DebugSensorContext context = new DebugSensorContext();
//    sensor.execute(context);
  }

  @Test
  public void analyseWithBadSourceFileSyntax() {
    delphiProject.getSourceFiles().clear();
    delphiProject.getSourceFiles().add(new File(baseDir + "/Globals.pas"));
    delphiProject.getSourceFiles().add(new File(baseDir + "/../BadSyntax.pas"));
//    DebugSensorContext context = new DebugSensorContext();
//    sensor.execute(context);

    //assertThat("processed files", sensor.getProcessedFilesCount(), is(1));
    //assertThat("units", sensor.getUnits(), hasSize(1));
    //assertThat("file classes", sensor.getFileClasses().size(), is(1));
    //assertThat("file functions", sensor.getFileFunctions().size(), is(1));
  }

}
