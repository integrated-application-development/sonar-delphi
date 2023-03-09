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
package au.com.integradev.delphi.preprocessor.directive.expression;

import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createBoolean;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createDecimal;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createInteger;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createSet;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createString;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.unknownValue;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType;
import au.com.integradev.delphi.preprocessor.directive.expression.Expression.ExpressionValue;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExpressionValuesTest {

  @Test
  void testStringValue() {
    ExpressionValue value = createString("string");
    assertThat(value.type()).isEqualTo(ConstExpressionType.STRING);
    assertThat(value.asString()).isEqualTo("string");
    assertThat(value.asInteger()).isZero();
    assertThat(value.asBigInteger()).isZero();
    assertThat(value.asDecimal()).isZero();
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();
  }

  @Test
  void testStringValueEquals() {
    ExpressionValue value = createString("string");
    assertThat(value)
        .isNotEqualTo(createString("other"))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createString("string"))
        .hasSameHashCodeAs(createString("string"))
        .isNotEqualTo(null)
        .isNotEqualTo(new Object());
  }

  @Test
  void testIntegerValue() {
    ExpressionValue value = createInteger(1);
    assertThat(value.type()).isEqualTo(ConstExpressionType.INTEGER);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isEqualTo(1);
    assertThat(value.asBigInteger()).isEqualTo(1);
    assertThat(value.asDecimal()).isEqualTo(1.0);
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();
  }

  @Test
  void testIntegerValueEquals() {
    ExpressionValue value = createInteger(1);
    assertThat(value)
        .isNotEqualTo(createInteger(2))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createInteger(1))
        .hasSameHashCodeAs(createInteger(1))
        .isNotEqualTo(null)
        .isNotEqualTo(new Object());
  }

  @Test
  void testDecimalValue() {
    ExpressionValue value = createDecimal(1.0);
    assertThat(value.type()).isEqualTo(ConstExpressionType.DECIMAL);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isEqualTo(1);
    assertThat(value.asBigInteger()).isEqualTo(1);
    assertThat(value.asDecimal()).isEqualTo(1.0);
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();
  }

  @Test
  void testDecimalValueEquals() {
    ExpressionValue value = createDecimal(1.0);
    assertThat(value)
        .isNotEqualTo(createDecimal(2.0))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createDecimal(1.0))
        .hasSameHashCodeAs(createDecimal(1.0))
        .isNotEqualTo(null)
        .isNotEqualTo(new Object());
  }

  @Test
  void testBooleanValue() {
    ExpressionValue value = createBoolean(true);
    assertThat(value.type()).isEqualTo(ConstExpressionType.BOOLEAN);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isZero();
    assertThat(value.asBigInteger()).isZero();
    assertThat(value.asDecimal()).isZero();
    assertThat(value.asBoolean()).isTrue();
    assertThat(value.asSet()).isEmpty();
  }

  @Test
  void testBooleanValueEquals() {
    ExpressionValue value = createBoolean(true);
    assertThat(value)
        .isNotEqualTo(createBoolean(false))
        .isNotEqualTo(unknownValue())
        .isEqualTo(value)
        .isEqualTo(createBoolean(true))
        .hasSameHashCodeAs(createBoolean(true))
        .isNotEqualTo(null)
        .isNotEqualTo(new Object());
  }

  @Test
  void testSetValue() {
    ExpressionValue value = createSet(Set.of(createInteger(1), createInteger(2), createInteger(3)));
    assertThat(value.type()).isEqualTo(ConstExpressionType.SET);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isZero();
    assertThat(value.asBigInteger()).isZero();
    assertThat(value.asDecimal()).isZero();
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).hasSize(3);
  }

  @Test
  void testSetValueEquals() {
    ExpressionValue value = createSet(Set.of(createInteger(1), createInteger(2), createInteger(3)));
    ExpressionValue other = createSet(Set.of(createInteger(1), createInteger(2), createInteger(3)));

    assertThat(value)
        .isNotEqualTo(createInteger(123))
        .isNotEqualTo(createSet(Collections.emptySet()))
        .isEqualTo(value)
        .isEqualTo(other)
        .hasSameHashCodeAs(other)
        .isNotEqualTo(null)
        .isNotEqualTo(new Object());
  }

  @Test
  void testUnknownValue() {
    ExpressionValue value = unknownValue();
    assertThat(value.type()).isEqualTo(ConstExpressionType.UNKNOWN);
    assertThat(value.asString()).isEmpty();
    assertThat(value.asInteger()).isZero();
    assertThat(value.asBigInteger()).isZero();
    assertThat(value.asDecimal()).isZero();
    assertThat(value.asBoolean()).isFalse();
    assertThat(value.asSet()).isEmpty();
  }
}
