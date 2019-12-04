package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import java.util.Set;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;

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

      Expression.ConstExpressionType type();

      String asString();

      Integer asInteger();

      Double asDecimal();

      Boolean asBoolean();

      Set<ExpressionValue> asSet();
    }
  }
}
