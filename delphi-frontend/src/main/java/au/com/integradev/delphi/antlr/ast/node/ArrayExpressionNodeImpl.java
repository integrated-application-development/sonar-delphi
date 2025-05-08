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
import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArrayExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public final class ArrayExpressionNodeImpl extends ExpressionNodeImpl
    implements ArrayExpressionNode {
  private String image;

  public ArrayExpressionNodeImpl(Token token) {
    super(token);
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
  @Nonnull
  protected Type createType() {
    int nestingLevel = 0;
    DelphiNode parent = this;
    do {
      ++nestingLevel;
      parent = parent.getParent();
    } while (parent instanceof ArrayExpressionNode);

    Type elementType = TypeFactory.unknownType();
    if (parent instanceof Typed && !(parent instanceof TypeDeclarationNode)) {
      elementType = ((Typed) parent).getType();
      for (int i = 0; i < nestingLevel; ++i) {
        if (!(elementType instanceof CollectionType)) {
          elementType = TypeFactory.unknownType();
          break;
        }
        elementType = ((CollectionType) elementType).elementType();
      }
    }

    if (elementType.isUnknown()) {
      List<ExpressionNode> elements = getElements();
      if (!elements.isEmpty()) {
        elementType = elements.get(0).getType();
      }
    }

    return ((TypeFactoryImpl) getTypeFactory()).array(null, elementType, Set.of(ArrayOption.FIXED));
  }
}
