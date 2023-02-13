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
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;

public final class StatementListNodeImpl extends DelphiNodeImpl implements StatementListNode {
  private List<StatementNode> statements;
  private List<StatementNode> descendantStatements;

  public StatementListNodeImpl(Token token) {
    super(token);
  }

  public StatementListNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean isEmpty() {
    return getStatements().isEmpty();
  }

  @Override
  public List<StatementNode> getStatements() {
    if (statements == null) {
      statements = findChildrenOfType(StatementNode.class);
    }
    return statements;
  }

  @Override
  public List<StatementNode> getDescendantStatements() {
    if (descendantStatements == null) {
      descendantStatements = findDescendantsOfType(StatementNode.class);
    }
    return descendantStatements;
  }

  @Override
  public Stream<StatementNode> statementStream() {
    return getStatements().stream();
  }

  @Override
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
