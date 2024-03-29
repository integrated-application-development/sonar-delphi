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
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.WeakAliasTypeNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class WeakAliasTypeNodeImpl extends TypeNodeImpl implements WeakAliasTypeNode {
  public WeakAliasTypeNodeImpl(Token token) {
    super(token);
  }

  public WeakAliasTypeNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public TypeReferenceNode getAliasedTypeNode() {
    return (TypeReferenceNode) getChild(0);
  }

  @Nonnull
  @Override
  protected Type createType() {
    TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) getParent();

    String typeName = typeDeclaration.fullyQualifiedName();
    Type aliasedType = getAliasedTypeNode().getType();

    return getTypeFactory().weakAlias(typeName, aliasedType);
  }
}
