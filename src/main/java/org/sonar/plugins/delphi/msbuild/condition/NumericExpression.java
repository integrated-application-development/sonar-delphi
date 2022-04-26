package org.sonar.plugins.delphi.msbuild.condition;

import java.util.Optional;
import org.sonar.plugins.delphi.msbuild.utils.NumericUtils;
import org.sonar.plugins.delphi.msbuild.utils.VersionUtils;

public class NumericExpression implements Expression {
  private final String value;

  public NumericExpression(String value) {
    this.value = value;
  }

  @Override
  public Optional<Double> numericEvaluate(ExpressionEvaluator evaluator) {
    return NumericUtils.parse(value);
  }

  @Override
  public Optional<Version> versionEvaluate(ExpressionEvaluator evaluator) {
    return VersionUtils.parse(value);
  }

  @Override
  public Optional<String> getValue() {
    return Optional.of(value);
  }

  @Override
  public Optional<String> getExpandedValue(ExpressionEvaluator evaluator) {
    return Optional.of(value);
  }
}
