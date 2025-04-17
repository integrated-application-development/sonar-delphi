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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.CLASS;
import static org.sonar.plugins.communitydelphi.api.type.StructKind.RECORD;

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
import org.sonar.plugins.communitydelphi.api.type.Constraint;
import org.sonar.plugins.communitydelphi.api.type.Constraint.TypeConstraint;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class TypeConstraintTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();

  private static final Type TFOO = TypeMocker.struct("TFoo", CLASS);
  private static final Type TBAR = TypeMocker.struct("TBar", CLASS, TFOO);
  private static final Type TBAZ = TypeMocker.struct("TBaz", CLASS, TBAR);
  private static final TypeConstraint CONSTRAINT = new TypeConstraintImpl(TBAR);

  private static class SatisfiedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(TBAR),
          Arguments.of(TBAZ),
          Arguments.of(FACTORY.classOf(null, TBAR)),
          Arguments.of(FACTORY.classOf(null, TBAZ)),
          Arguments.of(TBAR),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T",
                  List.of(
                      new TypeConstraintImpl(TypeMocker.struct("TBar", CLASS)),
                      ClassConstraintImpl.instance(),
                      ConstructorConstraintImpl.instance()))));
    }
  }

  private static class ViolatedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.BYTE)),
          Arguments.of(FACTORY.getIntrinsic(IntrinsicType.STRING)),
          Arguments.of(TFOO),
          Arguments.of(TypeMocker.struct("TFlarp", RECORD)),
          Arguments.of(TypeMocker.struct("TFlimFlam", CLASS)),
          Arguments.of(TypeParameterTypeImpl.create("T")),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(mock(Constraint.class)))),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(ClassConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create("T", List.of(ConstructorConstraintImpl.instance()))),
          Arguments.of(TypeParameterTypeImpl.create("T", List.of(RecordConstraintImpl.instance()))),
          Arguments.of(
              TypeParameterTypeImpl.create(
                  "T",
                  List.of(
                      ClassConstraintImpl.instance(),
                      ConstructorConstraintImpl.instance(),
                      RecordConstraintImpl.instance()))));
    }
  }

  @ParameterizedTest
  @ArgumentsSource(SatisfiedArgumentsProvider.class)
  void testSatisfied(Type argumentType) {
    assertThat(CONSTRAINT.satisfiedBy(argumentType)).isTrue();
  }

  @ParameterizedTest
  @ArgumentsSource(ViolatedArgumentsProvider.class)
  void testViolated(Type argumentType) {
    assertThat(CONSTRAINT.satisfiedBy(argumentType)).isFalse();
  }
}
