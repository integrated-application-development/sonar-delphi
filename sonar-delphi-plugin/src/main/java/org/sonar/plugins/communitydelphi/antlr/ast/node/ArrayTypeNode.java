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
package org.sonar.plugins.communitydelphi.antlr.ast.node;

import java.util.EnumSet;
import java.util.Set;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.type.ArrayOption;
import org.sonar.plugins.communitydelphi.type.Type;

public final class ArrayTypeNode extends TypeNode {
  public ArrayTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @NotNull
  public TypeNode getElementTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Nullable
  public ArrayIndicesNode getArrayIndices() {
    Node indices = jjtGetChild(2);
    return (indices instanceof ArrayIndicesNode) ? (ArrayIndicesNode) indices : null;
  }

  @Override
  @NotNull
  public Type createType() {
    Node parent = jjtGetParent();
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
