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
import java.util.stream.Stream;
import org.antlr.runtime.Token;

/**
 * Statement Lists are effectively implicit compound statements that Delphi uses in a few places.
 * The StatementListNode is not itself a statement.
 *
 * <p>In terms of file position, Statement Lists are entirely abstract, meaning they have an
 * imaginary token at their root and can also be empty. In light of this, StatementListNodes will
 * always return their parents file position.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code try {statementList} except {statementList} end}
 *   <li>{@code repeat {statementList} until {expression}}
 * </ul>
 */
public final class StatementListNode extends DelphiNode {
  private List<StatementNode> statements;
  private List<StatementNode> descendantStatements;

  public StatementListNode(Token token) {
    super(token);
  }

  public StatementListNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean isEmpty() {
    return getStatements().isEmpty();
  }

  public List<StatementNode> getStatements() {
    if (statements == null) {
      statements = findChildrenOfType(StatementNode.class);
    }
    return statements;
  }

  public List<StatementNode> getDescendantStatements() {
    if (descendantStatements == null) {
      descendantStatements = findDescendantsOfType(StatementNode.class);
    }
    return descendantStatements;
  }

  public Stream<StatementNode> statementStream() {
    return getStatements().stream();
  }

  public Stream<StatementNode> descendantStatementStream() {
    return getDescendantStatements().stream();
  }

  @Override
  public int getBeginLine() {
    return jjtGetParent().getBeginLine();
  }

  @Override
  public int getBeginColumn() {
    return jjtGetParent().getBeginColumn();
  }

  @Override
  public int getEndLine() {
    return jjtGetParent().getEndLine();
  }

  @Override
  public int getEndColumn() {
    return jjtGetParent().getEndColumn();
  }
}
