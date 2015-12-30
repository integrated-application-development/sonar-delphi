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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.StubIssueBuilder;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;
import org.sonar.plugins.delphi.core.language.impl.DelphiClassProperty;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Ignore("Unused functions it's not working. There are many false positives.")
public class DeadCodeMetricsTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/metrics/DeadCodeMetricsTest.pas";
  private static final String DEAD_FILE = "/org/sonar/plugins/delphi/metrics/DeadCodeUnit.pas";

  private DeadCodeMetrics metrics;
  private List<UnitInterface> units;
  private List<ClassInterface> classes;
  private List<FunctionInterface> functions;
  private ResourcePerspectives perspectives;
  private Issuable issuable;
  private final List<Issue> issues = new ArrayList<Issue>();
  private ActiveRules activeRules;

  @Before
  public void init() {
    functions = new ArrayList<FunctionInterface>();
    classes = new ArrayList<ClassInterface>();
    units = new ArrayList<UnitInterface>();

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

    perspectives = mock(ResourcePerspectives.class);

    issuable = mock(Issuable.class);

    when(perspectives.as(Matchers.eq(Issuable.class), Matchers.isA(InputFile.class))).thenReturn(issuable);

    when(issuable.newIssueBuilder()).thenReturn(new StubIssueBuilder());

    when(issuable.addIssue(Matchers.any(Issue.class))).then(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        Issue issue = (Issue) invocation.getArguments()[0];
        issues.add(issue);
        return Boolean.TRUE;
      }
    });

    ActiveRule activeRuleUnusedFunction = mock(ActiveRule.class);
    ActiveRule activeRuleUnusedUnit = mock(ActiveRule.class);

    when(activeRuleUnusedFunction.ruleKey()).thenReturn(DeadCodeMetrics.RULE_KEY_UNUSED_FUNCTION);
    when(activeRuleUnusedUnit.ruleKey()).thenReturn(DeadCodeMetrics.RULE_KEY_UNUSED_UNIT);

    activeRules = mock(ActiveRules.class);
    when(activeRules.find(DeadCodeMetrics.RULE_KEY_UNUSED_FUNCTION)).thenReturn(activeRuleUnusedUnit);
    when(activeRules.find(DeadCodeMetrics.RULE_KEY_UNUSED_UNIT)).thenReturn(activeRuleUnusedUnit);

    metrics = new DeadCodeMetrics(activeRules, perspectives);
  }

  @Test
  public void analyseTest() {
    DebugSensorContext context = new DebugSensorContext();
    metrics.analyse(null, context, classes, functions, units);
    metrics.save(new DefaultInputFile("unit3").setAbsolutePath(pathTo("unit3.pas")), context);
    metrics.save(new DefaultInputFile("unit2").setAbsolutePath(pathTo("unit2.pas")), context);

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
  public void analyseFileTest() throws IllegalStateException, IOException, RecognitionException {
    DebugSensorContext context = new DebugSensorContext();
    DelphiAST ast = new DelphiAST(DelphiUtils.getResource(TEST_FILE));
    ASTAnalyzer analyser = new DelphiASTAnalyzer(DelphiTestUtils.mockProjectHelper());
    assertFalse("Grammar error", ast.isError());
    analyser.analyze(ast);
    metrics.analyse(null, context, analyser.getResults().getClasses(), analyser.getResults().getFunctions(),
      analyser.getResults()
        .getCachedUnitsAsList());

    metrics.save(new DefaultInputFile("DeadCodeUnit").setAbsolutePath(DEAD_FILE), context);

    for (Issue issue : issues) {
      System.out.println("issue: " + issue.key() + " line: " + issue.line() + " message: " + issue.message());
    }

    assertThat(issues, hasSize(2));
  }

}
