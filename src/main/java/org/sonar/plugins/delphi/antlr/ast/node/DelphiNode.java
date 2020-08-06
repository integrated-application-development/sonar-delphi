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
package org.sonar.plugins.delphi.antlr.ast.node;

import static org.apache.commons.lang3.ArrayUtils.isArrayIndexValid;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiTreeAdaptor;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;

/** AST node extended with PMD interfaces for PMD analysis */
public abstract class DelphiNode extends AbstractNode implements ScopedNode, IndexedNode {

  private final DelphiToken token;
  private DelphiScope scope;

  /**
   * All nodes must implement this constructor. Used to create a node with a concrete token. Also
   * used by {@link DelphiTreeAdaptor#dupNode}}
   *
   * @param token Token to create the node with
   */
  protected DelphiNode(Token token) {
    super(token == null ? Token.INVALID_TOKEN_TYPE : token.getType());
    this.token = new DelphiToken(token);
  }

  /**
   * Nodes created from imaginary tokens must implement this constructor.
   *
   * @param tokenType Token type
   */
  protected DelphiNode(int tokenType) {
    this(new CommonToken(tokenType, DelphiParser.tokenNames[tokenType]));
  }

  /**
   * Allow a DelphiParserVisitor to visit this node and do some work
   *
   * @param <T> The visitor's data type
   * @param visitor The DelphiParserVisitor
   * @param data Data related to this visit
   * @return Data related to this visit
   */
  public abstract <T> T accept(DelphiParserVisitor<T> visitor, T data);

  /**
   * Allow a DelphiParserVisitor to visit all of the children of this node
   *
   * @param <T> The visitor's data type
   * @param visitor The DelphiParserVisitor
   * @param data Data related to this visit
   * @return Data related to this visit
   */
  public final <T> T childrenAccept(DelphiParserVisitor<T> visitor, T data) {
    if (children != null) {
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        ((DelphiNode) jjtGetChild(i)).accept(visitor, data);
      }
    }
    return data;
  }

  @Override
  public int getBeginLine() {
    return jjtGetFirstToken().getBeginLine();
  }

  @Override
  public int getBeginColumn() {
    return jjtGetFirstToken().getBeginColumn();
  }

  @Override
  public int getEndLine() {
    return jjtGetLastToken().getEndLine();
  }

  @Override
  public int getEndColumn() {
    return jjtGetLastToken().getEndColumn();
  }

  @Override
  public String getImage() {
    return token.getImage();
  }

  @Override
  public boolean hasImageEqualTo(final String image) {
    return this.getImage().equalsIgnoreCase(image);
  }

  public void setScope(DelphiScope scope) {
    this.scope = scope;
  }

  @Override
  @NotNull
  public DelphiScope getScope() {
    if (scope == null) {
      if (parent != null) {
        return ((DelphiNode) parent).getScope();
      }
      return unknownScope();
    }
    return scope;
  }

  @Override
  public String getXPathNodeName() {
    return getClass().getSimpleName();
  }

  @Override
  public Node jjtGetChild(int index) {
    if (isArrayIndexValid(this.children, index)) {
      return super.jjtGetChild(index);
    }
    return null;
  }

  @Override
  public void jjtAddChild(Node child, int index) {
    super.jjtAddChild(child, index);
    child.jjtSetParent(this);
  }

  public void jjtAddChild(@NotNull Node node) {
    DelphiNode child = (DelphiNode) node;

    if (child.getToken().isNil()) {
      boolean sameChildren = this.children != null && Arrays.equals(this.children, child.children);
      Preconditions.checkArgument(!sameChildren, "Cannot add child list to itself!");

      int count = child.jjtGetNumChildren();
      for (int i = 0; i < count; ++i) {
        jjtAddChild(child.jjtGetChild(i), jjtGetNumChildren());
      }
    } else {
      jjtAddChild(child, jjtGetNumChildren());
    }
  }

  @Override
  public DelphiToken jjtGetFirstToken() {
    if (this.firstToken == null) {
      this.firstToken = findFirstToken();
    }
    return (DelphiToken) this.firstToken;
  }

  @Override
  public DelphiToken jjtGetLastToken() {
    if (this.lastToken == null) {
      this.lastToken = findLastToken();
    }
    return (DelphiToken) this.lastToken;
  }

  private GenericToken findFirstToken() {
    DelphiToken result = this.token;
    if (result.isImaginary()) {
      int index = result.getIndex();

      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        DelphiToken childToken = ((DelphiNode) jjtGetChild(i)).jjtGetFirstToken();
        int childIndex = childToken.getIndex();
        if (!childToken.isImaginary() && childIndex < index) {
          result = childToken;
        }
      }
    }
    return result;
  }

  private GenericToken findLastToken() {
    DelphiToken result = this.jjtGetFirstToken();
    int index = result.getIndex();

    for (int i = 0; i < jjtGetNumChildren(); ++i) {
      DelphiToken childToken = ((DelphiNode) jjtGetChild(i)).jjtGetFirstToken();
      int childIndex = childToken.getIndex();
      if (!childToken.isImaginary() && childIndex > index) {
        result = childToken;
      }
    }
    return result;
  }

  public Node nextNode() {
    if (parent == null || parent.jjtGetNumChildren() == childIndex + 1) {
      return null;
    }

    return parent.jjtGetChild(childIndex + 1);
  }

  public Node getFirstChildWithId(int nodeId) {
    for (int i = 0; i < jjtGetNumChildren(); ++i) {
      Node child = jjtGetChild(i);
      if (child.jjtGetId() == nodeId) {
        return child;
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
  public int jjtGetChildId(int index) {
    Node child = jjtGetChild(index);
    return (child == null) ? -1 : child.jjtGetId();
  }

  public DelphiToken getToken() {
    return token;
  }

  @Override
  public int getTokenIndex() {
    return jjtGetFirstToken().getIndex();
  }

  /**
   * Gets the AST Tree associated with this node
   *
   * @return AST Tree
   */
  public final DelphiAST getASTTree() {
    if (this instanceof DelphiAST) {
      return (DelphiAST) this;
    }

    return getFirstParentOfType(DelphiAST.class);
  }

  /**
   * Return the name of the unit where this node is located
   *
   * @return Unit name
   */
  public String findUnitName() {
    return getASTTree().getFileHeader().getName();
  }

  /**
   * Gets any comments nested inside of this node
   *
   * @return List of comment tokens
   */
  public List<DelphiToken> getComments() {
    return getASTTree().getCommentsInsideNode(this);
  }

  /**
   * Returns whether the node is within the interface section of the file
   *
   * @return true if the node is in the interface section
   */
  public boolean isInterfaceSection() {
    return getFirstParentOfType(InterfaceSectionNode.class) != null;
  }

  /**
   * Returns whether the node is within the implementation section of the file
   *
   * @return true if the node is in the implementation section
   */
  public boolean isImplementationSection() {
    return getFirstParentOfType(ImplementationSectionNode.class) != null;
  }
}
