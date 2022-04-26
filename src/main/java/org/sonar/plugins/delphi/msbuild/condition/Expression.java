package org.sonar.plugins.delphi.msbuild.condition;

import java.util.Optional;

public interface Expression {
  default Optional<Boolean> boolEvaluate(ExpressionEvaluator evaluator) {
    return Optional.empty();
  }

  default Optional<Double> numericEvaluate(ExpressionEvaluator evaluator) {
    return Optional.empty();
  }

  default Optional<Version> versionEvaluate(ExpressionEvaluator evaluator) {
    return Optional.empty();
  }

  default Optional<String> getValue() {
    return Optional.empty();
  }

  default Optional<String> getExpandedValue(ExpressionEvaluator evaluator) {
    return Optional.empty();
  }
}
