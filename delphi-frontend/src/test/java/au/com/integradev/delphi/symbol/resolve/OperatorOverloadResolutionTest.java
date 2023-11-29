/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.symbol.resolve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.operator.OperatorInvocableCollector;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.operator.Operator;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class OperatorOverloadResolutionTest {
  private static final TypeFactory TYPE_FACTORY =
      new TypeFactoryImpl(Toolchain.DCC64, DelphiProperties.COMPILER_VERSION_DEFAULT);

  // For an explanation of this data (and reproduction steps):
  // https://github.com/integrated-application-development/sonar-delphi/issues/107#issuecomment-1830841874
  private static final String OVERLOAD_RESOLUTION_TABLE =
      "ShortInt, ShortInt, Integer, ShortInt, ShortInt, Integer, Integer, Extended\n"
          + "ShortInt, SmallInt, Integer, SmallInt, SmallInt, Integer, Integer, Extended\n"
          + "ShortInt, Integer, Integer, Integer, Integer, Integer, Integer, Extended\n"
          + "ShortInt, Int64, Int64, Int64, Int64, Integer, Int64, Extended\n"
          + "ShortInt, NativeInt, Int64, Int64, Int64, Integer, Int64, Extended\n"
          + "ShortInt, Byte, Integer, SmallInt, SmallInt, Integer, Integer, Extended\n"
          + "ShortInt, Word, Integer, Integer, Integer, Integer, Integer, Extended\n"
          + "ShortInt, Cardinal, Int64, Int64, Cardinal, Integer, Cardinal, Extended\n"
          + "ShortInt, UInt64, Int64, Int64, UInt64, Integer, UInt64, Extended\n"
          + "ShortInt, NativeUInt, Int64, Int64, UInt64, Integer, UInt64, Extended\n"
          + "SmallInt, SmallInt, Integer, SmallInt, SmallInt, Integer, Integer, Extended\n"
          + "SmallInt, Integer, Integer, Integer, Integer, Integer, Integer, Extended\n"
          + "SmallInt, Int64, Int64, Int64, Int64, Integer, Int64, Extended\n"
          + "SmallInt, NativeInt, Int64, Int64, Int64, Integer, Int64, Extended\n"
          + "SmallInt, Byte, Integer, SmallInt, SmallInt, Integer, Integer, Extended\n"
          + "SmallInt, Word, Integer, Integer, Integer, Integer, Integer, Extended\n"
          + "SmallInt, Cardinal, Int64, Int64, Cardinal, Integer, Cardinal, Extended\n"
          + "SmallInt, UInt64, Int64, Int64, UInt64, Integer, UInt64, Extended\n"
          + "SmallInt, NativeUInt, Int64, Int64, UInt64, Integer, UInt64, Extended\n"
          + "Integer, Integer, Integer, Integer, Integer, Integer, Integer, Extended\n"
          + "Integer, Int64, Int64, Int64, Int64, Integer, Int64, Extended\n"
          + "Integer, NativeInt, Int64, Int64, Int64, Integer, Int64, Extended\n"
          + "Integer, Byte, Integer, Integer, Integer, Integer, Integer, Extended\n"
          + "Integer, Word, Integer, Integer, Integer, Integer, Integer, Extended\n"
          + "Integer, Cardinal, Int64, Int64, Cardinal, Integer, Cardinal, Extended\n"
          + "Integer, UInt64, Int64, Int64, UInt64, Integer, UInt64, Extended\n"
          + "Integer, NativeUInt, Int64, Int64, UInt64, Integer, UInt64, Extended\n"
          + "Int64, Int64, Int64, Int64, Int64, Int64, Int64, Extended\n"
          + "Int64, NativeInt, Int64, Int64, Int64, Int64, Int64, Extended\n"
          + "Int64, Byte, Int64, Int64, Int64, Int64, Integer, Extended\n"
          + "Int64, Word, Int64, Int64, Int64, Int64, Integer, Extended\n"
          + "Int64, Cardinal, Int64, Int64, Int64, Int64, Cardinal, Extended\n"
          + "Int64, UInt64, Int64, Int64, UInt64, Int64, UInt64, Extended\n"
          + "Int64, NativeUInt, Int64, Int64, UInt64, Int64, UInt64, Extended\n"
          + "NativeInt, NativeInt, Int64, Int64, Int64, Int64, Int64, Extended\n"
          + "NativeInt, Byte, Int64, Int64, Int64, Int64, Integer, Extended\n"
          + "NativeInt, Word, Int64, Int64, Int64, Int64, Integer, Extended\n"
          + "NativeInt, Cardinal, Int64, Int64, Int64, Int64, Cardinal, Extended\n"
          + "NativeInt, UInt64, Int64, Int64, UInt64, Int64, UInt64, Extended\n"
          + "NativeInt, NativeUInt, Int64, Int64, UInt64, Int64, UInt64, Extended\n"
          + "Byte, Byte, Integer, Byte, Byte, Integer, Integer, Extended\n"
          + "Byte, Word, Integer, Word, Word, Integer, Integer, Extended\n"
          + "Byte, Cardinal, Cardinal, Cardinal, Cardinal, Integer, Cardinal, Extended\n"
          + "Byte, UInt64, UInt64, UInt64, UInt64, Integer, UInt64, Extended\n"
          + "Byte, NativeUInt, UInt64, UInt64, UInt64, Integer, UInt64, Extended\n"
          + "Word, Word, Integer, Word, Word, Integer, Integer, Extended\n"
          + "Word, Cardinal, Cardinal, Cardinal, Cardinal, Integer, Cardinal, Extended\n"
          + "Word, UInt64, UInt64, UInt64, UInt64, Integer, UInt64, Extended\n"
          + "Word, NativeUInt, UInt64, UInt64, UInt64, Integer, UInt64, Extended\n"
          + "Cardinal, Cardinal, Cardinal, Cardinal, Cardinal, Cardinal, Cardinal, Extended\n"
          + "Cardinal, UInt64, UInt64, UInt64, UInt64, Cardinal, UInt64, Extended\n"
          + "Cardinal, NativeUInt, UInt64, UInt64, UInt64, Cardinal, UInt64, Extended\n"
          + "UInt64, UInt64, UInt64, UInt64, UInt64, UInt64, UInt64, Extended\n"
          + "UInt64, NativeUInt, UInt64, UInt64, UInt64, UInt64, UInt64, Extended\n"
          + "NativeUInt, NativeUInt, UInt64, UInt64, UInt64, UInt64, UInt64, Extended";

  static class ArithmeticDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new ExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(2));
    }
  }

  static class BitwiseOrDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new ExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(3));
    }
  }

  static class BitwiseAndDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new ExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(4));
    }
  }

  static class ShiftDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new ExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(5));
    }
  }

  static class ShiftWithFlippedOperandsDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new ExpressionData(
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(6));
    }
  }

  static class DivideDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new ExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(7));
    }
  }

  static class ExpressionData {
    private final String left;
    private final String right;
    private final String result;

    ExpressionData(String left, String right, String result) {
      this.left = left;
      this.right = right;
      this.result = result;
    }

    public String getLeft() {
      return left;
    }

    public String getRight() {
      return right;
    }

    public String getResult() {
      return result;
    }
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} + {1} = {2}")
  void testAddOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.ADD);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} - {1} = {2}")
  void testSubtractOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SUBTRACT);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} * {1} = {2}")
  void testMultiplyOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.MULTIPLY);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} div {1} = {2}")
  void testDivOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.DIV);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} mod {1} = {2}")
  void testModOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.MOD);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} or {1} = {3}")
  void testOrOperatorOverloadResolution(
      @AggregateWith(BitwiseOrDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.OR);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} xor {1} = {3}")
  void testXorOperatorOverloadResolution(
      @AggregateWith(BitwiseOrDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.XOR);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} and {1} = {4}")
  void testAndOperatorOverloadResolution(
      @AggregateWith(BitwiseAndDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.AND);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} shl {1} = {5}")
  void testShlOperatorOverloadResolution(
      @AggregateWith(ShiftDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHL);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} shr {1} = {5}")
  void testShrOperatorOverloadResolution(
      @AggregateWith(ShiftDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHR);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{1} shl {0} = {6}")
  void testShlWithFlippedOperandsOperatorOverloadResolution(
      @AggregateWith(ShiftWithFlippedOperandsDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHL);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{1} shr {0} = {6}")
  void testShrWithFlippedOperandsOperatorOverloadResolution(
      @AggregateWith(ShiftWithFlippedOperandsDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHR);
  }

  @CsvSource(textBlock = OVERLOAD_RESOLUTION_TABLE)
  @ParameterizedTest(name = "{0} / {1} = {7}")
  void testDivideOperatorOverloadResolution(
      @AggregateWith(DivideDataAggregator.class) ExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.DIVIDE);
  }

  private static void assertResolved(ExpressionData expressionData, Operator operator) {
    String left = expressionData.getLeft();
    String right = expressionData.getRight();
    String result = expressionData.getResult();

    assertThat(resolveOperatorOverload(left, right, operator))
        .isEqualTo(intrinsicType(result).getImage());
  }

  private static String resolveOperatorOverload(String left, String right, Operator operator) {
    Type leftType = intrinsicType(left);
    Type rightType = intrinsicType(right);

    ExpressionNode leftExpr = mockExpressionNode(leftType);
    ExpressionNode rightExpr = mockExpressionNode(rightType);

    InvocationResolver resolver = new InvocationResolver();

    OperatorInvocableCollector collector = new OperatorInvocableCollector(TYPE_FACTORY);

    List.of(new InvocationArgument(leftExpr), new InvocationArgument(rightExpr))
        .forEach(
            argument -> {
              resolver.addArgument(argument);
              collector.collect(argument.getType(), operator).stream()
                  .map(InvocationCandidate::new)
                  .forEach(resolver::addCandidate);
            });

    resolver.processCandidates();

    Set<InvocationCandidate> bestCandidate = resolver.chooseBest();
    assertThat(bestCandidate)
        .overridingErrorMessage("No valid operator overload candidates.")
        .isNotEmpty();
    assertThat(bestCandidate)
        .overridingErrorMessage("Ambiguous operator overload candidates.")
        .hasSize(1);

    return Iterables.getLast(bestCandidate).getData().getReturnType().getImage();
  }

  private static Type intrinsicType(String simpleName) {
    IntrinsicType intrinsic =
        Arrays.stream(IntrinsicType.values())
            .filter(it -> it.simpleName().equals(simpleName))
            .findFirst()
            .orElseThrow();
    return TYPE_FACTORY.getIntrinsic(intrinsic);
  }

  private static ExpressionNode mockExpressionNode(Type type) {
    ExpressionNode result = mock();
    when(result.getType()).thenReturn(type);
    return result;
  }
}
