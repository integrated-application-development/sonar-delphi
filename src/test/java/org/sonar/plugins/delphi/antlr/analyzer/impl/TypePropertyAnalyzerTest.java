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
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class TypePropertyAnalyzerTest {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/PropertyTest.pas";
  private TypePropertyAnalyzer analyzer;
  private ASTTree ast;
  private CodeAnalysisResults results;
  private CodeTree code;
  private AdvanceToNodeOperation advanceToOp;

  @Before
  public void init() throws IOException, RecognitionException {
    analyzer = new TypePropertyAnalyzer();
    results = new CodeAnalysisResults();
    results.setActiveUnit(new DelphiUnit("test"));

    File file = DelphiUtils.getResource(FILE_NAME);
    ast = new DelphiAST(file);
    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));
    advanceToOp = new AdvanceToNodeOperation(LexerMetrics.PROPERTY);
  }

  @Test
  public void analyzeTest() {
    ClassInterface clazz = new DelphiClass("test");
    results.setActiveClass(clazz);

    code.setCurrentNode(advanceToOp.execute(code.getCurrentCodeNode().getNode()));
    analyzer.analyze(code, results);

    ClassPropertyInterface properties[] = clazz.getProperties();
    assertEquals(1, properties.length);
    assertEquals("isfoo", properties[0].getName());
    assertEquals(clazz, properties[0].getParent());
    assertEquals("foo", properties[0].getReadFunction().getName());
    assertEquals("foo", properties[0].getWriteFunction().getName());
    assertEquals("boolean", properties[0].getType());

    code.setCurrentNode(advanceToOp.execute(code.getCurrentCodeNode().getNode()));
    analyzer.analyze(code, results);

    properties = clazz.getProperties();
    assertEquals(2, properties.length);
    assertEquals("isbar", properties[1].getName());
    assertEquals(clazz, properties[1].getParent());
    assertEquals("bar", properties[1].getReadFunction().getName());
    assertEquals("bar", properties[1].getWriteFunction().getName());
    assertEquals("boolean", properties[1].getType());
  }

  @Test
  public void canAnalyzeTest() {
    assertFalse(analyzer.canAnalyze(code));
    code.setCurrentNode(advanceToOp.execute(code.getCurrentCodeNode().getNode()));
    assertTrue(analyzer.canAnalyze(code));
  }
}
