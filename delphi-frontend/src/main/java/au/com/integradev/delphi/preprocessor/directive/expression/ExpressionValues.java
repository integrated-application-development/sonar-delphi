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
package au.com.integradev.delphi.preprocessor.directive.expression;

import static au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType.BOOLEAN;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType.DECIMAL;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType.INTEGER;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType.SET;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType.STRING;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType.UNKNOWN;
import static java.lang.Math.ulp;

import au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType;
import au.com.integradev.delphi.preprocessor.directive.expression.Expression.ExpressionValue;
import com.google.common.math.DoubleMath;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;

class ExpressionValues {
  private static final ExpressionValue UNKNOWN_VALUE = () -> UNKNOWN;

  private ExpressionValues() {
    // Utility class
  }

  static ExpressionValue unknownValue() {
    return UNKNOWN_VALUE;
  }

  static ExpressionValue createInteger(long value) {
    return new PrimitiveExpressionValue(INTEGER, BigInteger.valueOf(value));
  }

  static ExpressionValue createInteger(BigInteger value) {
    return new PrimitiveExpressionValue(INTEGER, value);
  }

  static ExpressionValue createDecimal(Double value) {
    return new PrimitiveExpressionValue(DECIMAL, value);
  }

  static ExpressionValue createString(String value) {
    return new PrimitiveExpressionValue(STRING, value);
  }

  static ExpressionValue createBoolean(Boolean value) {
    return new PrimitiveExpressionValue(BOOLEAN, value);
  }

  static ExpressionValue createSet(Set<ExpressionValue> elements) {
    return new SetExpressionValue(elements);
  }

  private static boolean isNumeric(ExpressionValue value) {
    return value.type() == INTEGER || value.type() == DECIMAL;
  }

  private static ExpressionValue calculate(
      ExpressionValue left, ExpressionValue right, DoubleBinaryOperator op) {
    if (isNumeric(left) && isNumeric(right)) {
      double value = op.applyAsDouble(left.asDecimal(), right.asDecimal());

      if (DoubleMath.isMathematicalInteger(value)) {
        return createInteger((int) value);
      }
      return createDecimal(value);
    }
    return unknownValue();
  }

  private static ExpressionValue calculateInteger(
      ExpressionValue left, ExpressionValue right, IntBinaryOperator op) {
    if (left.type() == INTEGER && right.type() == INTEGER) {
      return createInteger(op.applyAsInt(left.asInteger(), right.asInteger()));
    }
    return unknownValue();
  }

  static ExpressionValue add(ExpressionValue left, ExpressionValue right) {
    if (left.type() == STRING && right.type() == STRING) {
      return createString(left.asString() + right.asString());
    }
    return calculate(left, right, Double::sum);
  }

  static ExpressionValue subtract(ExpressionValue left, ExpressionValue right) {
    return calculate(left, right, (l, r) -> l - r);
  }

  static ExpressionValue multiply(ExpressionValue left, ExpressionValue right) {
    return calculate(left, right, (l, r) -> l * r);
  }

  static ExpressionValue divide(ExpressionValue left, ExpressionValue right) {
    return calculate(left, right, (l, r) -> l / r);
  }

  static ExpressionValue div(ExpressionValue left, ExpressionValue right) {
    return calculateInteger(left, right, Math::floorDiv);
  }

  static ExpressionValue mod(ExpressionValue left, ExpressionValue right) {
    return calculateInteger(left, right, Math::floorMod);
  }

  static ExpressionValue shl(ExpressionValue left, ExpressionValue right) {
    return calculateInteger(left, right, (l, r) -> l << r);
  }

  static ExpressionValue shr(ExpressionValue left, ExpressionValue right) {
    return calculateInteger(left, right, (l, r) -> l >> r);
  }

  static ExpressionValue isEqual(ExpressionValue left, ExpressionValue right) {
    if (left.type() == UNKNOWN || right.type() == UNKNOWN) {
      return unknownValue();
    }

    if (isNumeric(left) && isNumeric(right)) {
      double leftValue = left.asDecimal();
      double rightValue = right.asDecimal();
      return createBoolean(DoubleMath.fuzzyEquals(leftValue, rightValue, ulp(leftValue)));
    }

    return createBoolean(left.equals(right));
  }

  static ExpressionValue greaterThan(ExpressionValue left, ExpressionValue right) {
    if (isNumeric(left) && isNumeric(right)) {
      return createBoolean(left.asDecimal() > right.asDecimal());
    }
    return unknownValue();
  }

  static ExpressionValue lessThan(ExpressionValue left, ExpressionValue right) {
    if (isNumeric(left) && isNumeric(right)) {
      return createBoolean(left.asDecimal() < right.asDecimal());
    }
    return unknownValue();
  }

