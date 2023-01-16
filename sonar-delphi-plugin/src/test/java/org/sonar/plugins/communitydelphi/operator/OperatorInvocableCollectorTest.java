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
package org.sonar.plugins.communitydelphi.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.type.ArrayOption;
import org.sonar.plugins.communitydelphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.type.factory.TypeFactory;
import org.sonar.plugins.communitydelphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.communitydelphi.utils.types.TypeFactoryUtils;

class OperatorInvocableCollectorTest {
  @Test
  void testUnhandledOperatorShouldThrow() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThatThrownBy(() -> collector.collect(DelphiType.unknownType(), mock(Operator.class)))
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
    assertThat(collector.collect(typeFactory.emptySet(), BinaryOperator.ADD)).hasSize(9);
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
                typeFactory.array(
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
    assertThat(collector.collect(typeFactory.untypedType(), UnaryOperator.ADDRESS)).isEmpty();
  }
}
