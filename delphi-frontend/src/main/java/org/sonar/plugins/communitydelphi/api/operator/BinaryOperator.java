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
package org.sonar.plugins.communitydelphi.api.operator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public enum BinaryOperator implements Operator {
  AND(DelphiTokenType.AND, "LogicalAnd", "BitwiseAnd"),
  OR(DelphiTokenType.OR, "LogicalOr", "BitwiseOr"),
  XOR(DelphiTokenType.XOR, "LogicalXor", "BitwiseXor"),
  EQUAL(DelphiTokenType.EQUAL, "Equal"),
  GREATER_THAN(DelphiTokenType.GREATER_THAN, "GreaterThan"),
  LESS_THAN(DelphiTokenType.LESS_THAN, "LessThan"),
  GREATER_THAN_EQUAL(DelphiTokenType.GREATER_THAN_EQUAL, "GreaterThanOrEqual"),
  LESS_THAN_EQUAL(DelphiTokenType.LESS_THAN_EQUAL, "LessThanOrEqual"),
  NOT_EQUAL(DelphiTokenType.NOT_EQUAL, "NotEqual"),
  IN(DelphiTokenType.IN),
  ADD(DelphiTokenType.PLUS, "Add"),
  SUBTRACT(DelphiTokenType.MINUS, "Subtract"),
  MULTIPLY(DelphiTokenType.MULTIPLY, "Multiply"),
  DIVIDE(DelphiTokenType.DIVIDE, "Divide"),
  DIV(DelphiTokenType.DIV, "IntDivide"),
  MOD(DelphiTokenType.MOD, "Modulus"),
  SHL(DelphiTokenType.SHL, "LeftShift"),
  SHR(DelphiTokenType.SHR, "RightShift"),
  IS(DelphiTokenType.IS),
  AS(DelphiTokenType.AS);

  private static final Map<DelphiTokenType, BinaryOperator> TOKEN_TYPE_MAP =
      Arrays.stream(BinaryOperator.values())
          .collect(Maps.toImmutableEnumMap(op -> op.tokenType, op -> op));

  private final DelphiTokenType tokenType;
  private final ImmutableSet<String> names;

  BinaryOperator(DelphiTokenType tokenType, String... names) {
    this.tokenType = tokenType;
    this.names = ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER).add(names).build();
  }

  @Override
  public Set<String> getNames() {
    return names;
  }

  public static BinaryOperator fromTokenType(DelphiTokenType tokenType) {
    BinaryOperator operator = TOKEN_TYPE_MAP.get(tokenType);
    if (operator != null) {
      return operator;
    }
    throw new AssertionError("Unhandled BinaryOperator token type: " + tokenType.name());
  }
}
