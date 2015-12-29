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

import java.io.File;
import java.io.IOException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceToNodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TypeInheritanceAnalyzerTest {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/FunctionMetricsTest.pas";

  private TypeInheritanceAnalyzer analyzer;
  private ASTTree ast;
  private CodeAnalysisResults results;
  private CodeTree code;
  private AdvanceToNodeOperation advanceToOp;

  @Before
  public void init() throws IOException, RecognitionException {
    analyzer = new TypeInheritanceAnalyzer();
    results = new CodeAnalysisResults();
    results.setActiveUnit(new DelphiUnit("test"));

    File file = DelphiUtils.getResource(FILE_NAME);
    ast = new DelphiAST(file);
    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));
    advanceToOp = new AdvanceToNodeOperation(LexerMetrics.CLASS_PARENTS);
  }

  @Test
  public void analyzeTest() {
    code.setCurrentNode(advanceToOp.execute(code.getCurrentCodeNode().getNode()));

    ClassInterface clazz = new DelphiClass("test");
    results.setActiveClass(clazz);
    analyzer.analyze(code, results);

    ClassInterface parents[] = clazz.getParents();
    assertThat(parents, arrayWithSize(3));
    assertThat(parents, hasItemInArray(hasProperty("name", equalToIgnoringCase("TMyAncestor"))));
    assertThat(parents, hasItemInArray(hasProperty("name", equalToIgnoringCase("TMyClass"))));
    assertThat(parents, hasItemInArray(hasProperty("name", equalToIgnoringCase("TMyElder"))));
  }

  @Test
  public void canAnalyzeTest() {
    assertFalse(analyzer.canAnalyze(code));
    code.setCurrentNode(advanceToOp.execute(code.getCurrentCodeNode().getNode()));
    assertTrue(analyzer.canAnalyze(code));
  }
}
