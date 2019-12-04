/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.core;

import java.util.Set;
import org.sonar.plugins.delphi.antlr.DelphiLexer;

/** DelphiLanguage keywords used for code colorization. */
public final class DelphiKeywords {

  public static final Set<Integer> KEYWORDS =
      Set.of(
          DelphiLexer.ABSTRACT,
          DelphiLexer.ADD,
          DelphiLexer.ALIGN,
          DelphiLexer.AND,
          DelphiLexer.ARRAY,
          DelphiLexer.AS,
          DelphiLexer.ASM,
          DelphiLexer.ASSEMBLER,
          DelphiLexer.AT,
          DelphiLexer.AUTOMATED,
          DelphiLexer.BEGIN,
          DelphiLexer.CASE,
          DelphiLexer.CDECL,
          DelphiLexer.CLASS,
          DelphiLexer.CONST,
          DelphiLexer.CONSTRUCTOR,
          DelphiLexer.CONTAINS,
          DelphiLexer.DEFAULT,
          DelphiLexer.DELAYED,
          DelphiLexer.DEPRECATED,
          DelphiLexer.DESTRUCTOR,
          DelphiLexer.DISPID,
          DelphiLexer.DISPINTERFACE,
          DelphiLexer.DIV,
          DelphiLexer.DO,
          DelphiLexer.DOWNTO,
          DelphiLexer.DYNAMIC,
          DelphiLexer.ELSE,
          DelphiLexer.END,
          DelphiLexer.EXCEPT,
          DelphiLexer.EXPERIMENTAL,
          DelphiLexer.EXPORT,
          DelphiLexer.EXPORTS,
          DelphiLexer.EXTERNAL,
          DelphiLexer.FAR,
          DelphiLexer.FILE,
          DelphiLexer.FINAL,
          DelphiLexer.FINALIZATION,
          DelphiLexer.FINALLY,
          DelphiLexer.FOR,
          DelphiLexer.FORWARD,
          DelphiLexer.FUNCTION,
          DelphiLexer.GOTO,
          DelphiLexer.HELPER,
          DelphiLexer.IF,
          DelphiLexer.IMPLEMENTATION,
          DelphiLexer.IMPLEMENTS,
          DelphiLexer.IN,
          DelphiLexer.INDEX,
          DelphiLexer.INHERITED,
          DelphiLexer.INITIALIZATION,
          DelphiLexer.INLINE,
          DelphiLexer.INTERFACE,
          DelphiLexer.IS,
          DelphiLexer.LABEL,
          DelphiLexer.LIBRARY,
          DelphiLexer.LOCAL,
          DelphiLexer.MESSAGE,
          DelphiLexer.MOD,
          DelphiLexer.NAME,
          DelphiLexer.NEAR,
          DelphiLexer.NIL,
          DelphiLexer.NODEFAULT,
          DelphiLexer.NOT,
          DelphiLexer.OBJECT,
          DelphiLexer.OF,
          DelphiLexer.ON,
          DelphiLexer.OPERATOR,
          DelphiLexer.OR,
          DelphiLexer.OUT,
          DelphiLexer.OVERLOAD,
          DelphiLexer.OVERRIDE,
          DelphiLexer.PACKAGE,
          DelphiLexer.PACKED,
          DelphiLexer.PASCAL,
          DelphiLexer.PLATFORM,
          DelphiLexer.PRIVATE,
          DelphiLexer.PROCEDURE,
          DelphiLexer.PROGRAM,
          DelphiLexer.PROPERTY,
          DelphiLexer.PROTECTED,
          DelphiLexer.PUBLIC,
          DelphiLexer.PUBLISHED,
          DelphiLexer.RAISE,
          DelphiLexer.READ,
          DelphiLexer.READONLY,
          DelphiLexer.RECORD,
          DelphiLexer.REFERENCE,
          DelphiLexer.REGISTER,
          DelphiLexer.REINTRODUCE,
          DelphiLexer.REMOVE,
          DelphiLexer.REPEAT,
          DelphiLexer.REQUIRES,
          DelphiLexer.RESOURCESTRING,
          DelphiLexer.SAFECALL,
          DelphiLexer.SEALED,
          DelphiLexer.SET,
          DelphiLexer.SHL,
          DelphiLexer.SHR,
          DelphiLexer.STATIC,
          DelphiLexer.STDCALL,
          DelphiLexer.STORED,
          DelphiLexer.STRICT,
          DelphiLexer.THEN,
          DelphiLexer.THREADVAR,
          DelphiLexer.TO,
          DelphiLexer.TRY,
          DelphiLexer.TYPE,
          DelphiLexer.UNIT,
          DelphiLexer.UNSAFE,
          DelphiLexer.UNTIL,
          DelphiLexer.USES,
          DelphiLexer.VAR,
          DelphiLexer.VARARGS,
          DelphiLexer.VIRTUAL,
          DelphiLexer.WITH,
          DelphiLexer.WRITE,
          DelphiLexer.WRITEONLY,
          DelphiLexer.XOR,
          DelphiLexer.ABSOLUTE,
          DelphiLexer.WHILE);

  public static final Set<Integer> SPECIAL_KEYWORDS =
      Set.of(DelphiLexer.BREAK, DelphiLexer.CONTINUE, DelphiLexer.EXIT, DelphiLexer.GOTO);

  private DelphiKeywords() {
    // const class
  }
}
