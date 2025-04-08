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
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.symbol.EnumeratorOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

public final class ForInStatementNodeImpl extends ForStatementNodeImpl
    implements ForInStatementNode {
  private EnumeratorOccurrence enumeratorOccurrence;

  public ForInStatementNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @SuppressWarnings("removal")
  @Override
  @Nullable
  public RoutineNameDeclaration getGetEnumeratorDeclaration() {
    if (getEnumeratorOccurrence() == null) {
      return null;
    }
    return (RoutineNameDeclaration)
        getEnumeratorOccurrence().getGetEnumerator().getNameDeclaration();
  }

  @SuppressWarnings("removal")
  @Override
  @Nullable
  public RoutineNameDeclaration getMoveNextDeclaration() {
    if (getEnumeratorOccurrence() == null) {
      return null;
    }
    return (RoutineNameDeclaration) getEnumeratorOccurrence().getMoveNext().getNameDeclaration();
  }

  @SuppressWarnings("removal")
  @Override
  @Nullable
  public PropertyNameDeclaration getCurrentDeclaration() {
    if (getEnumeratorOccurrence() == null) {
      return null;
    }
    return (PropertyNameDeclaration) getEnumeratorOccurrence().getCurrent().getNameDeclaration();
  }

  @Override
  public ExpressionNode getEnumerable() {
    return (ExpressionNode) getChild(2);
  }

  @Override
  public StatementNode getStatement() {
    return (StatementNode) getChild(4);
  }

  @Override
  @Nullable
  public EnumeratorOccurrence getEnumeratorOccurrence() {
    return enumeratorOccurrence;
  }

  public void setEnumeratorOccurrence(EnumeratorOccurrence enumeratorOccurrence) {
    this.enumeratorOccurrence = enumeratorOccurrence;
  }
}
