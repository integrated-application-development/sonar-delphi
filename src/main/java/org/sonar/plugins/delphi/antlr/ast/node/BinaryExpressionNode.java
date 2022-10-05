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
package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.operator.BinaryOperator;
import org.sonar.plugins.delphi.symbol.resolve.ExpressionTypeResolver;
import org.sonar.plugins.delphi.type.Type;

public final class BinaryExpressionNode extends ExpressionNode {
  private String image;
  private BinaryOperator operator;

  public BinaryExpressionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getLeft() {
    return (ExpressionNode) jjtGetChild(0);
  }

  public ExpressionNode getRight() {
    return (ExpressionNode) jjtGetChild(1);
  }

  public BinaryOperator getOperator() {
    if (operator == null) {
      operator = BinaryOperator.from(jjtGetId());
    }
    return operator;
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getLeft().getImage() + " " + getToken().getImage() + " " + getRight().getImage();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    return new ExpressionTypeResolver(getTypeFactory()).resolve(this);
  }
}
