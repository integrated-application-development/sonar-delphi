package org.sonar.plugins.delphi.operator;

import static java.util.function.Predicate.not;
import static org.sonar.plugins.delphi.type.DelphiType.untypedType;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.POINTER_MATH_OPERAND;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.EXTENDED;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INT64;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.VARIANT;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.DelphiSetType;
import org.sonar.plugins.delphi.type.Type.ImmutableType;
import org.sonar.plugins.delphi.type.Type.PointerType;

public enum BinaryOperator implements Operator {
  AND(DelphiLexer.AND, createLogicalAndBitwiseSignatures("And")),
  OR(DelphiLexer.OR, createLogicalAndBitwiseSignatures("Or")),
  XOR(DelphiLexer.XOR, createLogicalAndBitwiseSignatures("Xor")),
  EQUAL(DelphiLexer.EQUAL, createComparisonSignature("Equal")),
  GREATER_THAN(DelphiLexer.GT, createComparisonSignature("GreaterThan")),
  LESS_THAN(DelphiLexer.LT, createComparisonSignature("LessThan")),
  GREATER_THAN_EQUAL(DelphiLexer.GE, createComparisonSignature("GreaterThanOrEqual")),
  LESS_THAN_EQUAL(DelphiLexer.LE, createComparisonSignature("LessThanOrEqual")),
  NOT_EQUAL(DelphiLexer.NOT_EQUAL, createComparisonSignature("NotEqual")),
  IN(DelphiLexer.IN, createInSignature()),
  ADD(DelphiLexer.PLUS, createAddSignatures()),
  SUBTRACT(DelphiLexer.MINUS, createArithmeticAndSetSignatures("Subtract")),
  MULTIPLY(DelphiLexer.STAR, createArithmeticAndSetSignatures("Multiply")),
  DIVIDE(DelphiLexer.SLASH, createDivideSignature()),
  DIV(DelphiLexer.DIV, createIntegerSignatures("IntDivide")),
  MOD(DelphiLexer.MOD, createIntegerSignatures("Modulus")),
  SHL(DelphiLexer.SHL, createIntegerSignatures("LeftShift")),
  SHR(DelphiLexer.SHR, createIntegerSignatures("LeftShift")),
  IS(DelphiLexer.IS),
  AS(DelphiLexer.AS);

  private final OperatorData data;

  private static final Map<Integer, BinaryOperator> TOKEN_TYPE_MAP =
      Arrays.stream(BinaryOperator.values())
          .collect(Collectors.toUnmodifiableMap(op -> op.getData().getTokenType(), op -> op));

  private static final ImmutableSetMultimap<BinaryOperator, Invocable> VARIANT_SIGNATURE_MAP =
      Arrays.stream(BinaryOperator.values())
          .filter(not(IN::equals))
          .filter(not(IS::equals))
          .collect(
              ImmutableSetMultimap.toImmutableSetMultimap(
                  operator -> operator, BinaryOperator::createVariantSignature));

  BinaryOperator(int tokenType, IntrinsicOperatorSignature... intrinsicOperatorSignatures) {
    this.data = new OperatorData(tokenType, intrinsicOperatorSignatures);
  }

  @Override
  public OperatorData getData() {
    return data;
  }

  public static BinaryOperator from(int tokenType) {
    return Preconditions.checkNotNull(TOKEN_TYPE_MAP.get(tokenType));
  }

  public static Set<Invocable> getVariantSignatures(BinaryOperator operator) {
    return VARIANT_SIGNATURE_MAP.get(operator);
  }

  public static Set<Invocable> createPointerMathAddSignaturesFor(PointerType type) {
    final String NAME = "Add";
    return Set.of(
        new PointerMathOperatorSignature(NAME, List.of(type, INTEGER.type), type),
        new PointerMathOperatorSignature(NAME, List.of(INTEGER.type, type), type),
        new PointerMathOperatorSignature(NAME, List.of(type, POINTER_MATH_OPERAND), type));
  }

