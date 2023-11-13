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
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ArrayIndicesNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstArrayElementTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class ArrayTypeNodeImpl extends TypeNodeImpl implements ArrayTypeNode {
  public ArrayTypeNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public TypeNode getElementTypeNode() {
    return (TypeNode) getChild(1);
  }

  @Override
  @Nullable
  public ArrayIndicesNode getArrayIndices() {
    DelphiNode indices = getChild(2);
    return (indices instanceof ArrayIndicesNode) ? (ArrayIndicesNode) indices : null;
  }

  @Override
  @Nonnull
  protected Type createType() {
    DelphiNode parent = getParent();
    String image = null;
    if (parent instanceof TypeDeclarationNode) {
      image = ((TypeDeclarationNode) parent).fullyQualifiedName();
    }

    Type elementType = getElementTypeNode().getType();
    ArrayIndicesNode indices = getArrayIndices();
    int indicesSize = (indices == null) ? 0 : indices.getTypeNodes().size();

    Set<ArrayOption> options = EnumSet.noneOf(ArrayOption.class);
    if (indicesSize > 0) {
      options.add(ArrayOption.FIXED);
    } else if (parent instanceof FormalParameterNode) {
      options.add(ArrayOption.OPEN);
    } else {
      options.add(ArrayOption.DYNAMIC);
    }

    if (getElementTypeNode() instanceof ConstArrayElementTypeNode) {
      options.add(ArrayOption.ARRAY_OF_CONST);
    }

    if (indicesSize > 1) {
      return ((TypeFactoryImpl) getTypeFactory())
          .multiDimensionalArray(image, elementType, indicesSize, options);
    }

    return ((TypeFactoryImpl) getTypeFactory()).array(image, elementType, options);
  }
}
