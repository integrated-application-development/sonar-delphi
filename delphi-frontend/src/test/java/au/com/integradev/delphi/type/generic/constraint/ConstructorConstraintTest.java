/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.type.generic.constraint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.CLASS;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.RECORD;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.RoutineNameDeclarationImpl;
import au.com.integradev.delphi.symbol.scope.DelphiScopeImpl;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.type.factory.VoidTypeImpl;
import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Constraint;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class ConstructorConstraintTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();

  private static class SatisfiedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      var defaultConstructor = constructor("Create", VisibilityType.PUBLIC);
      var lowercaseDefaultConstructor = constructor("create", VisibilityType.PUBLIC);
      return Stream.of(
          Arguments.of(addDeclaration(mockClass(), defaultConstructor)),
          Arguments.of(addDeclaration(mockClass(), lowercaseDefaultConstructor)),
          Arguments.of(mockClass(addDeclaration(mockClass(), defaultConstructor))),
          Arguments.of(
              TypeParameterTypeImpl.create("T", List.of(ConstructorConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T",
                  List.of(ClassConstraintImpl.instance(), ConstructorConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T",
                  List.of(
                      new TypeConstraintImpl(addDeclaration(mockClass(), defaultConstructor)),
                      ClassConstraintImpl.instance(),
                      ConstructorConstraintImpl.instance()))));
    }
  }

  private static class ViolatedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      var defaultConstructor = constructor("Create", VisibilityType.PUBLIC);
      var classWithDefaultConstructor = addDeclaration(mockClass(), defaultConstructor);

      var privateConstructor = constructor("Create", VisibilityType.PRIVATE);
      var protectedConstructor = constructor("Create", VisibilityType.PROTECTED);
      var publishedConstructor = constructor("Create", VisibilityType.PUBLISHED);
      var wrongNameConstructor = constructor("NotCreate", VisibilityType.PUBLIC);
      var parametersConstructor = constructor("Create", VisibilityType.PUBLIC, true);

      return Stream.of(
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.BYTE)),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.STRING)),
          Arguments.of(TypeMocker.struct("TFoo", RECORD)),
          Arguments.of(mockClass()),
          Arguments.of(addDeclaration(mockClass(), privateConstructor)),
          Arguments.of(addDeclaration(mockClass(), protectedConstructor)),
          Arguments.of(addDeclaration(mockClass(), publishedConstructor)),
          Arguments.of(addDeclaration(mockClass(), wrongNameConstructor)),
          Arguments.of(addDeclaration(mockClass(), parametersConstructor)),
          Arguments.of(addDeclaration(mockClass(), parametersConstructor)),
          Arguments.of(addDeclaration(mockClass(classWithDefaultConstructor), privateConstructor)),
          Arguments.of(TypeParameterTypeImpl.create("T")),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(mock(Constraint.class)))),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(RecordConstraintImpl.instance()))),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(ClassConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T",
                  List.of(RecordConstraintImpl.instance(), ConstructorConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T",
                  List.of(
                      RecordConstraintImpl.instance(),
                      ConstructorConstraintImpl.instance(),
                      ClassConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create("T", List.of(new TypeConstraintImpl(mockClass())))));
    }
  }

  private static StructType mockClass() {
    return TypeMocker.struct("TFoo", CLASS);
  }

  private static StructType mockClass(Type parent) {
    return TypeMocker.struct("TFoo", CLASS, parent);
  }

  private static StructType addDeclaration(StructType type, NameDeclaration declaration) {
    ((DelphiScopeImpl) type.typeScope()).addDeclaration(declaration);
    return type;
  }

  private static RoutineNameDeclaration constructor(String name, VisibilityType visibility) {
    return constructor(name, visibility, false);
  }

  private static RoutineNameDeclaration constructor(
      String name, VisibilityType visibility, boolean hasParameters) {
    return new RoutineNameDeclarationImpl(
        SymbolicNode.imaginary(name, DelphiScope.unknownScope()),
        "Test.TFoo." + name,
        VoidTypeImpl.instance(),
        Collections.emptySet(),
        false,
        true,
        RoutineKind.CONSTRUCTOR,
        ((TypeFactoryImpl) FACTORY)
            .createProcedural(
                ProceduralKind.ROUTINE,
                hasParameters ? List.of(mock(Parameter.class)) : Collections.emptyList(),
                VoidTypeImpl.instance(),
                Collections.emptySet()),
        null,
        visibility,
        Collections.emptyList(),
        Collections.emptyList());
  }

  @ParameterizedTest
  @ArgumentsSource(SatisfiedArgumentsProvider.class)
  void testSatisfied(Type argumentType) {
    assertThat(ConstructorConstraintImpl.instance().satisfiedBy(argumentType)).isTrue();
  }

  @ParameterizedTest
  @ArgumentsSource(ViolatedArgumentsProvider.class)
  void testViolated(Type argumentType) {
    assertThat(ConstructorConstraintImpl.instance().satisfiedBy(argumentType)).isFalse();
  }
}
