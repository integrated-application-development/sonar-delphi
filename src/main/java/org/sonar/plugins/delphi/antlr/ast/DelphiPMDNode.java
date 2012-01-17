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
package org.sonar.plugins.delphi.antlr.ast;

import net.sourceforge.pmd.ast.CompilationUnit;
import net.sourceforge.pmd.ast.JavaNode;
import net.sourceforge.pmd.ast.JavaParserVisitor;
import net.sourceforge.pmd.ast.Node;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;

/**
 * AST node extended with PMD interfaces for analysys PMD analysys
 */
public class DelphiPMDNode extends DelphiNode implements JavaNode, CompilationUnit {

  /**
   * C-tor
   * 
   * @param payload
   *          Token
   * @param tree
   *          AST Tree
   */
  public DelphiPMDNode(Token payload, ASTTree tree) {
    super(payload, tree);
  }

  /**
   * C-tor, used in DelphiPMD to safely cast from CommonTree to DelphiPMDNode
   * 
   * @param node
   *          CommonTree node
   */
  public DelphiPMDNode(CommonTree node) {
    super(node.getToken());
    this.children = node.getChildren();
    this.parent = (CommonTree) node.getParent();
    this.childIndex = node.getChildIndex();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void jjtOpen() { // unused
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void jjtClose() { // unused
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void jjtSetParent(Node n) { // unused

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node jjtGetParent() { // unused
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void jjtAddChild(Node n, int i) { // unused
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node jjtGetChild(int i) { // unused
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int jjtGetNumChildren() { // unused
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public Object jjtAccept(DelphiParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object jjtAccept(JavaParserVisitor visitor, Object data) {
    return jjtAccept((DelphiParserVisitor) visitor, data);
  }

}