  static ExpressionValue greaterThanEqual(ExpressionValue left, ExpressionValue right) {
    if (isNumeric(left) && isNumeric(right)) {
      return createBoolean(left.asDecimal() >= right.asDecimal());
    } else if (left.type() == SET && right.type() == SET) {
      return createBoolean(left.asSet().containsAll(right.asSet()));
    }
    return unknownValue();
  }

  static ExpressionValue lessThanEqual(ExpressionValue left, ExpressionValue right) {
    if (isNumeric(left) && isNumeric(right)) {
      return createBoolean(left.asDecimal() <= right.asDecimal());
    } else if (left.type() == SET && right.type() == SET) {
      return createBoolean(right.asSet().containsAll(left.asSet()));
    }
    return unknownValue();
  }

  static ExpressionValue notEqual(ExpressionValue left, ExpressionValue right) {
    return negate(isEqual(left, right));
  }

  static ExpressionValue in(ExpressionValue left, ExpressionValue right) {
    if (right.type() == SET) {
      return createBoolean(right.asSet().contains(left));
    }
    return unknownValue();
  }

  static ExpressionValue and(ExpressionValue left, ExpressionValue right) {
    if (left.type() == BOOLEAN && right.type() == BOOLEAN) {
      return createBoolean(left.asBoolean() && right.asBoolean());
    }
    return unknownValue();
  }

  static ExpressionValue or(ExpressionValue left, ExpressionValue right) {
    if (left.type() == BOOLEAN && right.type() == BOOLEAN) {
      return createBoolean(left.asBoolean() || right.asBoolean());
    }
    return unknownValue();
  }

  static ExpressionValue xor(ExpressionValue left, ExpressionValue right) {
    if (left.type() == BOOLEAN && right.type() == BOOLEAN) {
      return createBoolean(left.asBoolean() ^ right.asBoolean());
    }
    return unknownValue();
  }

  static ExpressionValue plus(ExpressionValue value) {
    if (value.type() == INTEGER || value.type() == DECIMAL) {
      return value;
    } else {
      return unknownValue();
    }
  }

  static ExpressionValue negate(ExpressionValue value) {
    switch (value.type()) {
      case INTEGER:
        return createInteger(value.asBigInteger().negate());
      case DECIMAL:
        return createDecimal(-value.asDecimal());
      case BOOLEAN:
        return createBoolean(!value.asBoolean());
      default:
        return unknownValue();
    }
  }

  static ExpressionValue not(ExpressionValue value) {
    if (value.type() == BOOLEAN) {
      return createBoolean(!value.asBoolean());
    }
    return unknownValue();
  }

  private static final class PrimitiveExpressionValue implements ExpressionValue {
    private final ConstExpressionType type;
    private final Object value;

    private PrimitiveExpressionValue(ConstExpressionType type, Object value) {
      this.type = type;
      this.value = value;
    }

    @Override
    public ConstExpressionType type() {
      return type;
    }

    @Override
    public String asString() {
      if (value instanceof String) {
        return (String) value;
      }
      return "";
    }

    @Override
    public Integer asInteger() {
      if (value instanceof BigInteger) {
        return ((BigInteger) value).intValue();
      } else if (value instanceof Double) {
        return ((Double) value).intValue();
      }
      return 0;
    }

    @Override
    public BigInteger asBigInteger() {
      if (value instanceof BigInteger) {
        return (BigInteger) value;
      } else if (value instanceof Double) {
        return BigDecimal.valueOf((Double) value).toBigInteger();
      }
      return BigInteger.ZERO;
    }

    @Override
    public Double asDecimal() {
      if (value instanceof Double) {
        return (Double) value;
      } else if (value instanceof BigInteger) {
        return ((BigInteger) value).doubleValue();
      }
      return 0.0;
    }

    @Override
    public Boolean asBoolean() {
      if (value instanceof Boolean) {
        return (Boolean) value;
      }
      return false;
    }

    @Override
    public Set<ExpressionValue> asSet() {
      return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PrimitiveExpressionValue that = (PrimitiveExpressionValue) o;
      return type == that.type && value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, value);
    }
  }

  private static final class SetExpressionValue implements ExpressionValue {
    private final Set<ExpressionValue> elements;

    private SetExpressionValue(Set<ExpressionValue> elements) {
      this.elements = elements;
    }

    @Override
    public ConstExpressionType type() {
      return SET;
    }

    @Override
    public Set<ExpressionValue> asSet() {
      return elements;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SetExpressionValue that = (SetExpressionValue) o;
      return elements.equals(that.elements);
    }

    @Override
    public int hashCode() {
      return Objects.hash(elements);
    }
  }
}
