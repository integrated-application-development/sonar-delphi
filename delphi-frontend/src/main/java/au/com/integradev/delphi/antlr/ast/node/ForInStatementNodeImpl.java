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

import static java.util.Collections.emptyList;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.symbol.resolve.NameResolutionHelper;
import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class ForInStatementNodeImpl extends ForStatementNodeImpl
    implements ForInStatementNode {
  private final Supplier<NameResolutionHelper> nameResolutionHelper =
      Suppliers.memoize(() -> new NameResolutionHelper(getAst().getDelphiFile().getTypeFactory()));

  private final Supplier<MethodNameDeclaration> getEnumeratorDeclaration =
      Suppliers.memoize(
          () ->
              nameResolutionHelper
                  .get()
                  .findMethodMember(this, getEnumerable().getType(), "GetEnumerator", emptyList()));

  private final Supplier<MethodNameDeclaration> moveNextDeclaration =
      Suppliers.memoize(
          () ->
              nameResolutionHelper
                  .get()
                  .findMethodMember(this, getEnumeratorType(), "MoveNext", emptyList()));

  private final Supplier<PropertyNameDeclaration> currentDeclaration =
      Suppliers.memoize(
          () ->
              nameResolutionHelper
                  .get()
                  .findPropertyMember(this, getEnumeratorType(), "Current", emptyList()));

  public ForInStatementNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @Nullable
  public MethodNameDeclaration getGetEnumeratorDeclaration() {
    return getEnumeratorDeclaration.get();
  }

  @Override
  @Nullable
  public MethodNameDeclaration getMoveNextDeclaration() {
    return moveNextDeclaration.get();
  }

  @Override
  @Nullable
  public PropertyNameDeclaration getCurrentDeclaration() {
    return currentDeclaration.get();
  }

  private Type getEnumeratorType() {
    MethodNameDeclaration enumerator = getGetEnumeratorDeclaration();
    if (enumerator != null) {
      return enumerator.getReturnType();
    }
    return TypeFactory.unknownType();
  }

  @Override
  public ExpressionNode getEnumerable() {
    return (ExpressionNode) getChild(2);
  }

  @Override
  public StatementNode getStatement() {
    return (StatementNode) getChild(4);
  }
}
