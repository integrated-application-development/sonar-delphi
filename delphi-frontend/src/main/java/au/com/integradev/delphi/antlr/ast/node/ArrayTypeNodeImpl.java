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
import au.com.integradev.delphi.type.factory.ArrayOption;
import org.sonar.plugins.communitydelphi.api.type.Type;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.ArrayIndicesNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstArraySubTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;

public final class ArrayTypeNodeImpl extends TypeNodeImpl implements ArrayTypeNode {
  public ArrayTypeNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public TypeNode getElementTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Override
  @Nullable
  public ArrayIndicesNode getArrayIndices() {
    DelphiNode indices = jjtGetChild(2);
    return (indices instanceof ArrayIndicesNode) ? (ArrayIndicesNode) indices : null;
  }

  @Override
  @Nonnull
  protected Type createType() {
    DelphiNode parent = jjtGetParent();
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

    if (getElementTypeNode() instanceof ConstArraySubTypeNode) {
      options.add(ArrayOption.ARRAY_OF_CONST);
    }

    if (indicesSize > 1) {
      return getTypeFactory().multiDimensionalArray(image, elementType, indicesSize, options);
    }

    return getTypeFactory().array(image, elementType, options);
  }
}
