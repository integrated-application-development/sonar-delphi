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
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public enum RoutineDirective {
  OVERLOAD(DelphiTokenType.OVERLOAD),
  REINTRODUCE(DelphiTokenType.REINTRODUCE),
  MESSAGE(DelphiTokenType.MESSAGE),
  STATIC(DelphiTokenType.STATIC),
  DYNAMIC(DelphiTokenType.DYNAMIC),
  OVERRIDE(DelphiTokenType.OVERRIDE),
  VIRTUAL(DelphiTokenType.VIRTUAL),
  ABSTRACT(DelphiTokenType.ABSTRACT),
  FINAL(DelphiTokenType.FINAL),
  INLINE(DelphiTokenType.INLINE),
  ASSEMBLER(DelphiTokenType.ASSEMBLER),
  CDECL(DelphiTokenType.CDECL),
  PASCAL(DelphiTokenType.PASCAL),
  REGISTER(DelphiTokenType.REGISTER),
  SAFECALL(DelphiTokenType.SAFECALL),
  STDCALL(DelphiTokenType.STDCALL),
  WINAPI(DelphiTokenType.WINAPI),
  EXPORT(DelphiTokenType.EXPORT),
  FAR(DelphiTokenType.FAR),
  LOCAL(DelphiTokenType.LOCAL),
  NEAR(DelphiTokenType.NEAR),
  DEPRECATED(DelphiTokenType.DEPRECATED),
  EXPERIMENTAL(DelphiTokenType.EXPERIMENTAL),
  PLATFORM(DelphiTokenType.PLATFORM),
  LIBRARY(DelphiTokenType.LIBRARY),
  VARARGS(DelphiTokenType.VARARGS),
  EXTERNAL(DelphiTokenType.EXTERNAL),
  NAME(DelphiTokenType.NAME),
  INDEX(DelphiTokenType.INDEX),
  DISPID(DelphiTokenType.DISPID);

  private static final Map<DelphiTokenType, RoutineDirective> TOKEN_TYPE_MAP =
      Arrays.stream(RoutineDirective.values())
          .collect(
              Maps.toImmutableEnumMap(directive -> directive.tokenType, directive -> directive));

  private final DelphiTokenType tokenType;

  RoutineDirective(DelphiTokenType tokenType) {
    this.tokenType = tokenType;
  }

  @Nullable
  public static RoutineDirective fromToken(DelphiToken token) {
    return TOKEN_TYPE_MAP.get(token.getType());
  }
}
