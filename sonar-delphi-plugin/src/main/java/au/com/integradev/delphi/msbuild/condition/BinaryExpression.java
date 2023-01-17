/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import au.com.integradev.delphi.msbuild.condition.Token.TokenType;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

class BinaryExpression implements Expression {
  private final Expression left;
  private final TokenType operator;
  private final Expression right;

  BinaryExpression(Expression left, TokenType operator, Expression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public Optional<Boolean> boolEvaluate(ExpressionEvaluator evaluator) {
    switch (operator) {
      case AND:
        return logical(evaluator, (a, b) -> a && b);
      case OR:
        return logical(evaluator, (a, b) -> a || b);
      case EQUAL:
        return isEqual(evaluator);
      case NOT_EQUAL:
        return isEqual(evaluator).map(value -> !value);
        // NOTE:
        // When comparing numerics against versions, if the number is the same as the "major"
        // version,
        // then that means we are comparing something like "6.X.Y.Z" to "6".
        // Version treats the objects with more dots as "larger" regardless of what those dots are
        // (e.g. 6.0.0.0 > 6 is a true statement)
      case GREATER_THAN:
        return compare(
            evaluator,
            (a, b) -> a > b,
            (a, b) -> a.compareTo(b) > 0,
            (a, b) -> a.getMajor() >= b,
            (a, b) -> a > b.getMajor());
      case GREATER_THAN_EQUAL:
        return compare(
            evaluator,
            (a, b) -> a >= b,
            (a, b) -> a.compareTo(b) >= 0,
            (a, b) -> a.getMajor() >= b,
            (a, b) -> a > b.getMajor());
      case LESS_THAN:
        return compare(
            evaluator,
            (a, b) -> a < b,
            (a, b) -> a.compareTo(b) < 0,
            (a, b) -> a.getMajor() < b,
            (a, b) -> a <= b.getMajor());
      case LESS_THAN_EQUAL:
        return compare(
            evaluator,
            (a, b) -> a <= b,
            (a, b) -> a.compareTo(b) <= 0,
            (a, b) -> a.getMajor() < b,
            (a, b) -> a <= b.getMajor());
      default:
        throw new InvalidExpressionException(String.format("Unhandled operator: %s", operator));
    }
  }

  private Optional<Boolean> logical(ExpressionEvaluator evaluator, BinaryOperator<Boolean> func) {
    Optional<Boolean> leftBool = left.boolEvaluate(evaluator);
    Optional<Boolean> rightBool = right.boolEvaluate(evaluator);
    if (leftBool.isEmpty() || rightBool.isEmpty()) {
      throw new InvalidExpressionException("Operand does not evaluate to boolean");
    }
    return Optional.of(func.apply(leftBool.get(), rightBool.get()));
  }

  private Optional<Boolean> isEqual(ExpressionEvaluator evaluator) {
    Optional<Double> leftNumeric = left.numericEvaluate(evaluator);
    Optional<Double> rightNumeric = right.numericEvaluate(evaluator);
    if (leftNumeric.isPresent() && rightNumeric.isPresent()) {
      return Optional.of(leftNumeric.get().equals(rightNumeric.get()));
    }

    Optional<Boolean> leftBool = left.boolEvaluate(evaluator);
    Optional<Boolean> rightBool = right.boolEvaluate(evaluator);
    if (leftBool.isPresent() && rightBool.isPresent()) {
      return Optional.of(leftBool.get().equals(rightBool.get()));
    }

    Optional<String> leftString = left.getExpandedValue(evaluator);
    Optional<String> rightString = right.getExpandedValue(evaluator);
    if (leftString.isPresent() && rightString.isPresent()) {
      return Optional.of(leftString.get().equalsIgnoreCase(rightString.get()));
    }

    throw new InvalidExpressionException("Invalid operands");
  }

  private Optional<Boolean> compare(
      ExpressionEvaluator evaluator,
      BiPredicate<Double, Double> numericToNumericFunc,
      BiPredicate<Version, Version> versionToVersionFunc,
      BiPredicate<Version, Double> versionToNumericFunc,
      BiPredicate<Double, Version> numericToVersionFunc) {
    Optional<Double> leftNumeric = left.numericEvaluate(evaluator);
    Optional<Version> leftVersion = left.versionEvaluate(evaluator);
    Optional<Double> rightNumeric = right.numericEvaluate(evaluator);
    Optional<Version> rightVersion = right.versionEvaluate(evaluator);

    if (leftNumeric.isPresent() && rightNumeric.isPresent()) {
      return Optional.of(numericToNumericFunc.test(leftNumeric.get(), rightNumeric.get()));
    } else if (leftVersion.isPresent() && rightVersion.isPresent()) {
      return Optional.of(versionToVersionFunc.test(leftVersion.get(), rightVersion.get()));
    } else if (leftVersion.isPresent() && rightNumeric.isPresent()) {
      return Optional.of(versionToNumericFunc.test(leftVersion.get(), rightNumeric.get()));
    } else if (leftNumeric.isPresent() && rightVersion.isPresent()) {
      return Optional.of(numericToVersionFunc.test(leftNumeric.get(), rightVersion.get()));
    }

    throw new InvalidExpressionException("Comparison has non-numeric operand");
  }
}
