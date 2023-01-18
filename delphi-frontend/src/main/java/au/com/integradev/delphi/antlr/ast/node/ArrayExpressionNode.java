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
import au.com.integradev.delphi.type.ArrayOption;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import java.util.List;
import java.util.Set;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;

public final class ArrayExpressionNode extends ExpressionNode {
  private String image;

  public ArrayExpressionNode(Token token) {
    super(token);
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
      StringBuilder imageBuilder = new StringBuilder();
      imageBuilder.append("(");
      for (ExpressionNode element : getElements()) {
        imageBuilder.append(element.getImage());
      }
      imageBuilder.append(")");
      image = imageBuilder.toString();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    Type elementType = DelphiType.unknownType();
    List<ExpressionNode> elements = getElements();
    if (!elements.isEmpty()) {
      elementType = elements.get(0).getType();
    }
    return getTypeFactory().array(null, elementType, Set.of(ArrayOption.FIXED));
  }
}
