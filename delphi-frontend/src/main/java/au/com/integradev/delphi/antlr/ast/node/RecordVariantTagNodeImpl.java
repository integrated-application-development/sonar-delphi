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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantTagNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;

public final class RecordVariantTagNodeImpl extends DelphiNodeImpl
    implements RecordVariantTagNode {
  public RecordVariantTagNodeImpl(Token token) {
    super(token);
  }

  public RecordVariantTagNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Nullable
  @Override
  public NameDeclarationNode getTagName() {
    if (hasTagName()) {
      return (NameDeclarationNode) jjtGetChild(0);
    }
    return null;
  }

  @Override
  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(hasTagName() ? 1 : 0);
  }

  @Nonnull
  @Override
  public Type getType() {
    return getTypeNode().getType();
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private boolean hasTagName() {
    return jjtGetChild(0) instanceof NameDeclarationNode;
  }
}
