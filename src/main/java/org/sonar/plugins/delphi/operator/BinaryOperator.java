package org.sonar.plugins.delphi.operator;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.delphi.antlr.DelphiLexer;

public enum BinaryOperator {
  AND(DelphiLexer.AND, true),
  OR(DelphiLexer.OR, true),
  EQUAL(DelphiLexer.EQUAL, true),
  GREATER_THAN(DelphiLexer.GT, true),
  LESS_THAN(DelphiLexer.LT, true),
  GREATER_THAN_EQUAL(DelphiLexer.GE, true),
  LESS_THAN_EQUAL(DelphiLexer.LE, true),
  NOT_EQUAL(DelphiLexer.NOT_EQUAL, true),
  IN(DelphiLexer.IN, true),
  IS(DelphiLexer.IS, true),
  XOR(DelphiLexer.XOR),
  ADD(DelphiLexer.PLUS),
  SUBTRACT(DelphiLexer.MINUS),
  MULTIPLY(DelphiLexer.STAR),
  DIVIDE(DelphiLexer.SLASH),
  DIV(DelphiLexer.DIV),
  MOD(DelphiLexer.MOD),
  SHL(DelphiLexer.SHL),
  SHR(DelphiLexer.SHR),
  AS(DelphiLexer.AS);

  private final int tokenType;
  public final boolean isLogicalOperator;
  private static final Map<Integer, BinaryOperator> TOKEN_TYPE_MAP = new HashMap<>();

  static {
    Arrays.asList(BinaryOperator.values()).forEach(op -> TOKEN_TYPE_MAP.put(op.tokenType, op));
  }

  BinaryOperator(int tokenType) {
    this(tokenType, false);
  }

  BinaryOperator(int tokenType, boolean isLogicalOperator) {
    this.tokenType = tokenType;
    this.isLogicalOperator = isLogicalOperator;
  }

  public static BinaryOperator from(int tokenType) {
    return Preconditions.checkNotNull(TOKEN_TYPE_MAP.get(tokenType));
  }
}
