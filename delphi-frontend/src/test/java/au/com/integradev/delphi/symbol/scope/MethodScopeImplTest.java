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
package au.com.integradev.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclarationImpl;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class MethodScopeImplTest {
  @Test
  void testToString() {
    MethodScopeImpl methodScope = new MethodScopeImpl();
    assertThat(methodScope).hasToString("<MethodScope>");

    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    methodScope.setMethodNameDeclaration(
        new MethodNameDeclarationImpl(
            symbolicNode,
            "Unit.Foo",
            TypeFactory.unknownType(),
            Collections.emptySet(),
            false,
            true,
            MethodKind.FUNCTION,
            mock(ProceduralType.class),
            null,
            VisibilityType.PUBLIC,
            Collections.emptyList()));

    assertThat(methodScope).hasToString("Foo <MethodScope>");
  }
}
