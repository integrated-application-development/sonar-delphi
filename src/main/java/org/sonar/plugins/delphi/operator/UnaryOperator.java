package org.sonar.plugins.delphi.operator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;

public enum UnaryOperator {
  NOT(DelphiLexer.NOT),
  PLUS(DelphiLexer.PLUS),
  NEGATE(DelphiLexer.MINUS),
  ADDRESS(DelphiLexer.AT2);

  public final int tokenType;
  private static final Map<Integer, UnaryOperator> TOKEN_TYPE_MAP = new HashMap<>();

  static {
    Arrays.asList(UnaryOperator.values()).forEach(op -> TOKEN_TYPE_MAP.put(op.tokenType, op));
  }

  UnaryOperator(int tokenType) {
    this.tokenType = tokenType;
  }

  @NotNull
  public static UnaryOperator from(int tokenType) {
    return TOKEN_TYPE_MAP.get(tokenType);
  }
}
