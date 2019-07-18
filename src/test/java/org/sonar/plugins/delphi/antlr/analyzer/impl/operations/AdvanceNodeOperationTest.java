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
package org.sonar.plugins.delphi.antlr.analyzer.impl.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;

import java.io.File;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class AdvanceNodeOperationTest extends OperationsTestsCommon {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/ComplexityMetricsTest.pas";

  public AdvanceNodeOperationTest() {
    super(new AdvanceNodeOperation());
  }

  @Override
  @Before
  public void init() {
    super.init();
  }

  @Test
  public void testExecute() {
    int parsedCount = 0;
    int[] types = {256, 1, 100, 2, 101, 3, 102};

    CodeNode<Tree> codeNode = new CodeNode<>(parent);
    do {
      assertEquals(types[parsedCount++], codeNode.getNode().getType());
      codeNode = operation.execute(codeNode.getNode());
    } while (codeNode.isValid());

    assertEquals(7, parsedCount);
  }

  @Test
  public void testExecuteOnFile() {
    File astFile = DelphiUtils.getResource(FILE_NAME);
    ASTTree ast = new DelphiAST(astFile);
    CodeTree codeTree = new CodeTree(new CodeNode<>(ast), new CodeNode<>(ast.getChild(0)));

    int lastLine = -1;
    CodeNode<Tree> codeNode = codeTree.getCurrentCodeNode();
    do {
      int codeNodeLine = codeNode.getNode().getLine();
      assertThat(lastLine, is(lessThanOrEqualTo(codeNodeLine)));
      lastLine = codeNodeLine;
      codeNode = operation.execute(codeNode.getNode());
    } while (codeNode.isValid());

  }

}
