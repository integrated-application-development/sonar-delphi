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
package org.sonar.plugins.delphi.antlr.analyzer;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CodeAnalyzedTest {

  private CodeTree code;
  private CommonTree[] nodes;
  private ASTTree ast;

  @Before
  public void init() {
    ast = new DelphiAST();
    nodes = new CommonTree[] {new CommonTree(new CommonToken(0, "a")), new CommonTree(new CommonToken(0, "ab1")),
      new CommonTree(new CommonToken(0, "ab1c1")), new CommonTree(new CommonToken(0, "ab1c2")),
      new CommonTree(new CommonToken(0, "ab2")), new CommonTree(new CommonToken(0, "ab2c1")),
      new CommonTree(new CommonToken(0, "ab2c2")), new CommonTree(new CommonToken(0, "ab2c3"))};

    ast.addChild(nodes[0]);
    nodes[0].addChild(nodes[1]); // a->ab1
    nodes[0].addChild(nodes[4]); // a->ab2

    nodes[1].addChild(nodes[2]); // ab1 -> ab1c1
    nodes[1].addChild(nodes[3]); // ab1 -> ab1c2

    nodes[4].addChild(nodes[5]); // ab2 -> ab2c1
    nodes[4].addChild(nodes[6]); // ab2 -> ab2c2
    nodes[4].addChild(nodes[7]); // ab2 -> ab2c3

    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));
  }

  /*
   * @Test public void advanceNodeTest() { Tree n =
   * code.getCurrentCodeNode().getNode(); int index = 0; do {
   * assertEquals(nodes[index++], n); code.advanceNode(); n =
   * code.getCurrentCodeNode(); } while (n != null);
   * 
   * assertFalse(code.hasValidNode()); }
   */

  @Test
  public void isValidTest() {
    assertTrue(code.getCurrentCodeNode().isValid());
    assertTrue(code.getRootCodeNode().isValid());
    assertEquals(ast, code.getRootCodeNode().getNode());
  }

}
