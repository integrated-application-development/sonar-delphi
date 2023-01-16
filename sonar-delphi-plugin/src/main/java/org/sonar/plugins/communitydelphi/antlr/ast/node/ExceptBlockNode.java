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

import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ExceptBlockNode extends DelphiNode {
  private List<ExceptItemNode> handlers;

  public ExceptBlockNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  public StatementListNode getStatementList() {
    if (isBareExcept()) {
      return (StatementListNode) jjtGetChild(0);
    }
    return null;
  }

  public List<ExceptItemNode> getHandlers() {
    if (handlers == null) {
      handlers = findChildrenOfType(ExceptItemNode.class);
    }
    return handlers;
  }

  @Nullable
  public ElseBlockNode getElseBlock() {
    return getFirstChildOfType(ElseBlockNode.class);
  }

  public boolean isBareExcept() {
    return jjtGetChild(0) instanceof StatementListNode;
  }

  public boolean hasHandlers() {
    return !getHandlers().isEmpty();
  }
}
