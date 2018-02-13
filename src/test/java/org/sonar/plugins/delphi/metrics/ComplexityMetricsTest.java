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
package org.sonar.plugins.delphi.metrics;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.*;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.fs.internal.DefaultIndexedFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ComplexityMetricsTest {

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/metrics/";
  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/ComplexityMetricsTest.pas";
  private static final String FILE_NAME_LIST_UTILS = "/org/sonar/plugins/delphi/metrics/ListUtils.pas";

  private File baseDir = null;
  private SensorContextTester sensorContext;

  private ActiveRules activeRules;

  @Before
  public void setup() {
    baseDir = DelphiUtils.getResource(ROOT_NAME);
    sensorContext = SensorContextTester.create(baseDir);

    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    NewActiveRule rule = rulesBuilder.create(ComplexityMetrics.RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY);
    rule.setParam("Threshold", "3").setLanguage(DelphiLanguage.KEY).activate();
    activeRules = rulesBuilder.build();
  }

  private String getRelativePath(File prefix, String fullPath)
  {
    String result = fullPath.substring(prefix.getAbsolutePath().length() + 1);
    return result;
  }

  @Test
  public void analyzeTest() throws FileNotFoundException {
    // init
    File testFile = DelphiUtils.getResource(FILE_NAME);
    CodeAnalysisCacheResults.resetCache();
    ASTAnalyzer analyzer = new DelphiASTAnalyzer(DelphiTestUtils.mockProjectHelper());
    final CodeAnalysisResults results = analyzer.analyze(new DelphiAST(testFile));

    // processing
    ComplexityMetrics metrics = new ComplexityMetrics(activeRules, sensorContext);

    InputStream fileStream = new FileInputStream(testFile);
    Metadata metadata = new FileMetadata().readMetadata(fileStream, StandardCharsets.UTF_8, testFile.getPath());
    DefaultInputFile inputFile = TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, testFile)
        .setModuleBaseDir(baseDir.toPath())
        .setLanguage(DelphiLanguage.KEY)
        .setType(InputFile.Type.MAIN)
        .setMetadata(metadata)
        .build();

    metrics.analyse(inputFile, results.getClasses(), results.getFunctions(), null);

    assertEquals("CLASS_COMPLEXITY", (Integer)7, metrics.getIntMetric("CLASS_COMPLEXITY"));
    assertEquals("CLASSES", (Integer)2, metrics.getIntMetric("CLASSES"));
    assertEquals("COMPLEXITY", (Integer)10, metrics.getIntMetric("COMPLEXITY"));
    assertEquals("FUNCTIONS", (Integer)4, metrics.getIntMetric("FUNCTIONS"));
    assertEquals("FUNCTION_COMPLEXITY", (Integer)10, metrics.getIntMetric("FUNCTION_COMPLEXITY"));
    assertEquals("PUBLIC_API", (Integer)5, metrics.getIntMetric("PUBLIC_API"));
    assertEquals("STATEMENTS", (Integer)20, metrics.getIntMetric("STATEMENTS"));

    Issue[] issues = sensorContext.allIssues().toArray(new Issue[0]);
    assertEquals(1, issues.length);
    Issue issue = issues[0];
    assertEquals("delph:MethodCyclomaticComplexityRule", issue.ruleKey().toString());
    assertEquals(44, issue.primaryLocation().textRange().start().line());
  }

  @Test
  public void analyseListUtils() {
    // init
    File testFile = DelphiUtils.getResource(FILE_NAME_LIST_UTILS);
    CodeAnalysisCacheResults.resetCache();
    ASTAnalyzer analyzer = new DelphiASTAnalyzer(DelphiTestUtils.mockProjectHelper());
    CodeAnalysisResults results = analyzer.analyze(new DelphiAST(testFile));

    // processing
    ComplexityMetrics metrics = new ComplexityMetrics(activeRules, sensorContext);
//    metrics.analyse(new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5","test"),
//        results.getClasses(), results.getFunctions(), null);
  }

}
