package org.sonar.plugins.delphi.msbuild.condition;

import java.util.Optional;

public class NotExpression implements Expression {
  private final Expression expression;

  NotExpression(Expression expression) {
    this.expression = expression;
  }

  @Override
  public Optional<Boolean> boolEvaluate(ExpressionEvaluator evaluator) {
    Optional<Boolean> result = this.expression.boolEvaluate(evaluator);
    return result
        .map(value -> Optional.of(!value))
        .orElseThrow(
            () -> new InvalidExpressionException("Expression does not evaluate to boolean"));
  }

  @Override
  public Optional<String> getValue() {
    return Optional.of("!" + expression.getValue().orElse(""));
  }

  @Override
  public Optional<String> getExpandedValue(ExpressionEvaluator evaluator) {
    return Optional.of("!" + expression.getExpandedValue(evaluator).orElse(""));
  }
}
