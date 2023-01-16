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
package org.sonar.plugins.communitydelphi.operator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.antlr.DelphiLexer;

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