  public static Set<Invocable> createPointerMathSubtractSignaturesFor(PointerType type) {
    final String NAME = "Subtract";
    return Set.of(
        new PointerMathOperatorSignature(NAME, List.of(type, INTEGER.type), type),
        new PointerMathOperatorSignature(NAME, List.of(type, POINTER_MATH_OPERAND), INTEGER.type));
  }

  private static IntrinsicOperatorSignature[] createArithmeticSignatures(String name) {
    return ArrayUtils.addAll(
        createIntegerSignatures(name),
        new IntrinsicOperatorSignature(name, List.of(EXTENDED.type, EXTENDED.type), EXTENDED.type),
        new IntrinsicOperatorSignature(name, List.of(INTEGER.type, EXTENDED.type), EXTENDED.type),
        new IntrinsicOperatorSignature(name, List.of(EXTENDED.type, INTEGER.type), EXTENDED.type));
  }

  private static IntrinsicOperatorSignature[] createArithmeticAndSetSignatures(String name) {
    return ArrayUtils.addAll(createArithmeticSignatures(name), createSetSignature(name));
  }

  private static IntrinsicOperatorSignature[] createAddSignatures() {
    final String NAME = "Add";
    return ArrayUtils.addAll(
        createArithmeticAndSetSignatures(NAME),
        new IntrinsicOperatorSignature(
            NAME, List.of(UNICODESTRING.type, UNICODESTRING.type), UNICODESTRING.type));
  }

  private static IntrinsicOperatorSignature createDivideSignature() {
    return new IntrinsicOperatorSignature(
        "Divide", List.of(EXTENDED.type, EXTENDED.type), EXTENDED.type);
  }

  private static IntrinsicOperatorSignature[] createLogicalAndBitwiseSignatures(String suffix) {
    return ArrayUtils.addAll(
        createIntegerSignatures("Bitwise" + suffix), createLogicalSignature("Logical" + suffix));
  }

  private static IntrinsicOperatorSignature createLogicalSignature(String name) {
    return new IntrinsicOperatorSignature(name, List.of(BOOLEAN.type, BOOLEAN.type), BOOLEAN.type);
  }

  private static IntrinsicOperatorSignature createComparisonSignature(String name) {
    return new IntrinsicOperatorSignature(
        name, List.of(untypedType(), untypedType()), BOOLEAN.type);
  }

  private static IntrinsicOperatorSignature createSetSignature(String name) {
    return new IntrinsicOperatorSignature(
        name, List.of(ANY_SET, ANY_SET), DelphiSetType.emptySet());
  }

  private static IntrinsicOperatorSignature createInSignature() {
    return new IntrinsicOperatorSignature("In", List.of(ANY_ORDINAL, ANY_SET), BOOLEAN.type);
  }

  private static IntrinsicOperatorSignature[] createIntegerSignatures(String name) {
    return ArrayUtils.toArray(
        new IntrinsicOperatorSignature(name, List.of(INTEGER.type, INTEGER.type), INTEGER.type),
        new IntrinsicOperatorSignature(name, List.of(INTEGER.type, INT64.type), INT64.type),
        new IntrinsicOperatorSignature(name, List.of(INT64.type, INTEGER.type), INT64.type),
        new IntrinsicOperatorSignature(name, List.of(INT64.type, INT64.type), INT64.type));
  }

  private static Invocable createVariantSignature(BinaryOperator operator) {
    final String PREFIX = "Variant::";
    final List<ImmutableType> arguments = List.of(VARIANT.type, VARIANT.type);
    ImmutableType returnType;
    switch (operator) {
      case EQUAL:
      case NOT_EQUAL:
      case LESS_THAN:
      case LESS_THAN_EQUAL:
      case GREATER_THAN:
      case GREATER_THAN_EQUAL:
        returnType = BOOLEAN.type;
        break;
      default:
        returnType = VARIANT.type;
    }
    return new IntrinsicOperatorSignature(PREFIX + operator.name(), arguments, returnType);
  }
}
