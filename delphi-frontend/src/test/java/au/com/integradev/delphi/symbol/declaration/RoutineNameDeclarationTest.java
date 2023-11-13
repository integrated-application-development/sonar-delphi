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
package au.com.integradev.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class RoutineNameDeclarationTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();
  private static final SymbolicNode LOCATION =
      SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
  private static final String FULLY_QUALIFIED_NAME = "Foo.Bar";
  private static final Type RETURN_TYPE = TypeFactory.voidType();
  private static final Set<RoutineDirective> DIRECTIVES = Set.of(RoutineDirective.VIRTUAL);
  private static final boolean IS_CLASS_INVOCABLE = false;
  private static final boolean IS_CALLABLE = true;
  private static final RoutineKind KIND = RoutineKind.PROCEDURE;
  private static final ProceduralType ROUTINE_TYPE =
      ((TypeFactoryImpl) FACTORY).routine(List.of(mock(Parameter.class)), RETURN_TYPE);
  private static final TypeNameDeclaration TYPE_NAME_DECLARATION =
      new TypeNameDeclarationImpl(
          SymbolicNode.imaginary("Baz", DelphiScope.unknownScope()),
          TypeFactory.unknownType(),
          "Flarp.Baz");
  private static final VisibilityType VISIBILITY = VisibilityType.PUBLIC;
  private static final List<TypedDeclaration> TYPE_PARAMETERS =
      List.of(mock(TypedDeclaration.class));

  private static final RoutineNameDeclaration ROUTINE =
      new RoutineNameDeclarationImpl(
          LOCATION,
          FULLY_QUALIFIED_NAME,
          RETURN_TYPE,
          DIRECTIVES,
          IS_CLASS_INVOCABLE,
          IS_CALLABLE,
          KIND,
          ROUTINE_TYPE,
          TYPE_NAME_DECLARATION,
          VISIBILITY,
          TYPE_PARAMETERS);

  @Test
  void testEquals() {
    RoutineNameDeclaration equals =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclarationImpl forwardDeclaration =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);
    forwardDeclaration.setIsForwardDeclaration();

    RoutineNameDeclarationImpl implementationDeclaration =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);
    implementationDeclaration.setIsImplementationDeclaration();

    RoutineNameDeclaration differentLocation =
        new RoutineNameDeclarationImpl(
            SymbolicNode.imaginary("Baz", DelphiScope.unknownScope()),
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclaration differentFullyQualifiedName =
        new RoutineNameDeclarationImpl(
            LOCATION,
            "Flarp",
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclaration differentReturnType =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            TypeFactory.untypedType(),
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclaration differentDirectives =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            Collections.emptySet(),
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclaration differentIsClassInvocable =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            true,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclaration differentIsCallable =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            false,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclaration differentRoutineType =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ((TypeFactoryImpl) FACTORY).routine(Collections.emptyList(), TypeFactory.untypedType()),
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    RoutineNameDeclaration differentTypeParameters =
        new RoutineNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            ROUTINE_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            Collections.emptyList());

    assertThat(ROUTINE)
        .isEqualTo(ROUTINE)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(equals)
        .hasSameHashCodeAs(equals)
        .isEqualByComparingTo(equals)
        .isNotEqualTo(forwardDeclaration)
        .doesNotHaveSameHashCodeAs(forwardDeclaration)
        .isNotEqualByComparingTo(forwardDeclaration)
        .isNotEqualTo(implementationDeclaration)
        .doesNotHaveSameHashCodeAs(implementationDeclaration)
        .isNotEqualByComparingTo(implementationDeclaration)
        .isNotEqualTo(differentLocation)
        .doesNotHaveSameHashCodeAs(differentLocation)
        .isNotEqualByComparingTo(differentLocation)
        .isNotEqualTo(differentFullyQualifiedName)
        .doesNotHaveSameHashCodeAs(differentFullyQualifiedName)
        .isNotEqualByComparingTo(differentFullyQualifiedName)
        .isNotEqualTo(differentReturnType)
        .doesNotHaveSameHashCodeAs(differentReturnType)
        .isNotEqualByComparingTo(differentReturnType)
        .isNotEqualTo(differentDirectives)
        .doesNotHaveSameHashCodeAs(differentDirectives)
        .isNotEqualByComparingTo(differentDirectives)
        .isNotEqualTo(differentIsClassInvocable)
        .doesNotHaveSameHashCodeAs(differentIsClassInvocable)
        .isNotEqualByComparingTo(differentIsClassInvocable)
        .isNotEqualTo(differentIsCallable)
        .doesNotHaveSameHashCodeAs(differentIsCallable)
        .isNotEqualByComparingTo(differentIsCallable)
        .isNotEqualTo(differentRoutineType)
        .doesNotHaveSameHashCodeAs(differentRoutineType)
        .isNotEqualByComparingTo(differentRoutineType)
        .isNotEqualTo(differentTypeParameters)
        .doesNotHaveSameHashCodeAs(differentTypeParameters)
        .isNotEqualByComparingTo(differentTypeParameters);
  }

  @Test
  void testToString() {
    assertThat(ROUTINE).hasToString("Routine Bar, line 0, params = 1 <<unknown unit>>");
  }
}
