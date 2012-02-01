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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.antlr.runtime.RecognitionException;
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
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.debug.FileTestsCommon;

public class FunctionAnalyzerTest extends FileTestsCommon {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/FunctionMetricsTest.pas";

  private FunctionAnalyzer analyzer;
  private CodeAnalysisResults results;

  private DelphiAST ast;
  private CodeTree code;
  private AdvanceToNodeOperation advanceToFunction;

  @Before
  public void setup() throws IOException, RecognitionException {
    loadFile(FILE_NAME);
    analyzer = new FunctionAnalyzer();
    results = new CodeAnalysisResults();
    results.setActiveUnit(new DelphiUnit("test"));
    ast = new DelphiAST(testFile);
    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));
    advanceToFunction = new AdvanceToNodeOperation(Arrays.asList(LexerMetrics.FUNCTION, LexerMetrics.PROCEDURE, LexerMetrics.CONSTRUCTOR,
        LexerMetrics.DESTRUCTOR));

    CodeAnalysisCacheResults.resetCache();
  }

  @Test
  public void canAnalyzeTest() {
    assertEquals(false, analyzer.canAnalyze(code));

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        ;
        assertEquals(true, analyzer.canAnalyze(code));
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }
  }

  @Test
  public void analyseTest() {
    int index = 0;
    int lines[] = { 10, 11, 19, 20, 42, 48, 58, 69, 75, 89 };
    boolean body[] = { false, false, false, false, false, true, true, false, true, true };
    String names[] = { "bShowTrackerClick", "getFunction", "myProcedure", "setSomething", "TDemo.getFunction", "TDemo.bShowTrackerClick",
        "TMyClass.myProcedure", "TMyClass.setSomething", "StandAloneProcedure", "StandAloneFunction" };

    CodeNode<Tree> currentNode = code.getCurrentCodeNode();
    while (currentNode != null) {
      try {
        code.setCurrentNode(advanceToFunction.execute(code.getCurrentCodeNode().getNode()));
        analyzer.analyze(code, results);
        assertTrue(results.getActiveFunction() != null);
        assertEquals(names[index], results.getActiveFunction().getRealName());
        assertEquals(lines[index], results.getActiveFunction().getLine());
        // assertEquals(body[index], results.getActiveFunction().hasBody() );
        ++index;
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }
  }

}
