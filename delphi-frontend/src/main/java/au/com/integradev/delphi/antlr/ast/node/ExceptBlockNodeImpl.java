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
import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ElseBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;

public final class ExceptBlockNodeImpl extends DelphiNodeImpl implements ExceptBlockNode {
  private List<ExceptItemNode> handlers;

  public ExceptBlockNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @Nullable
  public StatementListNode getStatementList() {
    if (isBareExcept()) {
      return (StatementListNode) getChild(0);
    }
    return null;
  }

  @Override
  public List<ExceptItemNode> getHandlers() {
    if (handlers == null) {
      handlers = findChildrenOfType(ExceptItemNode.class);
    }
    return handlers;
  }

  @Override
  @Nullable
  public ElseBlockNode getElseBlock() {
    return getFirstChildOfType(ElseBlockNode.class);
  }

  @Override
  public boolean isBareExcept() {
    return getChild(0) instanceof StatementListNode;
  }

  @Override
  public boolean hasHandlers() {
    return !getHandlers().isEmpty();
  }
}
