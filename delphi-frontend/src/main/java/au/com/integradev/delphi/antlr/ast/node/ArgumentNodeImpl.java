/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;

public final class ArgumentNodeImpl extends DelphiNodeImpl implements ArgumentNode {
  public ArgumentNodeImpl(Token token) {
    super(token);
  }

  public ArgumentNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  @Override
  public IdentifierNode getName() {
    return hasName() ? (IdentifierNode) getChild(0) : null;
  }

  @Override
  public ExpressionNode getExpression() {
    return (ExpressionNode) getChild(hasName() ? 1 : 0);
  }

  @Nullable
  @Override
  public ExpressionNode getWidth() {
    return (ExpressionNode) getChild(hasName() ? 2 : 1);
  }

  @Nullable
  @Override
  public ExpressionNode getDecimals() {
    return (ExpressionNode) getChild(hasName() ? 3 : 2);
  }

  private boolean hasName() {
    return getChild(0) instanceof IdentifierNode;
  }

  @Override
  public String getImage() {
    StringBuilder image = new StringBuilder();

    IdentifierNode name = getName();
    if (name != null) {
      image.append(name.getImage()).append(" := ");
    }

    image.append(getExpression().getImage());

    ExpressionNode width = getWidth();
    if (width != null) {
      image.append(":").append(width.getImage());

      ExpressionNode decimals = getDecimals();
      if (decimals != null) {
        image.append(":").append(decimals.getImage());
      }
    }

    return image.toString();
  }
}
