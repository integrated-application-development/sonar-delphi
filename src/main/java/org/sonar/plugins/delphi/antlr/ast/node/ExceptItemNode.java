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

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class ExceptItemNode extends DelphiNode implements Typed {

  public ExceptItemNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @NotNull
  @Override
  public Type getType() {
    return getExceptionType().getType();
  }

  @Nullable
  public NameDeclarationNode getExceptionName() {
    return hasExceptionName() ? (NameDeclarationNode) jjtGetChild(0) : null;
  }

  @NotNull
  public TypeReferenceNode getExceptionType() {
    return (TypeReferenceNode) jjtGetChild(hasExceptionName() ? 1 : 0);
  }

  @Nullable
  public StatementNode getStatement() {
    Node statement = jjtGetChild(hasExceptionName() ? 3 : 2);
    return statement instanceof StatementNode ? (StatementNode) statement : null;
  }

  private boolean hasExceptionName() {
    return jjtGetChild(0) instanceof NameDeclarationNode;
  }
}
