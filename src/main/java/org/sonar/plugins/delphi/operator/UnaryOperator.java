package org.sonar.plugins.delphi.operator;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.EXTENDED;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INT64;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.VARIANT;

import com.google.common.collect.ImmutableSetMultimap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

public enum UnaryOperator implements Operator {
  NOT(DelphiLexer.NOT, createNotSignatures()),
  PLUS(DelphiLexer.PLUS, createArithmeticSignatures("Positive")),
  NEGATE(DelphiLexer.MINUS, createArithmeticSignatures("Negative")),
  ADDRESS(DelphiLexer.AT2);

  private final OperatorData data;

  private static final Map<Integer, UnaryOperator> TOKEN_TYPE_MAP =
      Arrays.stream(UnaryOperator.values())
          .collect(Collectors.toUnmodifiableMap(op -> op.getData().getTokenType(), op -> op));

  private static final ImmutableSetMultimap<UnaryOperator, Invocable> VARIANT_SIGNATURE_MAP =
      Arrays.stream(UnaryOperator.values())
          .collect(
              ImmutableSetMultimap.toImmutableSetMultimap(
                  operator -> operator, UnaryOperator::createVariantSignature));

  UnaryOperator(int tokenType, IntrinsicOperatorSignature... intrinsicOperatorSignatures) {
    this.data = new OperatorData(tokenType, intrinsicOperatorSignatures);
  }

  @NotNull
  public static UnaryOperator from(int tokenType) {
    return TOKEN_TYPE_MAP.get(tokenType);
  }

  @Override
  public OperatorData getData() {
    return data;
  }

  public static Set<Invocable> getVariantSignatures(UnaryOperator operator) {
    return VARIANT_SIGNATURE_MAP.get(operator);
  }

  private static IntrinsicOperatorSignature[] createNotSignatures() {
    return ArrayUtils.addAll(
        createIntegerSignatures("BitwiseNot"),
        new IntrinsicOperatorSignature("LogicalNot", List.of(BOOLEAN.type), BOOLEAN.type));
  }

  private static IntrinsicOperatorSignature[] createArithmeticSignatures(String name) {
    return ArrayUtils.addAll(
        createIntegerSignatures(name),
        new IntrinsicOperatorSignature(name, List.of(EXTENDED.type), EXTENDED.type));
  }

  private static IntrinsicOperatorSignature[] createIntegerSignatures(String name) {
    return ArrayUtils.toArray(
        new IntrinsicOperatorSignature(name, List.of(INTEGER.type), INTEGER.type),
        new IntrinsicOperatorSignature(name, List.of(INT64.type), INT64.type));
  }

  private static Invocable createVariantSignature(UnaryOperator operator) {
    final String PREFIX = "Variant::";
    final List<ImmutableType> arguments = List.of(VARIANT.type, VARIANT.type);
    ImmutableType returnType = VARIANT.type;
    return new IntrinsicOperatorSignature(PREFIX + operator.name(), arguments, returnType);
  }
}
