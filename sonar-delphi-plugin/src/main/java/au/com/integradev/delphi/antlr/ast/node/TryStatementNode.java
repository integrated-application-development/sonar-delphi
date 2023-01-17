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
import org.antlr.runtime.Token;

public final class TryStatementNode extends StatementNode {
  private ExceptBlockNode exceptBlock;
  private FinallyBlockNode finallyBlock;

  public TryStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public StatementListNode getStatementList() {
    return (StatementListNode) jjtGetChild(0);
  }

  public boolean hasExceptBlock() {
    return jjtGetChild(1) instanceof ExceptBlockNode;
  }

  public boolean hasFinallyBlock() {
    return jjtGetChild(1) instanceof FinallyBlockNode;
  }

  public ExceptBlockNode getExpectBlock() {
    if (exceptBlock == null && hasExceptBlock()) {
      exceptBlock = (ExceptBlockNode) jjtGetChild(1);
    }
    return exceptBlock;
  }

  public FinallyBlockNode getFinallyBlock() {
    if (finallyBlock == null && hasFinallyBlock()) {
      finallyBlock = (FinallyBlockNode) jjtGetChild(1);
    }
    return finallyBlock;
  }
}
