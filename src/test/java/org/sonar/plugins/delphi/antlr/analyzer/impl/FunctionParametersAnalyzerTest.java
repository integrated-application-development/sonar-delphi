/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
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

public class FunctionParametersAnalyzerTest {

  private static final Tree EMPTY_NODE = new CommonTree(new CommonToken(0));
  private static final Tree PARAMETERS_NODE = new CommonTree(new CommonToken(LexerMetrics.FUNCTION_ARGS.toMetrics()));
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/syntax/FunctionParametersAnalyzerTest.pas";

  private CodeTree code;
  private CodeAnalysisResults results;
  private FunctionParametersAnalyzer analyzer;

  @Before
  public void setup() {
    analyzer = new FunctionParametersAnalyzer();
    results = new CodeAnalysisResults();
    results.setActiveFunction(new DelphiFunction("myProcedure"));
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
    File testFile = DelphiUtils.getResource(TEST_FILE);
    ASTTree ast = new DelphiAST(testFile);
    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));

    NodeOperation operation = new AdvanceToNodeOperation(LexerMetrics.FUNCTION_ARGS);
    CodeNode<Tree> startNode = operation.execute(ast.getChild(0));
    code.setCurrentNode(startNode);

    assertEquals(true, startNode.isValid());
    assertEquals(LexerMetrics.FUNCTION_ARGS.toMetrics(), startNode.getNode().getType());

    analyzer.analyze(code, results);

    ArgumentInterface expectedArgs[] = { new DelphiArgument("x", "real"), new DelphiArgument("y", "integer"),
        new DelphiArgument("z", "integer"), new DelphiArgument("q", FunctionParametersAnalyzer.UNTYPED_PARAMETER_NAME) };

    FunctionInterface function = results.getActiveFunction();
    ArgumentInterface arguments[] = function.getArguments();

    assertEquals(expectedArgs.length, arguments.length);
    for (int i = 0; i < expectedArgs.length; ++i) {
      assertEquals(expectedArgs[i], arguments[i]);
    }

  }
}
