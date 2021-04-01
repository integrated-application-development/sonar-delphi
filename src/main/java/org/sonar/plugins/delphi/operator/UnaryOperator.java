package org.sonar.plugins.delphi.operator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;

public enum UnaryOperator implements Operator {
  NOT(DelphiLexer.NOT, "BitwiseNot", "LogicalNot"),
  PLUS(DelphiLexer.PLUS, "Positive"),
  NEGATE(DelphiLexer.MINUS, "Negative"),
  ADDRESS(DelphiLexer.AT2);

  private static final Map<Integer, UnaryOperator> TOKEN_TYPE_MAP =
      Arrays.stream(UnaryOperator.values())
          .collect(Collectors.toUnmodifiableMap(op -> op.tokenType, op -> op));

  private final int tokenType;
  private final ImmutableSet<String> names;

  UnaryOperator(int tokenType, String... names) {
    this.tokenType = tokenType;
    this.names = ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER).add(names).build();
  }

  @Override
  public Set<String> getNames() {
    return names;
  }

  @NotNull
  public static UnaryOperator from(int tokenType) {
    return TOKEN_TYPE_MAP.get(tokenType);
  }
}
