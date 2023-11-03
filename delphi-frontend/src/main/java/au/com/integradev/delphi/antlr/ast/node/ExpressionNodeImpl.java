/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ParenthesizedExpressionNode;
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
