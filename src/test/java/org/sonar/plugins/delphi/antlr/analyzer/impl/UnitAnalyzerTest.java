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
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.IOException;

import static org.junit.Assert.*;

public class UnitAnalyzerTest {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/MetricsTest.pas";

  private UnitAnalyzer analyzer;
  private CodeAnalysisResults results;
  private CodeTree code;
  private CodeNode<ASTTree> astNode;

  @Before
  public void init() throws IllegalStateException, IOException, RecognitionException {
    analyzer = new UnitAnalyzer();
    results = new CodeAnalysisResults();
    astNode = new CodeNode<ASTTree>(new DelphiAST(DelphiUtils.getResource(FILE_NAME)));
    code = new CodeTree(astNode, new CodeNode<Tree>(astNode.getNode().getChild(0)));
  }

  @Test
  public void analyzeTest() {
    analyzer.analyze(code, results);

    UnitInterface unit = new DelphiUnit("DemoForm");
    unit.setPath(DelphiUtils.getResource(FILE_NAME).getAbsolutePath());
    assertEquals(unit, results.getActiveUnit());

    assertEquals(LexerMetrics.PUBLIC, results.getParseVisibility());
  }

  @Test
  public void canAnalyzeTest() {
    code.setCurrentNode(new CodeNode<Tree>(new CommonTree(new CommonToken(LexerMetrics.UNIT.toMetrics(), "token"))));
    assertTrue(analyzer.canAnalyze(code));
    code.setCurrentNode(new CodeNode<Tree>(new CommonTree(
      new CommonToken(LexerMetrics.LIBRARY.toMetrics(), "token"))));
    assertTrue(analyzer.canAnalyze(code));
    code.setCurrentNode(new CodeNode<Tree>(new CommonTree(new CommonToken(LexerMetrics.IMPLEMENTATION.toMetrics(),
      "token"))));
    assertFalse(analyzer.canAnalyze(code));
  }

}
