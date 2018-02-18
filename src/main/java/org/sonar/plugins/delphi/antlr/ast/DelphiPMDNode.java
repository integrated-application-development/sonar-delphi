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

import net.sourceforge.pmd.lang.dfa.DataFlowNode;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitor;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.symboltable.Scope;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.jaxen.JaxenException;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node extended with PMD interfaces for analysis PMD analysis
 */
public class DelphiPMDNode extends DelphiNode implements JavaNode {

  /**
   * C-tor
   * 
   * @param payload Token
   * @param tree AST Tree
   */
  public DelphiPMDNode(Token payload, ASTTree tree) {
    super(payload, tree);
  }

  /**
   * C-tor, used in DelphiPMD to safely cast from CommonTree to DelphiPMDNode
   * 
   * @param node CommonTree node
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
  public void jjtOpen() {
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void jjtClose() {
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void jjtSetParent(Node n) {

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
  public int jjtGetNumChildren() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  private Object jjtAccept(DelphiParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object jjtAccept(JavaParserVisitor visitor, Object data) {
    return jjtAccept((DelphiParserVisitor) visitor, data);
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
   {@inheritDoc}
   */
  @Override
  public int jjtGetId() { return 0; };

  /**
   {@inheritDoc}
   */
  @Override
  public Document getAsDocument() { return null; };

  /**
   {@inheritDoc}
  */
  @Override
  public Object getUserData() { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public String getImage() { return ""; };

  /**
   {@inheritDoc}
   */
  @Override
  public void setImage(String image) {};

  /**
   {@inheritDoc}
   */
  @Override
  public boolean hasImageEqualTo(String image) { return false; };

  /**
   {@inheritDoc}
   */
  @Override
  public int getBeginLine() { return 0; };

  /**
   {@inheritDoc}
   */
  @Override
  public int getBeginColumn() { return 0; };

  /**
   {@inheritDoc}
   */
  @Override
  public int getEndLine() { return 0; };

  /**
   {@inheritDoc}
   */
  @Override
  public int getEndColumn() { return 0; };

  /**
   {@inheritDoc}
   */
  @Override
  public DataFlowNode getDataFlowNode() { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public void setDataFlowNode(DataFlowNode dataFlowNode) {};

  /**
   {@inheritDoc}
   */
  @Override
  public boolean isFindBoundary() { return false; };

  /**
   {@inheritDoc}
   */
  @Override
  public Node getNthParent(int n) { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public <T> T getFirstParentOfType(Class<T> parentType) { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public<T> List<T> getParentsOfType(Class<T> parentType) { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public <T> List<T> findChildrenOfType(Class<T> childType) { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public <T> List<T> findDescendantsOfType(Class<T> targetType) { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public <T> void findDescendantsOfType(Class<T> targetType, List<T> results, boolean crossFindBoundaries) { }

  /**
   {@inheritDoc}
   */
  @Override
  public <T> T getFirstChildOfType(Class<T> childType) { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public <T> T getFirstDescendantOfType(Class<T> descendantType) { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public <T> boolean hasDescendantOfType(Class<T> type) { return false; };

  /**
   {@inheritDoc}
   */
  @Override
  public List<? extends Node> findChildNodesWithXPath(String xpathString) throws JaxenException { return null; };

  /**
   {@inheritDoc}
   */
  @Override
  public boolean hasDescendantMatchingXPath(String xpathString) { return false; };

  /**
   {@inheritDoc}
   */
  @Override
  public void setUserData(Object userData) {};

  @Override
  public Object childrenAccept(JavaParserVisitor visitor, Object data) { return null; };

  @Override
  public Scope getScope() { return null; };

  @Override
  public void setScope(Scope scope) {};

}
