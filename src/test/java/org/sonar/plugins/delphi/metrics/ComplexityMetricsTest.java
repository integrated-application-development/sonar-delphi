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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.IssueMatchers;
import org.sonar.plugins.delphi.StubIssueBuilder;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ComplexityMetricsTest {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/ComplexityMetricsTest.pas";

  private ResourcePerspectives perspectives;

  private final List<Issue> issues = new ArrayList<Issue>();

  private RuleFinder ruleFinder;

  @Before
  public void setup() {
    perspectives = mock(ResourcePerspectives.class);

    final Issuable issuable = mock(Issuable.class);

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

    ruleFinder = mock(RuleFinder.class);
    Rule rule = Rule.create(ComplexityMetrics.RULE_QUERY_METHOD_CYCLOMATIC_COMPLEXITY.getRepositoryKey(),
      ComplexityMetrics.RULE_QUERY_METHOD_CYCLOMATIC_COMPLEXITY.getKey());
    rule.createParameter("Threshold").setDefaultValue("3");
    when(ruleFinder.find(ComplexityMetrics.RULE_QUERY_METHOD_CYCLOMATIC_COMPLEXITY)).thenReturn(rule);
  }

  @Test
  public void analyseTest() throws Exception {
    // init
    File testFile = DelphiUtils.getResource(FILE_NAME);
    CodeAnalysisCacheResults.resetCache();
    ASTAnalyzer analyzer = new DelphiASTAnalyzer(DelphiTestUtils.mockProjectHelper());
    analyzer.analyze(new DelphiAST(testFile));

    // processing
    ComplexityMetrics metrics = new ComplexityMetrics(null, ruleFinder, perspectives);
    metrics.analyse(new DefaultInputFile("test"), null, analyzer.getResults().getClasses(), analyzer.getResults().getFunctions(), null);
    String[] keys = {"ACCESSORS", "CLASS_COMPLEXITY", "CLASSES", "COMPLEXITY", "FUNCTIONS", "FUNCTION_COMPLEXITY",
      "PUBLIC_API",
      "STATEMENTS", "DEPTH_IN_TREE", "NUMBER_OF_CHILDREN", "RFC"};
    double[] values = {2.0, 3.5, 2.0, 10.0, 4.0, 2.5, 5.0, 20.0, 2.0, 1.0, 3.0};

    for (int i = 0; i < keys.length; ++i) {
      assertEquals(keys[i] + " failure ->", values[i], metrics.getMetric(keys[i]), 0.0);
    }

    assertThat(issues, hasSize(1));
    assertThat(issues, hasItem(IssueMatchers.hasRuleKeyAtLine("MethodCyclomaticComplexityRule", 48)));
  }

}
