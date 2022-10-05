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
package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiUnresolvedType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class TypeReferenceNode extends TypeNode implements Qualifiable {
  public TypeReferenceNode(Token token) {
    super(token);
  }

  public TypeReferenceNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  public DelphiNameDeclaration getTypeDeclaration() {
    return getNameNode().getLastName().getNameDeclaration();
  }

  public NameReferenceNode getNameNode() {
    return ((NameReferenceNode) jjtGetChild(0));
  }

  @Override
  @NotNull
  public Type createType() {
    DelphiNameDeclaration declaration = getTypeDeclaration();
    if (declaration instanceof Typed) {
      return ((Typed) declaration).getType();
    }
    return DelphiUnresolvedType.referenceTo(getNameNode().getImage());
  }

  @Override
  public QualifiedName getQualifiedName() {
    return getNameNode().getQualifiedName();
  }
}
