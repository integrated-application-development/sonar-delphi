package org.sonar.plugins.delphi.msbuild.condition;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import java.util.Set;
import org.sonar.plugins.delphi.msbuild.utils.NumericUtils;
import org.sonar.plugins.delphi.msbuild.utils.VersionUtils;

public class StringExpression implements Expression {
  private static final Set<String> TRUE_VALUES =
      ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER)
          .add("true", "on", "yes", "!false", "!off", "!no")
          .build();

  private static final Set<String> FALSE_VALUES =
      ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER)
          .add("false", "off", "no", "!true", "!on", "!yes")
          .build();

  private final String value;
  private final boolean expandable;
  private String expandedValue;

  StringExpression(String value, boolean expandable) {
    this.value = value;
    this.expandable = expandable;
  }

  @Override
  public Optional<Boolean> boolEvaluate(ExpressionEvaluator evaluator) {
    if (TRUE_VALUES.contains(getExpandedValue(evaluator).orElseThrow())) {
      return Optional.of(true);
    }

    if (FALSE_VALUES.contains(getExpandedValue(evaluator).orElseThrow())) {
      return Optional.of(false);
    }

    return Optional.empty();
  }

  @Override
  public Optional<Double> numericEvaluate(ExpressionEvaluator evaluator) {
    return NumericUtils.parse(getExpandedValue(evaluator).orElseThrow());
  }

  @Override
  public Optional<Version> versionEvaluate(ExpressionEvaluator evaluator) {
    return VersionUtils.parse(getExpandedValue(evaluator).orElseThrow());
  }

  @Override
  public Optional<String> getValue() {
    return Optional.of(value);
  }

  @Override
  public Optional<String> getExpandedValue(ExpressionEvaluator evaluator) {
    if (!expandable) {
      return getValue();
    }
    if (expandedValue == null) {
      expandedValue = evaluator.expand(value);
    }
    return Optional.of(expandedValue);
  }
}
