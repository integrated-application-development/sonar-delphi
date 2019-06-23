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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceToNodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class IncludeAnalyzerTest {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/analyzer/IncludeAnalyzer.pas";

  private IncludeAnalyzer analyzer;
  private CodeAnalysisResults results;
  private CodeTree code;
  private AdvanceToNodeOperation advanceToUses;
  private AdvanceToNodeOperation advanceToImpl;

  @Before
  public void init() {
    analyzer = new IncludeAnalyzer();
    results = new CodeAnalysisResults();
    results.setActiveUnit(new DelphiUnit("test"));

    File file = DelphiUtils.getResource(FILE_NAME);
    ASTTree ast = new DelphiAST(file);
    code = new CodeTree(new CodeNode<>(ast), new CodeNode<>(ast.getChild(0)));
    advanceToUses = new AdvanceToNodeOperation(LexerMetrics.USES);
    advanceToImpl = new AdvanceToNodeOperation(LexerMetrics.PROCEDURE);
  }

  @Test
  public void testAnalyze() {
    code.setCurrentNode(advanceToUses.execute(code.getCurrentCodeNode().getNode()));
    analyzer.analyze(code, results);

    UnitInterface unit = results.getActiveUnit();
    String[] includes = unit.getIncludes();
    String[] expected = {
        "Types", "SysUtils", "System.Classes", "System.Generics.Collections", "System.Rtti"};

    Arrays.sort(includes);
    Arrays.sort(expected);
    for (String s : includes) {
      System.out.print("'" + s + "' ");
    }
    assertArrayEquals(expected, includes);
  }

  @Test
  public void testCanAnalyze() {
    code.setCurrentNode(advanceToUses.execute(code.getCurrentCodeNode().getNode()));
    assertTrue(analyzer.canAnalyze(code));

    code.setCurrentNode(advanceToImpl.execute(code.getCurrentCodeNode().getNode()));
    assertFalse(analyzer.canAnalyze(code));
  }
}
