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
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;

/** AST node extended with PMD interfaces for PMD analysis */
public class DelphiNode extends CommonTree implements AntlrPmdNodeInterface {

  private DelphiAST mainTree;

  /**
   * C-tor
   *
   * @param payload Token
   * @param tree AST Tree
   */
  public DelphiNode(Token payload, DelphiAST tree) {
    super(payload);
    mainTree = tree;
  }

  @Override
  public Tree dupNode() {
    return new DelphiNode(this.token, mainTree);
  }

  public void jjtAccept(DelphiParserVisitor visitor, RuleContext ctx) {
    visitor.visit(this, ctx);
  }

  public List<Tree> findAllChildren(int[] types) {
    List<Tree> children = new ArrayList<>();
    for (int type : types) {
      children.addAll(internalFindAllChildren(this, type));
    }
    return children;
  }

  public List<Tree> findAllChildren(int type) {
    return internalFindAllChildren(this, type);
  }

  private List<Tree> internalFindAllChildren(Tree node, int type) {
    List<Tree> result = new ArrayList<>();
    for (int i = 0; i < node.getChildCount(); i++) {
      Tree child = node.getChild(i);
      if (child.getType() == type) {
        result.add(child);
      } else {
        result.addAll(internalFindAllChildren(child, type));
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public int getBeginLine() {
    int line = getLine();
    if (getChildCount() > 0) {
      DelphiNode firstChild = (DelphiNode) getChild(0);

      line = Math.min(line, firstChild.getBeginLine());
    }

    return line;
  }

  /** {@inheritDoc} */
  @Override
  public int getBeginColumn() {
    return getCharPositionInLine();
  }

  /** {@inheritDoc} */
  @Override
  public int getEndLine() {
    if (getChildCount() > 0) {
      DelphiNode lastChild = (DelphiNode) getChild(getChildCount() - 1);
      return lastChild.getEndLine();
    }

    return getLine();
  }

  /** {@inheritDoc} */
  @Override
  public int getEndColumn() {
    if (getChildCount() > 0) {
      DelphiNode lastChild = (DelphiNode) getChild(getChildCount() - 1);
      return lastChild.getEndColumn();
    }

    int maxColumn = mainTree.getFileSourceLine(getEndLine()).length();

    if (isNil()) {
      return maxColumn;
    }

    int calcColumn = getBeginColumn() + getText().length();
    return Math.min(maxColumn, calcColumn);
  }

  public DelphiNode prevNode() {
    if (parent == null || childIndex == 0) {
      return null;
    }

    return (DelphiNode) parent.getChild(childIndex - 1);
  }

  public DelphiNode nextNode() {
    if (parent == null || parent.getChildCount() == childIndex + 1) {
      return null;
    }

    return (DelphiNode) parent.getChild(childIndex + 1);
  }

  public DelphiNode findNextSiblingOfType(int type) {
    if (parent != null) {
      for (int i = childIndex; i < parent.getChildCount(); ++i) {
        Tree child = parent.getChild(i);

        if (child.getType() == type) {
          return (DelphiNode) parent.getChild(i);
        }
      }
    }
    return null;
  }

  /**
   * Gets child type, or -1 if child does not exist
   *
   * @param index Child index
   * @return Child type, or -1 if child is non-existent
   */
  public int getChildType(int index) {
    if (index > -1 && index < getChildCount()) {
      return getChild(index).getType();
    }
    return -1;
  }

  /**
   * Gets the AST Tree associated with this node
   *
   * @return AST Tree
   */
  public DelphiAST getASTTree() {
    return mainTree;
  }

  /**
   * Gets any comments nested inside of this node
   *
   * @return List of comment tokens
   */
  public List<Token> getComments() {
    return mainTree.getCommentsInsideNode(this);
  }
}
