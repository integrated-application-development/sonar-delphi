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
package org.sonar.plugins.delphi.antlr.ast;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;

/**
 * AST node extended with PMD interfaces for analysis PMD analysis
 */
public class DelphiPMDNode extends DelphiNode implements AntlrPmdNodeInterface {

  /**
   * C-tor
   *
   * @param payload Token
   * @param tree AST Tree
   */
  public DelphiPMDNode(Token payload, DelphiAST tree) {
    super(payload, tree);
  }

  /**
   * C-tor, used in DelphiPMD to safely cast from CommonTree to DelphiPMDNode
   *
   * @param node CommonTree node
   * @param tree AST Tree
   */
  public DelphiPMDNode(CommonTree node, DelphiAST tree) {
    super(node.getToken(), tree);
    this.children = node.getChildren();
    this.parent = (CommonTree) node.getParent();
    this.childIndex = node.getChildIndex();
  }

  public void jjtAccept(DelphiParserVisitor visitor, Object data) {
    visitor.visit(this, data);
  }

  public List<Tree> findAllChildren(int[] types) {
    List<Tree> children = new ArrayList<>();
    for (int type : types) {
      children.addAll(internalfindAllChildren(this, type));
    }
    return children;
  }

  public List<Tree> findAllChildren(int type) {
    return internalfindAllChildren(this, type);
  }

  private List<Tree> internalfindAllChildren(Tree node, int type) {
    List<Tree> result = new ArrayList<>();
    for (int i = 0; i < node.getChildCount(); i++) {
      Tree child = node.getChild(i);
      if (child.getType() == type) {
        result.add(child);
      } else {
        result.addAll(internalfindAllChildren(child, type));
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getBeginLine() {
    int line = getLine();
    if (getChildCount() > 0) {
      DelphiPMDNode firstChild = new DelphiPMDNode((CommonTree) getChild(0), getASTTree());

      line = Math.min(line, firstChild.getBeginLine());
    }

    return line;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getBeginColumn() {
    return getCharPositionInLine();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getEndLine() {
    if (getChildCount() > 0) {
      DelphiPMDNode lastChild = new DelphiPMDNode((CommonTree) getChild(getChildCount() - 1),
          getASTTree());

      return lastChild.getEndLine();
    }

    return getLine();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getEndColumn() {
    if (getChildCount() > 0) {
      DelphiPMDNode lastChild = new DelphiPMDNode((CommonTree) getChild(getChildCount() - 1),
          getASTTree());

      return lastChild.getEndColumn();
    }

    int maxColumn = getASTTree().getFileSourceLine(getEndLine()).length();

    if (isNil()) {
      return maxColumn;
    }
    
    int calcColumn = getBeginColumn() + getText().length();
    return Math.min(maxColumn, calcColumn);
  }

  public DelphiPMDNode prevNode() {
    if (parent == null || childIndex == 0) {
      return null;
    }

    return new DelphiPMDNode((CommonTree) parent.getChild(childIndex - 1), getASTTree());
  }

  public DelphiPMDNode nextNode() {
    if (parent == null || parent.getChildCount() == childIndex + 1) {
      return null;
    }

    return new DelphiPMDNode((CommonTree) parent.getChild(childIndex + 1), getASTTree());
  }

  public DelphiPMDNode findNextSiblingOfType(int type) {
    if (parent != null) {
      for (int i = childIndex; i < parent.getChildCount(); ++i) {
        Tree child = parent.getChild(i);

        if (child.getType() == type) {
          return new DelphiPMDNode((CommonTree) parent.getChild(i), getASTTree());
        }
      }
    }
    return null;
  }
}
