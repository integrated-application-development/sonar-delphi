/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Expression {

  ExpressionValue evaluate(DelphiPreprocessor preprocessor);

  enum ConstExpressionType {
    UNKNOWN,
    STRING,
    INTEGER,
    REAL,
    BOOLEAN,
    SET
  }

  interface ExpressionValue {

    interface BinaryEvaluator
        extends BiFunction<ExpressionValue, ExpressionValue, ExpressionValue> {}

    interface UnaryEvaluator extends Function<ExpressionValue, ExpressionValue> {}

    ConstExpressionType type();

    default String asString() {
      return "";
    }

    default Integer asInteger() {
      return 0;
    }

    default BigInteger asBigInteger() {
      return BigInteger.ZERO;
    }

    default Double asDouble() {
      return 0.0;
    }

    default Boolean asBoolean() {
      return false;
    }

    default Set<ExpressionValue> asSet() {
      return Collections.emptySet();
    }
  }
}
