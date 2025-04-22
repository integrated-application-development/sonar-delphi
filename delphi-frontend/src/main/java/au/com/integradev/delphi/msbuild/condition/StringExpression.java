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
package au.com.integradev.delphi.msbuild.condition;

import au.com.integradev.delphi.msbuild.expression.ExpressionEvaluator;
import au.com.integradev.delphi.msbuild.utils.NumericUtils;
import au.com.integradev.delphi.msbuild.utils.VersionUtils;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import java.util.Set;

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
      expandedValue = evaluator.eval(value);
    }
    return Optional.of(expandedValue);
  }
}
