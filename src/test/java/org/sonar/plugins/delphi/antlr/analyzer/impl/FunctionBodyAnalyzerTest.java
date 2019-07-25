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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceToNodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.debug.FileTestsCommon;

public class FunctionBodyAnalyzerTest extends FileTestsCommon {

  private static final String FILE_NAME =
      "/org/sonar/plugins/delphi/metrics/FunctionMetricsTest.pas";
  private static final String FILE_NAME_OPERATOR_TEST =
      "/org/sonar/plugins/delphi/metrics/FunctionOperatorTest.pas";
  private static final String FILE_NAME_LIST_UTILS =
      "/org/sonar/plugins/delphi/metrics/ListUtils.pas";

  private static final Tree EMPTY_NODE = new CommonTree(new CommonToken(0, "nil"));
  private static final Tree BEGIN_NODE =
      new CommonTree(new CommonToken(LexerMetrics.BEGIN.toMetrics(), "begin"));

  private FunctionBodyAnalyzer analyzer;
  private CodeAnalysisResults results;
  private CodeTree codeTree;

  private ASTTree ast;

  @Before
  public void setup() {
    ast = mock(ASTTree.class);

    results = new CodeAnalysisResults();
    analyzer = new FunctionBodyAnalyzer(results);
    codeTree = new CodeTree(new CodeNode<>(ast), new CodeNode<>(EMPTY_NODE));
  }

  private void setupFile(String fileName) throws IOException {
    loadFile(fileName);

    results.setActiveUnit(new DelphiUnit("test"));
    ast = new DelphiAST(testFile);
    codeTree = new CodeTree(new CodeNode<>(ast), new CodeNode<>(ast.getChild(0)));

    CodeAnalysisCacheResults.resetCache();
  }

  @Test
  public void testConstructor() {
    try {
      new FunctionBodyAnalyzer(null);
      fail("No exception was caught");
    } catch (IllegalArgumentException e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }

  @Test
  public void testCanAnalyze() {
    assertFalse(analyzer.canAnalyze(codeTree));

    results.setActiveFunction(new DelphiFunction("testFunction"));
    assertFalse(analyzer.canAnalyze(codeTree));

    codeTree.setCurrentNode(new CodeNode<>(BEGIN_NODE));
    assertTrue(analyzer.canAnalyze(codeTree));
  }

  @Test
  public void testCaptureFunctionBodyLine() throws IOException {
    setupFile(FILE_NAME);

    results.setActiveClass(new DelphiClass("TDemo"));

    FunctionInterface function = findFunction("getFunction");
    assertThat(function, notNullValue());
    assertThat(function.hasBody(), is(true));
    assertThat(function.getBodyLine(), is(42));
  }

  private FunctionInterface findFunction(String functionName) {
    DelphiFunction activeFunction = new DelphiFunction(functionName);
    final AdvanceToNodeOperation advanceToImplementationSection =
        new AdvanceToNodeOperation(Collections.singletonList(LexerMetrics.IMPLEMENTATION));
    final AdvanceToNodeOperation advanceToFunctionName =
        new AdvanceToNodeOperation(Collections.singletonList(LexerMetrics.FUNCTION_NAME));
    final AdvanceToNodeOperation advanceToFunctionBody =
        new AdvanceToNodeOperation(Collections.singletonList(LexerMetrics.FUNCTION_BODY));

    CodeNode<Tree> initialNode = codeTree.getCurrentCodeNode();
    try {
      codeTree.setCurrentNode(
          advanceToImplementationSection.execute(codeTree.getCurrentCodeNode().getNode()));

      CodeNode<Tree> currentNode = codeTree.getCurrentCodeNode();
      while (currentNode != null) {
        try {
          CodeNode<Tree> functionNode =
              advanceToFunctionName.execute(codeTree.getCurrentCodeNode().getNode());
          codeTree.setCurrentNode(functionNode);
          final String currentFunctionName =
              functionNode.getNode().getChild(functionNode.getNode().getChildCount() - 1).getText();
          if (currentFunctionName.equalsIgnoreCase(functionName)) {
            results.setActiveFunction(activeFunction);
            codeTree.setCurrentNode(
                advanceToFunctionBody.execute(codeTree.getCurrentCodeNode().getNode()));
            analyzer.analyze(codeTree, results);
            return activeFunction;
          }
        } catch (IllegalStateException e) {
          currentNode = null;
        }
      }
    } finally {
      codeTree.setCurrentNode(initialNode);
    }
    return null;
  }

  @Test
  public void testCaptureFunctionBodyLineRecordOperator() throws IOException {
    setupFile(FILE_NAME_OPERATOR_TEST);

    results.setActiveClass(new DelphiClass("GenericA"));

    FunctionInterface function = findFunction("Implicit");
    assertThat(function, notNullValue());
    assertThat(function.hasBody(), is(true));
    assertThat(function.getBodyLine(), is(18));
  }

  @Test
  public void testListUtils() throws IOException {
    setupFile(FILE_NAME_LIST_UTILS);

    results.setActiveClass(new DelphiClass("TListUtils"));

    FunctionInterface functionSingleStatement = findFunction("SingleStatement");
    assertThat(functionSingleStatement, notNullValue());
    assertThat(functionSingleStatement.hasBody(), is(true));
    assertThat(functionSingleStatement.getBodyLine(), is(63));
    assertThat(functionSingleStatement.getStatements(), hasSize(0));

    FunctionInterface function = findFunction("AddAll1");
    assertThat(function, notNullValue());
    assertThat(function.hasBody(), is(true));
    assertThat(function.getBodyLine(), is(24));
    assertThat(function.getStatements(), hasSize(1));
  }
}
