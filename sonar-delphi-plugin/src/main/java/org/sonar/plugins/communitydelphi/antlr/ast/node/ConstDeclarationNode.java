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
package org.sonar.plugins.communitydelphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.Typed;

public final class ConstDeclarationNode extends DelphiNode implements Typed, Visibility {
  public ConstDeclarationNode(Token token) {
    super(token);
  }

  public ConstDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public NameDeclarationNode getNameDeclarationNode() {
    return (NameDeclarationNode) jjtGetChild(0);
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  public TypeNode getTypeNode() {
    Node typeNode = jjtGetChild(2);
    return (typeNode instanceof TypeNode) ? (TypeNode) typeNode : null;
  }

  @Override
  @NotNull
  public Type getType() {
    TypeNode typeNode = getTypeNode();
    if (typeNode != null) {
      return typeNode.getType();
    }
    return getExpression().getType();
  }

  @Override
  public VisibilityType getVisibility() {
    Node parent = jjtGetParent();
    if (parent instanceof ConstSectionNode) {
      return ((ConstSectionNode) parent).getVisibility();
    }
    return VisibilityType.PUBLIC;
  }
}
