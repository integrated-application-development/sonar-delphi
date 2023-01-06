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
package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility.VisibilityType;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;

class MethodScopeTest {
  @Test
  void testToString() {
    MethodScope methodScope = new MethodScope();
    assertThat(methodScope).hasToString("<MethodScope>");

    SymbolicNode symbolicNode = SymbolicNode.imaginary("Foo", DelphiScope.unknownScope());
    methodScope.setMethodNameDeclaration(
        new MethodNameDeclaration(
            symbolicNode,
            "Unit.Foo",
            DelphiType.unknownType(),
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
