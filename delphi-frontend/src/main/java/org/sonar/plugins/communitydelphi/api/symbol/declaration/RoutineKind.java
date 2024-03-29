/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public enum RoutineKind {
  CONSTRUCTOR(DelphiTokenType.CONSTRUCTOR),
  DESTRUCTOR(DelphiTokenType.DESTRUCTOR),
  FUNCTION(DelphiTokenType.FUNCTION),
  OPERATOR(DelphiTokenType.OPERATOR),
  PROCEDURE(DelphiTokenType.PROCEDURE);

  private static final Map<DelphiTokenType, RoutineKind> TOKEN_TYPE_MAP =
      Arrays.stream(RoutineKind.values())
          .collect(Maps.toImmutableEnumMap(kind -> kind.tokenType, kind -> kind));
  private final DelphiTokenType tokenType;

  RoutineKind(DelphiTokenType tokenType) {
    this.tokenType = tokenType;
  }

  public static RoutineKind fromTokenType(DelphiTokenType tokenType) {
    RoutineKind kind = TOKEN_TYPE_MAP.get(tokenType);
    if (kind != null) {
      return kind;
    }
    throw new AssertionError("Unhandled RoutineKind token type: " + tokenType.name());
  }
}
