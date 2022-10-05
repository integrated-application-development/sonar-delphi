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
package org.sonar.plugins.delphi.type.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;

class TypeSpecializationContextTest {
  @Test
  void testInvalidDeclarationProducesEmptyContext() {
    TypeSpecializationContext context =
        new TypeSpecializationContext(
            mock(NameDeclaration.class), List.of(DelphiType.untypedType()));

    assertThat(context)
        .isEqualTo(
            new TypeSpecializationContext(mock(NameDeclaration.class), Collections.emptyList()));
  }

  @Test
  void testParameterArgumentMismatchProducesInvalidContext() {
    TypedDeclaration typedDeclaration = mock(TypedDeclaration.class);
    when(typedDeclaration.getType()).thenReturn(DelphiTypeParameterType.create("T"));

    GenerifiableDeclaration generifiableDeclaration = mock(GenerifiableDeclaration.class);
    when(generifiableDeclaration.isGeneric()).thenReturn(true);
    when(generifiableDeclaration.getTypeParameters()).thenReturn(List.of(typedDeclaration));

    TypeSpecializationContext invalidContext =
        new TypeSpecializationContext(
            generifiableDeclaration, List.of(DelphiType.untypedType(), DelphiType.untypedType()));

    assertThat(invalidContext.hasSignatureMismatch()).isTrue();
    assertThat(invalidContext)
        .isEqualTo(new TypeSpecializationContext(generifiableDeclaration, Collections.emptyList()));
  }

  @Test
  void testEquals() {
    TypeSpecializationContext context =
        new TypeSpecializationContext(mock(NameDeclaration.class), Collections.emptyList());
    TypeSpecializationContext other =
        new TypeSpecializationContext(mock(NameDeclaration.class), Collections.emptyList());

    assertThat(context)
        .isEqualTo(context)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(other)
        .hasSameHashCodeAs(other);
  }
}
