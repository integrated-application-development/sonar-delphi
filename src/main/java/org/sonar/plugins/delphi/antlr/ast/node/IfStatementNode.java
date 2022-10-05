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
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class IfStatementNode extends StatementNode {
  public IfStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getGuardExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  private CommonDelphiNode getElseToken() {
    return (CommonDelphiNode) getFirstChildWithId(DelphiLexer.ELSE);
  }

  public boolean hasElseBranch() {
    return getElseToken() != null;
  }

  @Nullable
  public StatementNode getThenStatement() {
    Node node = jjtGetChild(2);
    if (node instanceof StatementNode) {
      return (StatementNode) node;
    }
    return null;
  }

  @Nullable
  public StatementNode getElseStatement() {
    if (hasElseBranch()) {
      return (StatementNode) jjtGetChild(getElseToken().jjtGetChildIndex() + 1);
    }
    return null;
  }
}
