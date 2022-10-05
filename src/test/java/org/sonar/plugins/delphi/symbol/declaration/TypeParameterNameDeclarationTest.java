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
package org.sonar.plugins.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;

import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.CommonDelphiNode;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.generic.DelphiTypeParameterType;

class TypeParameterNameDeclarationTest {
  @Test
  void testEquals() {
    TypeParameterType fooType = DelphiTypeParameterType.create("Foo");

    TypeParameterNameDeclaration foo = createTypeParameter(fooType);
    TypeParameterNameDeclaration otherFoo = createTypeParameter(fooType);
    TypeParameterNameDeclaration fooWithDifferentTypeInstance = createTypeParameter("Foo");
    TypeParameterNameDeclaration bar = createTypeParameter("Bar");

    assertThat(foo)
        .isEqualTo(foo)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(otherFoo)
        .isEqualByComparingTo(otherFoo)
        .hasSameHashCodeAs(otherFoo)
        .isNotEqualTo(fooWithDifferentTypeInstance)
        .doesNotHaveSameHashCodeAs(fooWithDifferentTypeInstance)
        .isNotEqualTo(bar)
        .isNotEqualByComparingTo(bar)
        .doesNotHaveSameHashCodeAs(bar);
  }

  @Test
  void testToString() {
    assertThat(createTypeParameter("T")).hasToString("type parameter <T>");
  }

  private static TypeParameterNameDeclaration createTypeParameter(String name) {
    return createTypeParameter(DelphiTypeParameterType.create(name));
  }

  private static TypeParameterNameDeclaration createTypeParameter(TypeParameterType type) {
    return new TypeParameterNameDeclaration(
        new CommonDelphiNode(new CommonToken(DelphiLexer.TkNameDeclaration, type.getImage())),
        type);
  }
}
