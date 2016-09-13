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

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceToNodeOperation;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.NodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.ArgumentInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiArgument;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

public class FunctionParametersAnalyzerTest {

  private static final Tree EMPTY_NODE = new CommonTree(new CommonToken(0));
  private static final Tree PARAMETERS_NODE = new CommonTree(new CommonToken(LexerMetrics.FUNCTION_ARGS.toMetrics()));
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/syntax/FunctionParametersAnalyzerTest.pas";
  private static final String FILE_NAME_OPERATOR_TEST = "/org/sonar/plugins/delphi/metrics/FunctionOperatorTest.pas";

  private CodeTree code;
  private CodeAnalysisResults results;
  private FunctionParametersAnalyzer analyzer;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setup() {
    analyzer = new FunctionParametersAnalyzer();
    results = new CodeAnalysisResults();
  }

  @Test
  public void canAnalyzeTest() {
    code = new CodeTree(null, null);
    code.setCurrentNode(new CodeNode<Tree>(EMPTY_NODE));
    assertEquals(false, analyzer.canAnalyze(code));

    code.setCurrentNode(new CodeNode<Tree>(PARAMETERS_NODE));
    assertEquals(true, analyzer.canAnalyze(code));
  }

  @Test
  public void doAnalyzeTest() throws IOException, RecognitionException {
    results.setActiveFunction(new DelphiFunction("myProcedure"));

    File testFile = DelphiUtils.getResource(TEST_FILE);
    ASTTree ast = new DelphiAST(testFile);
    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));

    NodeOperation operation = new AdvanceToNodeOperation(LexerMetrics.FUNCTION_ARGS);
    CodeNode<Tree> startNode = operation.execute(ast.getChild(0));
    code.setCurrentNode(startNode);

    assertEquals(true, startNode.isValid());
    assertEquals(LexerMetrics.FUNCTION_ARGS.toMetrics(), startNode.getNode().getType());

    analyzer.analyze(code, results);

    ArgumentInterface expectedArgs[] = {new DelphiArgument("x", "real"), new DelphiArgument("y", "integer"),
      new DelphiArgument("z", "integer"),
      new DelphiArgument("q", FunctionParametersAnalyzer.UNTYPED_PARAMETER_NAME)};

    FunctionInterface function = results.getActiveFunction();
    ArgumentInterface arguments[] = function.getArguments();

    assertEquals(expectedArgs.length, arguments.length);
    for (int i = 0; i < expectedArgs.length; ++i) {
      assertEquals(expectedArgs[i], arguments[i]);
    }
  }

  @Test
  public void throwExceptionWhenActiveFunctionIsNull() throws IOException, RecognitionException {
    expectedException.equals(IllegalArgumentException.class);
    expectedException.expectMessage(containsString("activeFunction cannot be null"));

    results.setActiveFunction(null);

    File testFile = DelphiUtils.getResource(TEST_FILE);
    ASTTree ast = new DelphiAST(testFile);
    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));

    NodeOperation operation = new AdvanceToNodeOperation(LexerMetrics.FUNCTION_ARGS);
    CodeNode<Tree> startNode = operation.execute(ast.getChild(0));
    code.setCurrentNode(startNode);

    assertEquals(true, startNode.isValid());
    assertEquals(LexerMetrics.FUNCTION_ARGS.toMetrics(), startNode.getNode().getType());

    analyzer.analyze(code, results);
  }
}
