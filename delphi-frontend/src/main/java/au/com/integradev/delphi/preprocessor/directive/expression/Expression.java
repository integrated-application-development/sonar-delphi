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
    DECIMAL,
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

    default Double asDecimal() {
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
