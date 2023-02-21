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
package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import au.com.integradev.delphi.antlr.DelphiLexer;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import java.util.HashMap;
import java.util.Map;

public enum MethodDirective {
  OVERLOAD(DelphiLexer.OVERLOAD),
  REINTRODUCE(DelphiLexer.REINTRODUCE),
  MESSAGE(DelphiLexer.MESSAGE),
  STATIC(DelphiLexer.STATIC),
  DYNAMIC(DelphiLexer.DYNAMIC),
  OVERRIDE(DelphiLexer.OVERRIDE),
  VIRTUAL(DelphiLexer.VIRTUAL),
  ABSTRACT(DelphiLexer.ABSTRACT),
  FINAL(DelphiLexer.FINAL),
  INLINE(DelphiLexer.INLINE),
  ASSEMBLER(DelphiLexer.ASSEMBLER),
  CDECL(DelphiLexer.CDECL),
  PASCAL(DelphiLexer.PASCAL),
  REGISTER(DelphiLexer.REGISTER),
  SAFECALL(DelphiLexer.SAFECALL),
  STDCALL(DelphiLexer.STDCALL),
  EXPORT(DelphiLexer.EXPORT),
  FAR(DelphiLexer.FAR),
  LOCAL(DelphiLexer.LOCAL),
  NEAR(DelphiLexer.NEAR),
  DEPRECATED(DelphiLexer.DEPRECATED),
  EXPERIMENTAL(DelphiLexer.EXPERIMENTAL),
  PLATFORM(DelphiLexer.PLATFORM),
  LIBRARY(DelphiLexer.LIBRARY),
  VARARGS(DelphiLexer.VARARGS),
  EXTERNAL(DelphiLexer.EXTERNAL),
  NAME(DelphiLexer.NAME),
  INDEX(DelphiLexer.INDEX),
  DISPID(DelphiLexer.DISPID);

  private static final Map<Integer, MethodDirective> TOKEN_TYPE_MAP = new HashMap<>();

  static {
    for (MethodDirective directive : MethodDirective.values()) {
      TOKEN_TYPE_MAP.put(directive.tokenType, directive);
    }
  }

  private final int tokenType;

  MethodDirective(int tokenType) {
    this.tokenType = tokenType;
  }

  public static MethodDirective fromToken(DelphiToken token) {
    return TOKEN_TYPE_MAP.get(token.getType());
  }
}
