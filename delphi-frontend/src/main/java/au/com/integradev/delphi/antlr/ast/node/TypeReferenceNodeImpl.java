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
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.QualifiedName;
import au.com.integradev.delphi.type.DelphiUnresolvedType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Typed;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;

public final class TypeReferenceNodeImpl extends TypeNodeImpl implements TypeReferenceNode {
  public TypeReferenceNodeImpl(Token token) {
    super(token);
  }

  public TypeReferenceNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @Nullable
  public NameDeclaration getTypeDeclaration() {
    return getNameNode().getLastName().getNameDeclaration();
  }

  @Override
  public NameReferenceNode getNameNode() {
    return ((NameReferenceNode) jjtGetChild(0));
  }

  @Override
  @Nonnull
  protected Type createType() {
    NameDeclaration declaration = getTypeDeclaration();
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
