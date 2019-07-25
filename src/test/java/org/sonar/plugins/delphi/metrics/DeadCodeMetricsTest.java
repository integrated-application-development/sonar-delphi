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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;
import org.sonar.plugins.delphi.core.language.impl.DelphiClassProperty;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DeadCodeMetricsTest {
  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/metrics/dead-code/";
  private static final String TEST_FILE =
      "/org/sonar/plugins/delphi/metrics/dead-code/DeadCodeMetricsTest.pas";
  private static final String UNIT_FILE_1 = "/org/sonar/plugins/delphi/metrics/dead-code/Unit1.pas";
  private static final String UNIT_FILE_2 = "/org/sonar/plugins/delphi/metrics/dead-code/Unit2.pas";
  private static final String UNIT_FILE_3 = "/org/sonar/plugins/delphi/metrics/dead-code/Unit3.pas";

  private DeadCodeMetrics metrics;
  private Set<UnitInterface> units;
  private List<ClassInterface> classes;
  private List<FunctionInterface> functions;
  private SensorContextTester sensorContext;
  private File baseDir;
  private File unitFile1;
  private File unitFile2;
  private File unitFile3;

  @Before
  public void init() {
    functions = new ArrayList<>();
    classes = new ArrayList<>();
    units = new HashSet<>();
    unitFile1 = DelphiUtils.getResource(UNIT_FILE_1);
    unitFile2 = DelphiUtils.getResource(UNIT_FILE_2);
    unitFile3 = DelphiUtils.getResource(UNIT_FILE_3);

    FunctionInterface f1 = new DelphiFunction("function1");
    FunctionInterface f2 = new DelphiFunction("function2");
    FunctionInterface f3 = new DelphiFunction("function3");
    f1.addCalledFunction(f2);
    f2.addCalledFunction(f1);
    f3.setLine(11);
    f3.setBeginColumn(14);
    f3.setEndColumn(23);

    ClassPropertyInterface p1 = new DelphiClassProperty();
    p1.setReadFunction(f1.getShortName());
    p1.setWriteFunction(f2.getShortName());

    ClassInterface c1 = new DelphiClass("class1");
    ClassInterface c2 = new DelphiClass("class2");
    c1.addFunction(f1);
    c2.addFunction(f2);
    c2.addFunction(f3);
    c2.addProperty(p1);

    UnitInterface u1 = new DelphiUnit("unit1");
    UnitInterface u2 = new DelphiUnit("unit2");
    UnitInterface u3 = new DelphiUnit("unit3");
    u1.setPath(unitFile1.getPath());
    u2.setPath(unitFile2.getPath());
    u3.setPath(unitFile3.getPath());

    u1.addIncludes("unit2");
    u2.addIncludes("unit1");
    u3.setLine(1);
    u1.addClass(c1);
    u2.addClass(c2);

    units.add(u1);
    units.add(u2);
    units.add(u3);

    baseDir = DelphiUtils.getResource(ROOT_NAME);
    sensorContext = SensorContextTester.create(baseDir);

    metrics = new DeadCodeMetrics(sensorContext);
  }

  @Test
  public void testAnalyse() throws IOException {
    metrics.analyse(null, classes, functions, units);

    DefaultInputFile unit1 =
        TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, unitFile1)
            .setLanguage(DelphiLanguage.KEY)
            .setType(InputFile.Type.MAIN)
            .setContents(DelphiUtils.readFileContent(unitFile1, Charset.defaultCharset().name()))
            .build();

    DefaultInputFile unit2 =
        TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, unitFile2)
            .setLanguage(DelphiLanguage.KEY)
            .setType(InputFile.Type.MAIN)
            .setContents(DelphiUtils.readFileContent(unitFile2, Charset.defaultCharset().name()))
            .build();

    DefaultInputFile unit3 =
        TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, unitFile3)
            .setLanguage(DelphiLanguage.KEY)
            .setType(InputFile.Type.MAIN)
            .setContents(DelphiUtils.readFileContent(unitFile3, Charset.defaultCharset().name()))
            .build();

    metrics.save(unit1);
    metrics.save(unit2);
    metrics.save(unit3);

    var issues = sensorContext.allIssues();
    assertThat(issues, hasSize(2));
    assertThat(issues, hasItem(hasRuleKeyAtLine("UnusedUnitRule", 1)));
    assertThat(issues, hasItem(hasRuleKeyAtLine("UnusedFunctionRule", 11)));
  }

  @Test
  public void testAnalyseFile() throws IOException {
    File file = DelphiUtils.getResource(TEST_FILE);
    DelphiAST ast = new DelphiAST(file);
    ASTAnalyzer analyser = new DelphiASTAnalyzer();
    CodeAnalysisResults results = analyser.analyze(ast);

    DefaultInputFile testFile =
        TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, file)
            .setLanguage(DelphiLanguage.KEY)
            .setType(InputFile.Type.MAIN)
            .setContents(DelphiUtils.readFileContent(file, Charset.defaultCharset().name()))
            .build();

    metrics.analyse(
        testFile, results.getClasses(), results.getFunctions(), results.getCachedUnits());

    metrics.save(testFile);

    var issues = sensorContext.allIssues();
    assertThat(issues, hasSize(3));
    assertThat(issues, hasItem(hasRuleKeyAtLine("UnusedUnitRule", 1)));
    assertThat(issues, hasItem(hasRuleKeyAtLine("UnusedFunctionRule", 14)));
    assertThat(issues, hasItem(hasRuleKeyAtLine("UnusedFunctionRule", 21)));
  }
}
