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
package au.com.integradev.delphi.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.operator.Operator;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class OperatorInvocableCollectorTest {
  @Test
  void testUnhandledOperatorShouldThrow() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    Type unknownType = TypeFactory.unknownType();
    assertThatThrownBy(() -> collector.collect(unknownType, mock(Operator.class)))
        .withFailMessage("Unhandled Operator")
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testPointerMathTypeWithInvalidOperatorShouldNotCollectPointerMathOperators() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(typeFactory.getIntrinsic(IntrinsicType.PCHAR), BinaryOperator.DIVIDE))
        .hasSize(1);
  }

  @Test
  void testVariantShouldCollectInOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(typeFactory.getIntrinsic(IntrinsicType.VARIANT), BinaryOperator.IN))
        .hasSize(1);
  }

  @Test
  void testSetShouldCollectAddOperators() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(typeFactory.emptySet(), BinaryOperator.ADD)).hasSize(13);
  }

  @Test
  void testVariantShouldNotCollectAsOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(typeFactory.getIntrinsic(IntrinsicType.VARIANT), BinaryOperator.AS))
        .isEmpty();
  }

  @Test
  void testDynamicArrayShouldNotCollectAsOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(
            collector.collect(
                ((TypeFactoryImpl) typeFactory)
                    .array(
                        "Foo",
                        typeFactory.getIntrinsic(IntrinsicType.STRING),
                        Set.of(ArrayOption.DYNAMIC)),
                BinaryOperator.AS))
        .isEmpty();
  }

  @Test
  void testNothingShouldCollectAddressOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(TypeFactory.untypedType(), UnaryOperator.ADDRESS)).isEmpty();
  }
}
