/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class ConstDeclarationNodeImpl extends DelphiNodeImpl implements ConstDeclarationNode {
  public ConstDeclarationNodeImpl(Token token) {
    super(token);
  }

  public ConstDeclarationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public NameDeclarationNode getNameDeclarationNode() {
    return (NameDeclarationNode) getChild(0);
  }

  @Override
  public ExpressionNode getExpression() {
    return (ExpressionNode) getChild(1);
  }

  @Override
  public TypeNode getTypeNode() {
    DelphiNode typeNode = getChild(2);
    return (typeNode instanceof TypeNode) ? (TypeNode) typeNode : null;
  }

  @Override
  public Type getType() {
    TypeNode typeNode = getTypeNode();
    if (typeNode != null) {
      return typeNode.getType();
    }
    return getExpression().getType();
  }

  @Override
  public VisibilityType getVisibility() {
    DelphiNode parent = getParent();
    if (parent instanceof ConstSectionNode) {
      return ((ConstSectionNode) parent).getVisibility();
    }
    return VisibilityType.PUBLIC;
  }
}
