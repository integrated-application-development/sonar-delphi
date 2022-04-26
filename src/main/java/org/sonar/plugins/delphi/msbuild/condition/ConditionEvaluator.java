package org.sonar.plugins.delphi.msbuild.condition;

import java.nio.file.Path;
import java.util.List;
import org.sonar.plugins.delphi.msbuild.ProjectProperties;

public final class ConditionEvaluator {
  private final ExpressionEvaluator expressionEvaluator;

  public ConditionEvaluator(ProjectProperties properties, Path evaluationDirectory) {
    expressionEvaluator = new ExpressionEvaluator(evaluationDirectory, properties.substitutor());
  }

  public boolean evaluate(String condition) {
    if (condition == null) {
      return true;
    }

    if (condition.isEmpty()) {
      return false;
    }

    try {
      ConditionLexer lexer = new ConditionLexer();
      List<Token> tokens = lexer.lex(condition);

      ConditionParser parser = new ConditionParser();
      Expression expression = parser.parse(tokens);

      return expression
          .boolEvaluate(expressionEvaluator)
          .orElseThrow(() -> new ConditionDoesNotEvaluateToBooleanException(condition));
    } catch (Exception e) {
      throw new ConditionEvaluationError(condition, e);
    }
  }

  private static class ConditionDoesNotEvaluateToBooleanException extends RuntimeException {
    ConditionDoesNotEvaluateToBooleanException(String condition) {
      super(String.format("Specified condition \"%s\" does not evaluate to boolean.", condition));
    }
  }
}
