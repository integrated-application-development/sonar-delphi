package org.sonar.plugins.delphi.preprocessor.directive;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public interface CompilerDirective {
  Token getToken();

  CompilerDirectiveType getType();

  void execute(DelphiPreprocessor preprocessor);

  static CompilerDirective fromToken(Token token) {
    CompilerDirectiveParser parser = new CompilerDirectiveParser();
    return parser.parse(token);
  }

  interface Expression {
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

      Expression.ConstExpressionType type();

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
}
