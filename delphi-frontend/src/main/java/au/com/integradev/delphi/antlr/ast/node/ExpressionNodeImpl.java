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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.LiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.ParenthesizedExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;

public abstract class ExpressionNodeImpl extends DelphiNodeImpl implements ExpressionNode {
  private Type type;

  protected ExpressionNodeImpl(Token token) {
    super(token);
  }

  protected ExpressionNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  @Nonnull
  public Type getType() {
    if (type == null) {
      type = createType();
    }
    return type;
  }

  @Override
  public boolean isLiteral() {
    return extractLiteral() != null;
  }

  @Override
  public boolean isIntegerLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isIntegerLiteral();
  }

  @Override
  public boolean isHexadecimalLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isHexadecimalLiteral();
  }

  @Override
  public boolean isBinaryLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isBinaryLiteral();
  }

  @Override
  public boolean isStringLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isTextLiteral();
  }

  @Override
  public boolean isRealLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isDecimalLiteral();
  }

  @Override
  public boolean isNilLiteral() {
    LiteralNode literal = extractLiteral();
    return literal != null && literal.isNilLiteral();
  }

  @Override
  public boolean isBooleanLiteral() {
    return isTrue() || isFalse();
  }

  @Override
  public boolean isTrue() {
    return isReferenceTo("True");
  }

  @Override
  public boolean isFalse() {
    return isReferenceTo("False");
  }

  @Override
  public boolean isResult() {
    return isReferenceTo("Result");
  }

  @Override
  public boolean isReferenceTo(String image) {
    ExpressionNode expression = skipParentheses();
    if (expression.getChildrenCount() == 1 && expression instanceof PrimaryExpressionNode) {
      DelphiNode child = expression.getChild(0);
      if (child instanceof NameReferenceNode) {
        NameReferenceNode reference = (NameReferenceNode) child;
        NameDeclaration declaration = reference.getLastName().getNameDeclaration();
        return declaration != null && declaration.getImage().equals(image);
      }
    }
    return false;
  }

  @Override
  @Nullable
  public LiteralNode extractLiteral() {
    ExpressionNode expression = skipParentheses();
    if (expression instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expression;
      if (primary.getChildrenCount() == 1 && primary.getChild(0) instanceof LiteralNode) {
        return (LiteralNode) primary.getChild(0);
      }
    }
    return null;
  }

  @Override
  @Nullable
  public NameReferenceNode extractSimpleNameReference() {
    ExpressionNode expression = skipParentheses();
    if (expression instanceof PrimaryExpressionNode && expression.getChildrenCount() == 1) {
      DelphiNode child = expression.getChild(0);
      if (child instanceof NameReferenceNode) {
        NameReferenceNode nameReference = (NameReferenceNode) child;
        if (nameReference.nextName() == null) {
          return nameReference;
        }
      }
    }
    return null;
  }

  @Override
  @Nonnull
  public ExpressionNode skipParentheses() {
    ExpressionNode result = this;
    while (result instanceof ParenthesizedExpressionNode) {
      result = ((ParenthesizedExpressionNode) result).getExpression();
    }
    return result;
  }

  @Override
  @Nonnull
  public ExpressionNode findParentheses() {
    ExpressionNode result = this;
    while (result.getParent() instanceof ParenthesizedExpressionNode) {
      result = (ExpressionNode) result.getParent();
    }
    return result;
  }

  protected abstract Type createType();
}
