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
package au.com.integradev.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;

class MethodNameDeclarationTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();
  private static final SymbolicNode LOCATION =
      SymbolicNode.imaginary("Bar", DelphiScope.unknownScope());
  private static final String FULLY_QUALIFIED_NAME = "Foo.Bar";
  private static final Type RETURN_TYPE = TypeFactory.voidType();
  private static final Set<MethodDirective> DIRECTIVES = Set.of(MethodDirective.VIRTUAL);
  private static final boolean IS_CLASS_INVOCABLE = false;
  private static final boolean IS_CALLABLE = true;
  private static final MethodKind KIND = MethodKind.PROCEDURE;
  private static final ProceduralType METHOD_TYPE =
      FACTORY.method(List.of(mock(Parameter.class)), RETURN_TYPE);
  private static final TypeNameDeclaration TYPE_NAME_DECLARATION =
      new TypeNameDeclarationImpl(
          SymbolicNode.imaginary("Baz", DelphiScope.unknownScope()),
          TypeFactory.unknownType(),
          "Flarp.Baz");
  private static final VisibilityType VISIBILITY = VisibilityType.PUBLIC;
  private static final List<TypedDeclaration> TYPE_PARAMETERS =
      List.of(mock(TypedDeclaration.class));

  private static final MethodNameDeclaration METHOD =
      new MethodNameDeclarationImpl(
          LOCATION,
          FULLY_QUALIFIED_NAME,
          RETURN_TYPE,
          DIRECTIVES,
          IS_CLASS_INVOCABLE,
          IS_CALLABLE,
          KIND,
          METHOD_TYPE,
          TYPE_NAME_DECLARATION,
          VISIBILITY,
          TYPE_PARAMETERS);

  @Test
  void testEquals() {
    MethodNameDeclaration equals =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration forwardMethod =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);
    forwardMethod.setIsForwardDeclaration();

    MethodNameDeclaration implementationMethod =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);
    implementationMethod.setIsImplementationDeclaration();

    MethodNameDeclaration differentLocation =
        new MethodNameDeclarationImpl(
            SymbolicNode.imaginary("Baz", DelphiScope.unknownScope()),
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration differentFullyQualifiedName =
        new MethodNameDeclarationImpl(
            LOCATION,
            "Flarp",
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration differentReturnType =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            TypeFactory.untypedType(),
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration differentDirectives =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            Collections.emptySet(),
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration differentIsClassInvocable =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            true,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration differentIsCallable =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            false,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration differentMethodType =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            FACTORY.method(Collections.emptyList(), TypeFactory.untypedType()),
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            TYPE_PARAMETERS);

    MethodNameDeclaration differentTypeParameters =
        new MethodNameDeclarationImpl(
            LOCATION,
            FULLY_QUALIFIED_NAME,
            RETURN_TYPE,
            DIRECTIVES,
            IS_CLASS_INVOCABLE,
            IS_CALLABLE,
            KIND,
            METHOD_TYPE,
            TYPE_NAME_DECLARATION,
            VISIBILITY,
            Collections.emptyList());

    assertThat(METHOD)
        .isEqualTo(METHOD)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object())
        .isEqualTo(equals)
        .hasSameHashCodeAs(equals)
        .isEqualByComparingTo(equals)
        .isNotEqualTo(forwardMethod)
        .doesNotHaveSameHashCodeAs(forwardMethod)
        .isNotEqualByComparingTo(forwardMethod)
        .isNotEqualTo(implementationMethod)
        .doesNotHaveSameHashCodeAs(implementationMethod)
        .isNotEqualByComparingTo(implementationMethod)
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
        .isNotEqualTo(differentMethodType)
        .doesNotHaveSameHashCodeAs(differentMethodType)
        .isNotEqualByComparingTo(differentMethodType)
        .isNotEqualTo(differentTypeParameters)
        .doesNotHaveSameHashCodeAs(differentTypeParameters)
        .isNotEqualByComparingTo(differentTypeParameters);
  }

  @Test
  void testToString() {
    assertThat(METHOD).hasToString("Method Bar, line 0, params = 1 <<unknown unit>>");
  }
}
