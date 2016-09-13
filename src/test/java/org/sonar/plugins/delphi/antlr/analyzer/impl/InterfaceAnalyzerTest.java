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
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceNodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;

import java.io.IOException;

import static org.junit.Assert.*;

public class InterfaceAnalyzerTest {

  private ASTTree ast;
  private InterfaceAnalyzer analyzer;
  private CodeAnalysisResults results;
  private CodeTree code;
  private AdvanceNodeOperation advanceOp;

  @Before
  public void init() throws IOException, RecognitionException {
    analyzer = new InterfaceAnalyzer();
    results = new CodeAnalysisResults();
    results.setActiveUnit(new DelphiUnit("test"));

    ast = new DelphiAST();
    ast.addChild(new CommonTree(new CommonToken(LexerMetrics.INTERFACE.toMetrics(), "interface")));
    ast.addChild(new CommonTree(new CommonToken(LexerMetrics.IDENT.toMetrics(), "ident")));
    ast.addChild(new CommonTree(new CommonToken(LexerMetrics.IMPLEMENTATION.toMetrics(), "impl")));

    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));
    advanceOp = new AdvanceNodeOperation();
  }

  @Test
  public void analyzeTest() {
    LexerMetrics metrics[] = {LexerMetrics.PUBLIC, LexerMetrics.PUBLIC, LexerMetrics.PRIVATE};
    int i = 0;
    do {
      analyzer.analyze(code, results);
      LexerMetrics visibility = results.getParseVisibility();
      assertEquals(metrics[i++], visibility);
      code.setCurrentNode(advanceOp.execute(code.getCurrentCodeNode().getNode()));
    } while (code.getCurrentCodeNode().isValid());
  }

  @Test
  public void canAnalyzeTest() {
    assertTrue(analyzer.canAnalyze(code));
    code.setCurrentNode(advanceOp.execute(code.getCurrentCodeNode().getNode()));
    assertFalse(analyzer.canAnalyze(code));
    code.setCurrentNode(advanceOp.execute(code.getCurrentCodeNode().getNode()));
    assertTrue(analyzer.canAnalyze(code));
  }

}
