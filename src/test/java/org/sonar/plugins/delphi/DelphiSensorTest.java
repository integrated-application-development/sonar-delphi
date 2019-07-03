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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputDir;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.ProjectMetricsXMLParser;
import org.sonar.plugins.delphi.metrics.ComplexityMetrics;
import org.sonar.plugins.delphi.metrics.DeadCodeMetrics;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiSensorTest {

  private DelphiSensor sensor;
  private SensorContextTester context;
  private ActiveRules activeRules;

  private static final String SIMPLE_PROJECT = "/org/sonar/plugins/delphi/projects/SimpleProject";
  private static final String SIMPLE_PROJECT_METRICS = "/org/sonar/plugins/delphi/projects/SimpleProject/metrics.xml";
  private static final String BAD_PROJECT = "/org/sonar/plugins/delphi/projects/BadSyntaxProject";
  private static final String EMPTY_PROJECT = "/org/sonar/plugins/delphi/projects/EmptyProject";
  private static final String MODULE_KEY = "TEST_MODULE";
  private final DelphiProject delphiProject = new DelphiProject("Default Project");

  private String getRelativePath(File prefix, String fullPath) {
    return fullPath.substring(prefix.getAbsolutePath().length() + 1);
  }

  @Before
  public void init() {
    NewActiveRule complexityRule = new NewActiveRule.Builder()
        .setRuleKey(ComplexityMetrics.RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY)
        .setParam("Threshold", "3")
        .setLanguage(DelphiLanguage.KEY)
        .build();

    NewActiveRule unusedFunctionRule = new NewActiveRule.Builder()
        .setRuleKey(DeadCodeMetrics.RULE_KEY_UNUSED_FUNCTION)
        .setLanguage(DelphiLanguage.KEY)
        .build();

    NewActiveRule unusedUnitRule = new NewActiveRule.Builder()
        .setRuleKey(DeadCodeMetrics.RULE_KEY_UNUSED_UNIT)
        .setLanguage(DelphiLanguage.KEY)
        .build();

    activeRules = new ActiveRulesBuilder()
        .addRule(complexityRule)
        .addRule(unusedFunctionRule)
        .addRule(unusedUnitRule)
        .build();
  }

  @Test
  public void testDescribeTest() throws IOException {
    setupProject(EMPTY_PROJECT);
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    sensor.describe(sensorDescriptor);
    assertEquals("Combined LCOV and LOC sensor", sensorDescriptor.name());
    String[] expected = {DelphiLanguage.KEY};
    assertArrayEquals(expected, sensorDescriptor.languages().toArray());
  }

  @Test
  public void testExecuteTest() throws IOException  {
    setupProject(SIMPLE_PROJECT);
    sensor.execute(context);

    assertEquals(19, context.allIssues().size());

    // xml file for expected metrics for files
    File metricsFile = DelphiUtils.getResource(SIMPLE_PROJECT_METRICS);
    ProjectMetricsXMLParser xmlParser = new ProjectMetricsXMLParser(metricsFile);
    Map<String, Map<String, String>> expectedValues = new HashMap<>();

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
  public void testAnalyseWithNoSourceFiles() throws IOException {
    setupProject(EMPTY_PROJECT);
    sensor.execute(context);
  }

  @Test
  public void testAnalyseWithBadSourceFileSyntax() throws IOException {
    setupProject(BAD_PROJECT);
    sensor.execute(context);

    assertThat("processed files", sensor.getProcessedFilesCount(), is(1));
    assertThat("units", sensor.getUnits(), hasSize(1));
    assertThat("file classes", sensor.getFileClasses().size(), is(1));
    assertThat("file functions", sensor.getFileFunctions().size(), is(1));
  }

  private void setupProject(String projectPath) throws IOException {
    File baseDir = DelphiUtils.getResource(projectPath);
    context = SensorContextTester.create(baseDir);
    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(context.config(),
        context.fileSystem());
    sensor = new DelphiSensor(delphiProjectHelper, activeRules, context);

    DefaultInputDir inputBaseDir = new DefaultInputDir(MODULE_KEY, "");
    inputBaseDir.setModuleBaseDir(baseDir.toPath());
    List<File> sourceFiles = new ArrayList<>();

    File[] baseDirFiles = baseDir.listFiles(DelphiUtils.getFileFilter());
    assertNotNull(baseDirFiles);

    for (File source : baseDirFiles) {
      InputFile baseInputFile = TestInputFileBuilder.create(MODULE_KEY, baseDir, source)
          .setLanguage(DelphiLanguage.KEY)
          .setType(InputFile.Type.MAIN)
          .setContents(DelphiUtils.readFileContent(source, delphiProjectHelper.encoding()))
          .build();

      sourceFiles.add(source);
      context.fileSystem().add(baseInputFile);
    }

    File[] dirs = baseDir.listFiles(DelphiUtils.getDirectoryFilter());
    assertNotNull(dirs);

    // get all source files from all directories
    for (File directory : dirs) {
      File[] dirFiles = directory.listFiles(DelphiUtils.getFileFilter());
      assertNotNull(dirFiles);

      for (File sourceFile : dirFiles) {
        DefaultInputFile inputFile = TestInputFileBuilder.create(MODULE_KEY, baseDir, sourceFile)
            .setLanguage(DelphiLanguage.KEY)
            .setType(InputFile.Type.MAIN)
            .setContents(DelphiUtils.readFileContent(sourceFile, delphiProjectHelper.encoding()))
            .build();

        context.fileSystem().add(inputFile);
        sourceFiles.add(sourceFile);
      }

      DefaultInputDir inputDir = new DefaultInputDir(MODULE_KEY,
          getRelativePath(baseDir, directory.getPath()));
      inputDir.setModuleBaseDir(baseDir.toPath());
    }

    delphiProject.setSourceFiles(sourceFiles);
  }
}
