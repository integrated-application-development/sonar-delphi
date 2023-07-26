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
import au.com.integradev.delphi.symbol.resolve.ExpressionTypeResolver;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class PrimaryExpressionNodeImpl extends ExpressionNodeImpl
    implements PrimaryExpressionNode {
  private String image;

  public PrimaryExpressionNodeImpl(Token token) {
    super(token);
  }

  public PrimaryExpressionNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean isInheritedCall() {
    return getChild(0).getTokenType() == DelphiTokenType.INHERITED;
  }

  @Override
  public boolean isBareInherited() {
    return (getChildren().size() == 1 || !(getChild(1) instanceof NameReferenceNode))
        && isInheritedCall();
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      for (DelphiNode child : getChildren()) {
        imageBuilder.append(child.getImage());
      }
      image = imageBuilder.toString();
    }
    return image;
  }

  @Override
  @Nonnull
  protected Type createType() {
    return new ExpressionTypeResolver(getTypeFactory()).resolve(this);
  }
}
