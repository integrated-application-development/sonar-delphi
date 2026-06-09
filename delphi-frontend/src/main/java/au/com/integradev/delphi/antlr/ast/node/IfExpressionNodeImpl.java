/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
import au.com.integradev.delphi.symbol.resolve.ExpressionTypeResolver;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IfExpressionNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class IfExpressionNodeImpl extends ExpressionNodeImpl implements IfExpressionNode {
  private String image;

  public IfExpressionNodeImpl(Token token) {
    super(token);
  }

  public IfExpressionNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public ExpressionNode getGuardExpression() {
    return (ExpressionNode) getChild(1);
  }

  @Override
  public ExpressionNode getThenExpression() {
    return (ExpressionNode) getChild(3);
  }

  @Override
  public ExpressionNode getElseExpression() {
    return (ExpressionNode) getChild(5);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image =
          "if "
              + getGuardExpression().getImage()
              + " then "
              + getThenExpression().getImage()
              + " else "
              + getElseExpression().getImage();
    }
    return image;
  }

  @Override
  @Nonnull
  protected Type createType() {
    return new ExpressionTypeResolver(getTypeFactory()).resolve(this);
  }
}
