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

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;

public final class ArrayConstructorNodeImpl extends ExpressionNodeImpl
    implements ArrayConstructorNode {
  private String image;

  public ArrayConstructorNodeImpl(Token token) {
    super(token);
  }

  public ArrayConstructorNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<ExpressionNode> getElements() {
    return findChildrenOfType(ExpressionNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image =
          "["
              + getChildren().stream()
                  .skip(1)
                  .limit(getChildren().size() - 2)
                  .map(DelphiNode::getImage)
                  .collect(Collectors.joining(", "))
              + "]";
    }
    return image;
  }

  @Override
  @Nonnull
  protected Type createType() {
    return getTypeFactory()
        .arrayConstructor(
            getElements().stream()
                .map(ExpressionNode::getType)
                .map(type -> type.isProcedural() ? ((ProceduralType) type).returnType() : type)
                .collect(Collectors.toUnmodifiableList()));
  }
}
