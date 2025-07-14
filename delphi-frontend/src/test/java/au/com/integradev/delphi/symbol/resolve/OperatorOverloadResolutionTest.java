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
import java.math.BigInteger;
import java.util.Set;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class OperatorOverloadResolutionTest {
  private static final TypeFactory TYPE_FACTORY =
      new TypeFactoryImpl(Toolchain.DCC64, DelphiProperties.COMPILER_VERSION_DEFAULT);

  // For more information:
  // https://github.com/integrated-application-development/sonar-delphi/issues/107#issuecomment-1841868071
  private static final String BINARY_OVERLOADS = "binary-overload-resolution.csv";

  // For more information:
  // https://github.com/integrated-application-development/sonar-delphi/issues/107#issuecomment-1840094351
  private static final String UNARY_OVERLOADS = "unary-overload-resolution.csv";

  static class ArithmeticDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new BinaryExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(2));
    }
  }

  static class SubtractDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new BinaryExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(3));
    }
  }

  static class BitwiseOrDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new BinaryExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(4));
    }
  }

  static class BitwiseAndDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new BinaryExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(5));
    }
  }

  static class ShiftDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new BinaryExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(6));
    }
  }

  static class ShiftWithFlippedOperandsDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new BinaryExpressionData(
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(7));
    }
  }

  static class DivideDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new BinaryExpressionData(
          argumentsAccessor.getString(0),
          argumentsAccessor.getString(1),
          argumentsAccessor.getString(8));
    }
  }

  static class NegativeDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new UnaryExpressionData(
          argumentsAccessor.getString(0), argumentsAccessor.getString(1));
    }
  }

  static class PositiveDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new UnaryExpressionData(
          argumentsAccessor.getString(0), argumentsAccessor.getString(2));
    }
  }

  static class NotDataAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(
        ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext)
        throws ArgumentsAggregationException {
      return new UnaryExpressionData(
          argumentsAccessor.getString(0), argumentsAccessor.getString(3));
    }
  }

  static class BinaryExpressionData {
    private final String left;
    private final String right;
    private final String result;

    BinaryExpressionData(String left, String right, String result) {
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

  static class UnaryExpressionData {
    private final String operand;
    private final String result;

    UnaryExpressionData(String operand, String result) {
      this.operand = operand;
      this.result = result;
    }

    public String getOperand() {
      return operand;
    }

    public String getResult() {
      return result;
    }
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} + {1} = {2}")
  void testAddOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.ADD);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} * {1} = {2}")
  void testMultiplyOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.MULTIPLY);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} div {1} = {2}")
  void testDivOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.DIV);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} mod {1} = {2}")
  void testModOperatorOverloadResolution(
      @AggregateWith(ArithmeticDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.MOD);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} - {1} = {3}")
  void testSubtractOperatorOverloadResolution(
      @AggregateWith(SubtractDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SUBTRACT);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} or {1} = {4}")
  void testOrOperatorOverloadResolution(
      @AggregateWith(BitwiseOrDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.OR);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} xor {1} = {4}")
  void testXorOperatorOverloadResolution(
      @AggregateWith(BitwiseOrDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.XOR);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} and {1} = {5}")
  void testAndOperatorOverloadResolution(
      @AggregateWith(BitwiseAndDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.AND);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} shl {1} = {6}")
  void testShlOperatorOverloadResolution(
      @AggregateWith(ShiftDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHL);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} shr {1} = {6}")
  void testShrOperatorOverloadResolution(
      @AggregateWith(ShiftDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHR);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{1} shl {0} = {7}")
  void testShlWithFlippedOperandsOperatorOverloadResolution(
      @AggregateWith(ShiftWithFlippedOperandsDataAggregator.class)
          BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHL);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{1} shr {0} = {7}")
  void testShrWithFlippedOperandsOperatorOverloadResolution(
      @AggregateWith(ShiftWithFlippedOperandsDataAggregator.class)
          BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.SHR);
  }

  @CsvFileSource(resources = BINARY_OVERLOADS)
  @ParameterizedTest(name = "{0} / {1} = {8}")
  void testDivideOperatorOverloadResolution(
      @AggregateWith(DivideDataAggregator.class) BinaryExpressionData expressionData) {
    assertResolved(expressionData, BinaryOperator.DIVIDE);
  }

  @CsvFileSource(resources = UNARY_OVERLOADS)
  @ParameterizedTest(name = "-{0} = {1}")
  void testNegativeOperatorOverloadResolution(
      @AggregateWith(NegativeDataAggregator.class) UnaryExpressionData expressionData) {
    assertResolved(expressionData, UnaryOperator.NEGATE);
  }

  @CsvFileSource(resources = UNARY_OVERLOADS)
  @ParameterizedTest(name = "+{0} = {2}")
  void testPositiveOperatorOverloadResolution(
      @AggregateWith(PositiveDataAggregator.class) UnaryExpressionData expressionData) {
    assertResolved(expressionData, UnaryOperator.PLUS);
  }

  @CsvFileSource(resources = UNARY_OVERLOADS)
  @ParameterizedTest(name = "not {0} = {3}")
  void testNotOperatorOverloadResolution(
      @AggregateWith(NotDataAggregator.class) UnaryExpressionData expressionData) {
    assertResolved(expressionData, UnaryOperator.NOT);
  }

  private static void assertResolved(BinaryExpressionData expressionData, BinaryOperator operator) {
    String left = expressionData.getLeft();
    String right = expressionData.getRight();
    String result = expressionData.getResult();

    assertThat(resolveOperatorOverload(operator, getType(left), getType(right)))
        .isEqualTo(getType(result).getImage());
  }

  private static void assertResolved(UnaryExpressionData expressionData, UnaryOperator operator) {
    String operand = expressionData.getOperand();
    String result = expressionData.getResult();

    assertThat(resolveOperatorOverload(operator, getType(operand)))
        .isEqualTo(getType(result).getImage());
  }

  private static String resolveOperatorOverload(BinaryOperator operator, Type left, Type right) {
    InvocationResolver resolver = new InvocationResolver();
    resolver.addArgument(new InvocationArgument(mockExpressionNode(left)));
    resolver.addArgument(new InvocationArgument(mockExpressionNode(right)));

    createOperatorInvocables(operator, left, right).stream()
        .map(InvocationCandidate::new)
        .forEach(resolver::addCandidate);

    return resolveOperatorOverload(resolver);
  }

  private static String resolveOperatorOverload(UnaryOperator operator, Type operand) {
    InvocationResolver resolver = new InvocationResolver();
    resolver.addArgument(new InvocationArgument(mockExpressionNode(operand)));

    createOperatorInvocables(operator, operand).stream()
        .map(InvocationCandidate::new)
        .forEach(resolver::addCandidate);

    return resolveOperatorOverload(resolver);
  }

  private static String resolveOperatorOverload(InvocationResolver resolver) {
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

  private static Set<Invocable> createOperatorInvocables(
      BinaryOperator operator, Type left, Type right) {
    OperatorInvocableCollector factory = new OperatorInvocableCollector(TYPE_FACTORY);
    return factory.collect(operator, left, right);
  }

  private static Set<Invocable> createOperatorInvocables(UnaryOperator operator, Type operand) {
    OperatorInvocableCollector factory = new OperatorInvocableCollector(TYPE_FACTORY);
    return factory.collect(operator, operand);
  }

  private static Type getType(String name) {
    switch (name) {
      case "Int8":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.SHORTINT);
      case "Int16":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.SMALLINT);
      case "Int32":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.INTEGER);
      case "Int64":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.INT64);
      case "NativeInt":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.NATIVEINT);
      case "UInt8":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.BYTE);
      case "UInt16":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.WORD);
      case "UInt32":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.CARDINAL);
      case "UInt64":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.UINT64);
      case "NativeUInt":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.NATIVEUINT);
      case "UInt16_Subrange":
        return ((TypeFactoryImpl) TYPE_FACTORY).anonymousUInt15();
      case "UInt32_Subrange":
        return ((TypeFactoryImpl) TYPE_FACTORY).anonymousUInt31();
      case "Extended":
        return TYPE_FACTORY.getIntrinsic(IntrinsicType.EXTENDED);
      default:
        // do nothing
    }

    if (name.endsWith("_Subrange")) {
      var hostType = (IntegerType) getType(Strings.CS.removeEnd(name, "_Subrange"));
      BigInteger min;
      BigInteger max;

      if (name.contains("UInt")) {
        min = BigInteger.ZERO;
        max = hostType.max().divide(BigInteger.TWO);
      } else {
        min = hostType.min();
        max = BigInteger.ZERO;
      }

      return TYPE_FACTORY.subrange(name, min, max);
    }

    if (name.endsWith("_Alias")) {
      Type aliasedType = getType(Strings.CS.removeEnd(name, "_Alias"));
      return TYPE_FACTORY.strongAlias(name, aliasedType);
    }

    throw new AssertionError("Unknown type: " + name);
  }

  private static ExpressionNode mockExpressionNode(Type type) {
    ExpressionNode result = mock();
    when(result.getType()).thenReturn(type);
    return result;
  }
}
