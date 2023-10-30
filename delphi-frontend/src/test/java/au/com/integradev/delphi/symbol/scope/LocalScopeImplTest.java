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
package au.com.integradev.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.VariableNameDeclarationImpl;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.LocalScope;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class LocalScopeImplTest {
  @Test
  void testOnlyVariableDeclarationsAllowed() {
    LocalScopeImpl scope = new LocalScopeImpl();

    VariableNameDeclaration variable =
        VariableNameDeclarationImpl.parameter(
            "Foo", TypeFactory.unknownType(), DelphiScope.unknownScope());

    scope.addDeclaration(variable);

    SymbolicNode symbolicNode = SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
    TypeNameDeclaration type =
        new TypeNameDeclarationImpl(symbolicNode, TypeFactory.unknownType(), "Bar");

    assertThatThrownBy(() -> scope.addDeclaration(type))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testToString() {
    LocalScope scope = new LocalScopeImpl();
    assertThat(scope).hasToString("<LocalScope>");
  }
}
