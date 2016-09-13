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
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VisibilityAnalyzerTest {

  private VisibilityAnalyzer analyzer;
  private CodeAnalysisResults results;
  private CodeTree code;

  @Before
  public void init() throws IOException, RecognitionException {
    code = mock(CodeTree.class);
    analyzer = new VisibilityAnalyzer();
    results = new CodeAnalysisResults();
    results.setActiveUnit(new DelphiUnit("test"));
  }

  @Test
  public void analyzeTest() {
    LexerMetrics[] metrics = {LexerMetrics.PRIVATE, LexerMetrics.PUBLIC, LexerMetrics.PUBLISHED,
      LexerMetrics.PROTECTED};
    for (LexerMetrics metric : metrics) {
      when(code.getCurrentCodeNode()).thenReturn(
        new CodeNode<Tree>(new CommonTree(new CommonToken(metric.toMetrics(), "token"))));
      analyzer.analyze(code, results);
      assertEquals(metric, results.getParseVisibility());
    }
  }

  @Test
  public void canAnalyzeTest() {
    when(code.getCurrentCodeNode()).thenReturn(
      new CodeNode<Tree>(new CommonTree(new CommonToken(LexerMetrics.PRIVATE.toMetrics(), "private"))));
    assertTrue(analyzer.canAnalyze(code));
    when(code.getCurrentCodeNode()).thenReturn(
      new CodeNode<Tree>(new CommonTree(new CommonToken(LexerMetrics.PUBLIC.toMetrics(), "public"))));
    assertTrue(analyzer.canAnalyze(code));
    when(code.getCurrentCodeNode()).thenReturn(
      new CodeNode<Tree>(new CommonTree(new CommonToken(LexerMetrics.PUBLISHED.toMetrics(), "published"))));
    assertTrue(analyzer.canAnalyze(code));
    when(code.getCurrentCodeNode()).thenReturn(
      new CodeNode<Tree>(new CommonTree(new CommonToken(LexerMetrics.PROTECTED.toMetrics(), "protected"))));
    assertTrue(analyzer.canAnalyze(code));
    when(code.getCurrentCodeNode()).thenReturn(
      new CodeNode<Tree>(new CommonTree(new CommonToken(LexerMetrics.IMPLEMENTATION.toMetrics(), "impl"))));
    assertFalse(analyzer.canAnalyze(code));
  }

}
