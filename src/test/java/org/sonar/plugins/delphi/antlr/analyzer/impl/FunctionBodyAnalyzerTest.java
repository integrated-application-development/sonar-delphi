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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;

public class FunctionBodyAnalyzerTest {

  private static final Tree EMPTY_NODE = new CommonTree(new CommonToken(0, "nil"));
  private static final Tree BEGIN_NODE = new CommonTree(new CommonToken(LexerMetrics.BEGIN.toMetrics(), "begin"));

  FunctionBodyAnalyzer analyzer;
  CodeAnalysisResults results;
  CodeTree codeTree;

  @Before
  public void setup() {
    ASTTree ast = mock(ASTTree.class);

    results = new CodeAnalysisResults();
    analyzer = new FunctionBodyAnalyzer(results);
    codeTree = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(EMPTY_NODE));
  }

  @Test
  public void constructorTest() {
    try {
      new FunctionBodyAnalyzer(null);
      fail("No exception was caught");
    } catch (IllegalArgumentException e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }

  @Test
  public void canAnalyzeTest() {
    assertEquals(false, analyzer.canAnalyze(codeTree));

    results.setActiveFunction(new DelphiFunction("testFunction"));
    assertEquals(false, analyzer.canAnalyze(codeTree));

    codeTree.setCurrentNode(new CodeNode<Tree>(BEGIN_NODE));
    assertEquals(true, analyzer.canAnalyze(codeTree));
  }

  @Test
  public void doAnalyzeTest() {

  }

}
