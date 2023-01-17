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
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.ProceduralType;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;

public final class ArrayConstructorNode extends ExpressionNode {
  private String image;

  public ArrayConstructorNode(Token token) {
    super(token);
  }

  public ArrayConstructorNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<ExpressionNode> getElements() {
    return findChildrenOfType(ExpressionNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder builder = new StringBuilder();
      builder.append("[");
      for (int i = 0; i < jjtGetNumChildren() - 1; ++i) {
        if (i > 0) {
          builder.append(", ");
        }
        builder.append(jjtGetChild(i).getImage());
      }
      builder.append("]");
      image = builder.toString();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    return getTypeFactory()
        .arrayConstructor(
            getElements().stream()
                .map(ExpressionNode::getType)
                .map(type -> type.isProcedural() ? ((ProceduralType) type).returnType() : type)
                .collect(Collectors.toUnmodifiableList()));
  }
}
