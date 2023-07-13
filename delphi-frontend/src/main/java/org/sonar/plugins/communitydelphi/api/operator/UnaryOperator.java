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

public enum UnaryOperator implements Operator {
  NOT(DelphiTokenType.NOT, "BitwiseNot", "LogicalNot"),
  PLUS(DelphiTokenType.PLUS, "Positive"),
  NEGATE(DelphiTokenType.MINUS, "Negative"),
  ADDRESS(DelphiTokenType.ADDRESS);

  private static final Map<DelphiTokenType, UnaryOperator> TOKEN_TYPE_MAP =
      Arrays.stream(UnaryOperator.values())
          .collect(Maps.toImmutableEnumMap(op -> op.tokenType, op -> op));

  private final DelphiTokenType tokenType;
  private final ImmutableSet<String> names;

  UnaryOperator(DelphiTokenType tokenType, String... names) {
    this.tokenType = tokenType;
    this.names = ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER).add(names).build();
  }

  @Override
  public Set<String> getNames() {
    return names;
  }

  public static UnaryOperator fromTokenType(DelphiTokenType tokenType) {
    UnaryOperator operator = TOKEN_TYPE_MAP.get(tokenType);
    if (operator != null) {
      return operator;
    }
    throw new AssertionError("Unhandled UnaryOperator token type: " + tokenType.name());
  }
}
