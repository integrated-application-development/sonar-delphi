/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
import static org.mockito.Mockito.when;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.CLASS;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.RECORD;

import au.com.integradev.delphi.symbol.scope.DelphiScopeImpl;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import au.com.integradev.delphi.utils.types.TypeMocker;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Constraint;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class UnmanagedConstraintTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();

  private static class SatisfiedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      Type integer = FACTORY.getIntrinsic(IntrinsicType.INTEGER);
      Type enumeration = ((TypeFactoryImpl) FACTORY).enumeration("", DelphiScope.unknownScope());
      return Stream.of(
          Arguments.of(integer),
          Arguments.of(enumeration),
          Arguments.of(FACTORY.subrange("", integer)),
          Arguments.of(FACTORY.subrange("", enumeration)),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.BOOLEAN)),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.DOUBLE)),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.ANSICHAR)),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.WIDECHAR)),
          Arguments.of(FACTORY.untypedPointer()),
          Arguments.of(TypeMocker.struct("TFoo", RECORD)),
          Arguments.of(recordWithField("TFoo", FACTORY.getIntrinsic(IntrinsicType.INTEGER))),
          Arguments.of(recordWithField("TFoo", FACTORY.untypedPointer())),
          Arguments.of(
              recordWithField(
                  "TFoo", recordWithField("TBar", FACTORY.getIntrinsic(IntrinsicType.INTEGER)))),
          Arguments.of(selfReferentialRecord()),
          Arguments.of(
              TypeParameterTypeImpl.create("T", List.of(UnmanagedConstraintImpl.instance()))));
    }
  }

  private static class ViolatedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.STRING)),
          Arguments.of(TypeMocker.struct("TBar", CLASS)),
          Arguments.of(recordWithField("TFoo", FACTORY.getIntrinsic(IntrinsicType.STRING))),
          Arguments.of(
              recordWithField(
                  "TFoo", recordWithField("TBar", FACTORY.getIntrinsic(IntrinsicType.STRING)))),
          Arguments.of(TypeParameterTypeImpl.create("T")),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(mock(Constraint.class)))),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(ClassConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create("T", List.of(ConstructorConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create("T", List.of(RecordConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create("T", List.of(InterfaceConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T",
                  List.of(ClassConstraintImpl.instance(), ConstructorConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T", List.of(new TypeConstraintImpl(TypeMocker.struct("TBar", CLASS))))));
    }
  }

  private static StructType recordWithField(String name, Type fieldType) {
    StructType type = TypeMocker.struct(name, RECORD);
    addField(type, "Field", fieldType);
    return type;
  }

  private static StructType selfReferentialRecord() {
    StructType type = TypeMocker.struct("TSelfRef", RECORD);
    addField(type, "Self", type);
    return type;
  }

  private static void addField(StructType type, String name, Type fieldType) {
    VariableNameDeclaration field = mock(VariableNameDeclaration.class);
    when(field.isField()).thenReturn(true);
    when(field.isClassVar()).thenReturn(false);
    when(field.getName()).thenReturn(name);
    when(field.getImage()).thenReturn(name);
    when(field.getType()).thenReturn(fieldType);
    ((DelphiScopeImpl) type.typeScope()).addDeclaration(field);
  }

  @ParameterizedTest
  @ArgumentsSource(SatisfiedArgumentsProvider.class)
  void testSatisfied(Type argumentType) {
    assertThat(UnmanagedConstraintImpl.instance().satisfiedBy(argumentType)).isTrue();
  }

  @ParameterizedTest
  @ArgumentsSource(ViolatedArgumentsProvider.class)
  void testViolated(Type argumentType) {
    assertThat(UnmanagedConstraintImpl.instance().satisfiedBy(argumentType)).isFalse();
  }
}
