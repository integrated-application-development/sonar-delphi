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
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class ExceptItemNodeImpl extends DelphiNodeImpl implements ExceptItemNode {
  public ExceptItemNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nonnull
  @Override
  public Type getType() {
    return getExceptionType().getType();
  }

  @Nullable
  @Override
  public NameDeclarationNode getExceptionName() {
    return hasExceptionName() ? (NameDeclarationNode) getChild(0) : null;
  }

  @Nonnull
  @Override
  public TypeReferenceNode getExceptionType() {
    return (TypeReferenceNode) getChild(hasExceptionName() ? 1 : 0);
  }

  @Nullable
  @Override
  public StatementNode getStatement() {
    DelphiNode statement = getChild(hasExceptionName() ? 3 : 2);
    return statement instanceof StatementNode ? (StatementNode) statement : null;
  }

  private boolean hasExceptionName() {
    return getChild(0) instanceof NameDeclarationNode;
  }
}
