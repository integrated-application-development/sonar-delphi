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
package au.com.integradev.delphi.type.parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import au.com.integradev.delphi.type.generic.TypeSpecializationContextImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

class ParameterTest {
  private static final Type TYPE_PARAMETER = TypeParameterTypeImpl.create("T");
  private static final Type TYPE_ARGUMENT = TypeFactory.untypedType();

  @Test
  void testIntrinsicParametersCannotBeSpecialized() {
    Parameter parameter = IntrinsicParameter.create(TYPE_PARAMETER);
    assertThat(parameter.specialize(createSpecializationContext()).getType())
        .isEqualTo(TYPE_PARAMETER);
  }

  @Test
  void testFormalParametersCanBeSpecialized() {
    FormalParameterData data = mock(FormalParameterData.class);
    when(data.getImage()).thenReturn("Foo");
    when(data.getType()).thenReturn(TYPE_PARAMETER);

    Parameter parameter = FormalParameter.create(data);
    assertThat(parameter.specialize(createSpecializationContext()).getType())
        .isEqualTo(TYPE_ARGUMENT);
  }

  @Test
  void testEquals() {
    FormalParameterData data = mock(FormalParameterData.class);
    when(data.getImage()).thenReturn("_");
    when(data.getType()).thenReturn(TYPE_PARAMETER);

    Parameter formalParameter = FormalParameter.create(data);
    Parameter intrinsicParameter = IntrinsicParameter.create(TYPE_PARAMETER);

    assertThat(formalParameter)
        .isEqualTo(formalParameter)
        .isNotEqualTo(new Object())
        .isNotEqualTo(null)
        .isEqualTo(intrinsicParameter)
        .isEqualByComparingTo(intrinsicParameter)
        .hasSameHashCodeAs(intrinsicParameter);
  }

  private static TypeSpecializationContext createSpecializationContext() {
    TypedDeclaration typedDeclaration = mock(TypedDeclaration.class);
    when(typedDeclaration.getType()).thenReturn(TYPE_PARAMETER);

    GenerifiableDeclaration generifiableDeclaration = mock(GenerifiableDeclaration.class);
    when(generifiableDeclaration.isGeneric()).thenReturn(true);
    when(generifiableDeclaration.getTypeParameters()).thenReturn(List.of(typedDeclaration));

    return new TypeSpecializationContextImpl(generifiableDeclaration, List.of(TYPE_ARGUMENT));
  }
}
