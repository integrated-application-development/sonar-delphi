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
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ExceptBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.FinallyBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.TryStatementNode;

public final class TryStatementNodeImpl extends DelphiNodeImpl implements TryStatementNode {
  private ExceptBlockNode exceptBlock;
  private FinallyBlockNode finallyBlock;

  public TryStatementNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public StatementListNode getStatementList() {
    return (StatementListNode) getChild(0);
  }

  @Override
  public boolean hasExceptBlock() {
    return getChild(1) instanceof ExceptBlockNode;
  }

  @Override
  public boolean hasFinallyBlock() {
    return getChild(1) instanceof FinallyBlockNode;
  }

  @SuppressWarnings("removal")
  @Override
  public ExceptBlockNode getExpectBlock() {
    return getExceptBlock();
  }

  @Override
  public ExceptBlockNode getExceptBlock() {
    if (exceptBlock == null && hasExceptBlock()) {
      exceptBlock = (ExceptBlockNode) getChild(1);
    }
    return exceptBlock;
  }

  @Override
  public FinallyBlockNode getFinallyBlock() {
    if (finallyBlock == null && hasFinallyBlock()) {
      finallyBlock = (FinallyBlockNode) getChild(1);
    }
    return finallyBlock;
  }
}
