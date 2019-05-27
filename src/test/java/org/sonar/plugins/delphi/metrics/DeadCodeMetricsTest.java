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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
//import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.plugins.delphi.DelphiTestUtils;
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

@Ignore("Unused functions it's not working. There are many false positives.")
public class DeadCodeMetricsTest {
/*
  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/metrics/";
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/metrics/DeadCodeMetricsTest.pas";
  private static final String DEAD_FILE = "/org/sonar/plugins/delphi/metrics/DeadCodeUnit.pas";

  private DeadCodeMetrics metrics;
  private Set<UnitInterface> units;
  private List<ClassInterface> classes;
  private List<FunctionInterface> functions;
  private SensorContextTester sensorContext;
  private Issuable issuable;
  private final List<Issue> issues = new ArrayList<>();
  private ActiveRules activeRules;
  private File baseDir;

  @Before
  public void init() {
    functions = new ArrayList<>();
    classes = new ArrayList<>();
    units = new HashSet<>();

    FunctionInterface f1 = new DelphiFunction("function1");
    FunctionInterface f2 = new DelphiFunction("function2");
    FunctionInterface f3 = new DelphiFunction("function3");
    f1.addCalledFunction(f2);
    f2.addCalledFunction(f1);
    f3.setLine(321);

    ClassPropertyInterface p1 = new DelphiClassProperty();
    p1.setReadFunction(f1);
    p1.setWriteFunction(f2);

    ClassInterface c1 = new DelphiClass("class1");
    ClassInterface c2 = new DelphiClass("class2");
    c1.addFunction(f1);
    c2.addFunction(f2);
    c2.addFunction(f3);
    c2.addProperty(p1);

    UnitInterface u1 = new DelphiUnit("unit1");
    UnitInterface u2 = new DelphiUnit("unit2");
    UnitInterface u3 = new DelphiUnit("unit3");
    u1.setPath("unit1.dpr");
    u2.setPath("unit2.pas");
    u3.setPath("unit3.pas");

    u1.addIncludes("unit2");
    u2.addIncludes("unit1");
    u3.setLine(123);
    u1.addClass(c1);
    u2.addClass(c2);

    units.add(u1);
    units.add(u2);
    units.add(u3);

    baseDir = DelphiUtils.getResource(ROOT_NAME);
    sensorContext = SensorContextTester.create(baseDir);

    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    rulesBuilder.create(DeadCodeMetrics.RULE_KEY_UNUSED_FUNCTION).setLanguage(DelphiLanguage.KEY)
        .activate();
    rulesBuilder.create(DeadCodeMetrics.RULE_KEY_UNUSED_UNIT).setLanguage(DelphiLanguage.KEY)
        .activate();
    activeRules = rulesBuilder.build();

    metrics = new DeadCodeMetrics(activeRules, sensorContext);
  }

  @Test
  public void testAnalyse() {
    metrics.analyse(null, classes, functions, units);
//    metrics.save(new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",pathTo("unit3")));
//    metrics.save(new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",pathTo("unit2")));

    assertThat(issues, hasSize(2));
    int lines[] = {123, 321};
    for (int i = 0; i < issues.size(); ++i) {
      assertEquals("Invalid unit line", lines[i], issues.get(i).line().intValue());
    }

  }

  private String pathTo(String file) {
    return "/org/sonar/plugins/delphi/metrics/" + file;
  }

  @Test
  public void testAnalyseFile() throws IllegalStateException {
    DelphiAST ast = new DelphiAST(DelphiUtils.getResource(TEST_FILE));
    ASTAnalyzer analyser = new DelphiASTAnalyzer(DelphiTestUtils.mockProjectHelper());
    assertFalse("Grammar error", ast.isError());
    CodeAnalysisResults results = analyser.analyze(ast);
    metrics.analyse(null, results.getClasses(), results.getFunctions(),
        results.getCachedUnitsAsList());

//    metrics.save(new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",DEAD_FILE));

    for (Issue issue : issues) {
      System.out.println(
          "issue: " + issue.key() + " line: " + issue.line() + " message: " + issue.message());
    }

    assertThat(issues, hasSize(2));
  }

 */
}
