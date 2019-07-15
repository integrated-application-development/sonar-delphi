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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.xpath.Attribute;
import net.sourceforge.pmd.lang.dfa.DataFlowNode;
import net.sourceforge.pmd.lang.symboltable.Scope;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;
import org.w3c.dom.Document;

/**
 * AST node extended with PMD interfaces for analysis PMD analysis
 */
public class DelphiPMDNode extends DelphiNode implements ScopedNode {

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

  /**
   * {@inheritDoc}
   */

  @Override
  public void jjtOpen() {
    // Do nothing
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void jjtClose() {
    // Do nothing
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void jjtSetParent(Node n) {
    // Do nothing
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public Node jjtGetParent() {
    return null;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void jjtAddChild(Node n, int i) {
    // Do nothing
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public Node jjtGetChild(int i) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void jjtSetChildIndex(int var1) {
    // Do nothing
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public int jjtGetChildIndex() {
    return 0;
  }


  /**
   * {@inheritDoc}
   */

  @Override
  public int jjtGetNumChildren() {
    return 0;
  }

  public void jjtAccept(DelphiParserVisitor visitor, Object data) {
    visitor.visit(this, data);
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
  public int jjtGetId() {
    return 0;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public Document getAsDocument() {
    return null;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public Object getUserData() {
    return null;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String getImage() {
    return "";
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void setImage(String image) {
    // Do nothing
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasImageEqualTo(String image) {
    return false;
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


  /**
   * {@inheritDoc}
   */
  @Override
  public DataFlowNode getDataFlowNode() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDataFlowNode(DataFlowNode dataFlowNode) {
    // Do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isFindBoundary() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node getNthParent(int n) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getFirstParentOfType(Class<T> parentType) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> List<T> getParentsOfType(Class<T> parentType) {
    return Collections.emptyList();
  }

  @Override
  @SafeVarargs
  public final <T> T getFirstParentOfAnyType(Class<? extends T>... classes) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> List<T> findChildrenOfType(Class<T> childType) {
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> List<T> findDescendantsOfType(Class<T> targetType) {
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void findDescendantsOfType(Class<T> targetType, List<T> results,
      boolean crossFindBoundaries) {
    // Do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getFirstChildOfType(Class<T> childType) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getFirstDescendantOfType(Class<T> descendantType) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> boolean hasDescendantOfType(Class<T> type) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<? extends Node> findChildNodesWithXPath(String xpathString) {
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasDescendantMatchingXPath(String xpathString) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUserData(Object userData) {
    // Do nothing
  }

  @Override
  public Scope getScope() {
    return null;
  }

  // since 6.0.0
  @Override
  public void remove() {
    // Do nothing
  }


  @Override
  public void removeChildAtIndex(int var1) {
    // Do nothing
  }

  @Override
  public String getXPathNodeName() {
    return null;
  }

  @Override
  public Iterator<Attribute> getXPathAttributesIterator() {
    return null;
  }

  public DelphiPMDNode prevNode() {
    if (parent == null || childIndex == 0) {
      return null;
    }

    return (DelphiPMDNode) parent.getChild(childIndex - 1);
  }

  public DelphiPMDNode nextNode() {
    if (parent == null || parent.getChildCount() == childIndex + 1) {
      return null;
    }

    return (DelphiPMDNode) parent.getChild(childIndex + 1);
  }

  public DelphiPMDNode findNextSiblingOfType(int type) {
    if (parent != null) {
      for (int i = childIndex; i < parent.getChildCount(); ++i) {
        Tree child = parent.getChild(i);

        if (child.getType() == type) {
          return (DelphiPMDNode) parent.getChild(i);
        }
      }
    }
    return null;
  }
}
