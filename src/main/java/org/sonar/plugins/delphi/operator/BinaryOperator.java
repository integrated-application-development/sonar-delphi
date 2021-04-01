package org.sonar.plugins.delphi.operator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.antlr.DelphiLexer;

public enum BinaryOperator implements Operator {
  AND(DelphiLexer.AND, "LogicalAnd", "BitwiseAnd"),
  OR(DelphiLexer.OR, "LogicalOr", "BitwiseOr"),
  XOR(DelphiLexer.XOR, "LogicalXor", "BitwiseXor"),
  EQUAL(DelphiLexer.EQUAL, "Equal"),
  GREATER_THAN(DelphiLexer.GT, "GreaterThan"),
  LESS_THAN(DelphiLexer.LT, "LessThan"),
  GREATER_THAN_EQUAL(DelphiLexer.GE, "GreaterThanOrEqual"),
  LESS_THAN_EQUAL(DelphiLexer.LE, "LessThanOrEqual"),
  NOT_EQUAL(DelphiLexer.NOT_EQUAL, "NotEqual"),
  IN(DelphiLexer.IN),
  ADD(DelphiLexer.PLUS, "Add"),
  SUBTRACT(DelphiLexer.MINUS, "Subtract"),
  MULTIPLY(DelphiLexer.STAR, "Multiply"),
  DIVIDE(DelphiLexer.SLASH, "Divide"),
  DIV(DelphiLexer.DIV, "IntDivide"),
  MOD(DelphiLexer.MOD, "Modulus"),
  SHL(DelphiLexer.SHL, "LeftShift"),
  SHR(DelphiLexer.SHR, "RightShift"),
  IS(DelphiLexer.IS),
  AS(DelphiLexer.AS);

  private static final Map<Integer, BinaryOperator> TOKEN_TYPE_MAP =
      Arrays.stream(BinaryOperator.values())
          .collect(Collectors.toUnmodifiableMap(op -> op.tokenType, op -> op));

  private final int tokenType;
  private final ImmutableSet<String> names;

  BinaryOperator(int tokenType, String... names) {
    this.tokenType = tokenType;
    this.names = ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER).add(names).build();
  }

  @Override
  public Set<String> getNames() {
    return names;
  }

  public static BinaryOperator from(int tokenType) {
    return Preconditions.checkNotNull(TOKEN_TYPE_MAP.get(tokenType));
  }
}
