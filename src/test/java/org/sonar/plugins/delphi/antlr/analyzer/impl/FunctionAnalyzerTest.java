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
package org.sonar.plugins.delphi.antlr.analyzer.impl;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import org.antlr.runtime.tree.Tree;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceToNodeOperation;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.ArgumentInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiArgument;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.debug.FileTestsCommon;

public class FunctionAnalyzerTest extends FileTestsCommon {

  private static final String FILE_NAME =
      "/org/sonar/plugins/delphi/metrics/FunctionMetricsTest.pas";
  private static final String FILE_NAME_MESSAGE_TEST =
      "/org/sonar/plugins/delphi/metrics/FunctionMessageTest.pas";
  private static final String FILE_NAME_VIRTUAL_TEST =
      "/org/sonar/plugins/delphi/metrics/FunctionVirtualTest.pas";
  private static final String FILE_NAME_OPERATOR_TEST =
      "/org/sonar/plugins/delphi/metrics/FunctionOperatorTest.pas";
  private static final String FILE_NAME_PARAMETERS_TEST =
      "/org/sonar/plugins/delphi/syntax/FunctionParametersAnalyzerTest.pas";

  @Rule public ExpectedException expectedException = ExpectedException.none();

  private final FunctionAnalyzer analyzer = new FunctionAnalyzer();

  private CodeAnalysisResults results;

  private CodeTree code;
  private AdvanceToNodeOperation advanceToFunction;

  private void setupFile(String fileName) throws IOException {
    loadFile(fileName);
    results = new CodeAnalysisResults();
    results.setActiveUnit(new DelphiUnit("test"));
    DelphiAST ast = new DelphiAST(testFile);
    code = new CodeTree(new CodeNode<>(ast), new CodeNode<>(ast.getChild(0)));
    advanceToFunction =
        new AdvanceToNodeOperation(
            Arrays.asList(
                LexerMetrics.FUNCTION,
                LexerMetrics.PROCEDURE,
                LexerMetrics.CONSTRUCTOR,
                LexerMetrics.DESTRUCTOR,
                LexerMetrics.OPERATOR));

    CodeAnalysisCacheResults.resetCache();
  }

  @Test
  public void testCanAnalyze() throws IOException {
    setupFile(FILE_NAME);
    assertFalse(analyzer.canAnalyze(code));

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        assertTrue(analyzer.canAnalyze(code));
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }
  }

  @Test
  public void testCanAnalyzeRecordOperator() throws IOException {
    setupFile(FILE_NAME_OPERATOR_TEST);
    assertFalse(analyzer.canAnalyze(code));

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        assertTrue(analyzer.canAnalyze(code));
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }
  }

  @Test
  public void testAnalyse() throws IOException {
    setupFile(FILE_NAME);

    int index = 0;
    int[] lines = {10, 11, 19, 20, 42, 48, 58, 69, 75, 89};
    String[] names = {
      "bShowTrackerClick",
      "getFunction",
      "myProcedure",
      "setSomething",
      "TDemo.getFunction",
      "TDemo.bShowTrackerClick",
      "TMyClass.myProcedure",
      "TMyClass.setSomething",
      "StandAloneProcedure",
      "StandAloneFunction"
    };

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        analyzer.analyze(code, results);
        assertNotNull(results.getActiveFunction());
        assertEquals(names[index], results.getActiveFunction().getRealName());
        assertEquals(lines[index], results.getActiveFunction().getLine());
        // assertEquals(body[index],
        // results.getActiveFunction().hasBody() );
        ++index;
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }
  }

  @Test
  public void testAnalyseMessageFunction() throws IOException {
    setupFile(FILE_NAME_MESSAGE_TEST);

    results.setActiveClass(new DelphiClass("TWithMessageFunction"));

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        analyzer.analyze(code, results);
        assertNotNull(results.getActiveFunction());

        assertThat(results.getActiveFunction().getRealName(), endsWith("CNCommand"));
        assertThat(results.getActiveFunction().isMessage(), is(true));
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }
  }

  @Test
  public void testAnalyseVirtualFunction() throws IOException {
    setupFile(FILE_NAME_VIRTUAL_TEST);

    results.setActiveClass(new DelphiClass("TWithVirtualFunction"));

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    int count = 0;
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        analyzer.analyze(code, results);
        assertNotNull(results.getActiveFunction());
        count++;
        assertThat("counting " + count, results.getActiveFunction().isVirtual(), is(true));
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }
  }

  @Test
  public void testAnalyseRecordOperator() throws IOException {
    setupFile(FILE_NAME_OPERATOR_TEST);

    results.setActiveClass(new DelphiClass("GenericA"));

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        analyzer.analyze(code, results);
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }

    assertThat("activeFunction", results.getActiveFunction(), notNullValue());
    assertThat(
        "function real name",
        results.getActiveFunction().getRealName(),
        containsString("Implicit"));
  }

  @Test
  public void testAnalyseParameters() throws IOException {
    setupFile(FILE_NAME_PARAMETERS_TEST);
    results.setActiveFunction(new DelphiFunction("myProcedure"));

    CodeNode<Tree> startNode = code.getCurrentCodeNode();
    code.setCurrentNode(advanceToFunction.execute(startNode.getNode()));
    assertTrue(startNode.isValid());

    analyzer.analyze(code, results);

    ArgumentInterface[] expectedArgs = {
      new DelphiArgument("x", "real"),
      new DelphiArgument("y", "integer"),
      new DelphiArgument("z", "integer"),
      new DelphiArgument("q", FunctionAnalyzer.UNTYPED_PARAMETER_NAME)
    };

    FunctionInterface function = results.getActiveFunction();
    ArgumentInterface[] arguments = function.getArguments();

    assertEquals(expectedArgs.length, arguments.length);
    for (int i = 0; i < expectedArgs.length; ++i) {
      assertEquals(expectedArgs[i], arguments[i]);
    }
  }
}
