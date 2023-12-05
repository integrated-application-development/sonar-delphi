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

import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class OperatorInvocableCollectorTest {
  @Test
  void testPointerMathTypeWithInvalidOperatorShouldNotCollectPointerMathOperators() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    Type pchar = typeFactory.getIntrinsic(IntrinsicType.PCHAR);

    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(BinaryOperator.DIVIDE, pchar, pchar)).isEmpty();
  }

  @Test
  void testVariantShouldCollectInOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    Type variant = typeFactory.getIntrinsic(IntrinsicType.VARIANT);

    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(BinaryOperator.IN, variant, variant)).hasSize(1);
  }

  @Test
  void testSetShouldCollectAddOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    Type set = typeFactory.emptySet();

    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(BinaryOperator.ADD, set, set)).hasSize(1);
  }

  @Test
  void testVariantShouldNotCollectAsOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    Type variant = typeFactory.getIntrinsic(IntrinsicType.VARIANT);

    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(BinaryOperator.AS, variant, variant)).isEmpty();
  }

  @Test
  void testDynamicArrayShouldNotCollectAsOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();
    Type array =
        ((TypeFactoryImpl) typeFactory)
            .array(
                "Foo", typeFactory.getIntrinsic(IntrinsicType.STRING), Set.of(ArrayOption.DYNAMIC));

    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(BinaryOperator.AS, array, array)).isEmpty();
  }

  @Test
  void testNothingShouldCollectAddressOperator() {
    TypeFactory typeFactory = TypeFactoryUtils.defaultFactory();

    OperatorInvocableCollector collector = new OperatorInvocableCollector(typeFactory);
    assertThat(collector.collect(UnaryOperator.ADDRESS, TypeFactory.untypedType())).isEmpty();
  }
}
