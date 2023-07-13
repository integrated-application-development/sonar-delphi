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
package au.com.integradev.delphi.core;

import com.google.common.collect.Sets;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

/** DelphiLanguage keywords used for code colorization. */
public final class DelphiKeywords {
  public static final Set<DelphiTokenType> KEYWORDS =
      Sets.immutableEnumSet(
          DelphiTokenType.ABSTRACT,
          DelphiTokenType.ADD,
          DelphiTokenType.ALIGN,
          DelphiTokenType.AND,
          DelphiTokenType.ARRAY,
          DelphiTokenType.AS,
          DelphiTokenType.ASM,
          DelphiTokenType.ASSEMBLER,
          DelphiTokenType.AT,
          DelphiTokenType.AUTOMATED,
          DelphiTokenType.BEGIN,
          DelphiTokenType.CASE,
          DelphiTokenType.CDECL,
          DelphiTokenType.CLASS,
          DelphiTokenType.CONST,
          DelphiTokenType.CONSTRUCTOR,
          DelphiTokenType.CONTAINS,
          DelphiTokenType.DEFAULT,
          DelphiTokenType.DELAYED,
          DelphiTokenType.DEPRECATED,
          DelphiTokenType.DESTRUCTOR,
          DelphiTokenType.DISPID,
          DelphiTokenType.DISPINTERFACE,
          DelphiTokenType.DIV,
          DelphiTokenType.DO,
          DelphiTokenType.DOWNTO,
          DelphiTokenType.DYNAMIC,
          DelphiTokenType.ELSE,
          DelphiTokenType.END,
          DelphiTokenType.EXCEPT,
          DelphiTokenType.EXPERIMENTAL,
          DelphiTokenType.EXPORT,
          DelphiTokenType.EXPORTS,
          DelphiTokenType.EXTERNAL,
          DelphiTokenType.FAR,
          DelphiTokenType.FILE,
          DelphiTokenType.FINAL,
          DelphiTokenType.FINALIZATION,
          DelphiTokenType.FINALLY,
          DelphiTokenType.FOR,
          DelphiTokenType.FORWARD,
          DelphiTokenType.FUNCTION,
          DelphiTokenType.GOTO,
          DelphiTokenType.HELPER,
          DelphiTokenType.IF,
          DelphiTokenType.IMPLEMENTATION,
          DelphiTokenType.IMPLEMENTS,
          DelphiTokenType.IN,
          DelphiTokenType.INDEX,
          DelphiTokenType.INHERITED,
          DelphiTokenType.INITIALIZATION,
          DelphiTokenType.INLINE,
          DelphiTokenType.INTERFACE,
          DelphiTokenType.IS,
          DelphiTokenType.LABEL,
          DelphiTokenType.LIBRARY,
          DelphiTokenType.LOCAL,
          DelphiTokenType.MESSAGE,
          DelphiTokenType.MOD,
          DelphiTokenType.NAME,
          DelphiTokenType.NEAR,
          DelphiTokenType.NIL,
          DelphiTokenType.NODEFAULT,
          DelphiTokenType.NOT,
          DelphiTokenType.OBJECT,
          DelphiTokenType.OF,
          DelphiTokenType.ON,
          DelphiTokenType.OPERATOR,
          DelphiTokenType.OR,
          DelphiTokenType.OUT,
          DelphiTokenType.OVERLOAD,
          DelphiTokenType.OVERRIDE,
          DelphiTokenType.PACKAGE,
          DelphiTokenType.PACKED,
          DelphiTokenType.PASCAL,
          DelphiTokenType.PLATFORM,
          DelphiTokenType.PRIVATE,
          DelphiTokenType.PROCEDURE,
          DelphiTokenType.PROGRAM,
          DelphiTokenType.PROPERTY,
          DelphiTokenType.PROTECTED,
          DelphiTokenType.PUBLIC,
          DelphiTokenType.PUBLISHED,
          DelphiTokenType.RAISE,
          DelphiTokenType.READ,
          DelphiTokenType.READONLY,
          DelphiTokenType.RECORD,
          DelphiTokenType.REFERENCE,
          DelphiTokenType.REGISTER,
          DelphiTokenType.REINTRODUCE,
          DelphiTokenType.REMOVE,
          DelphiTokenType.REPEAT,
          DelphiTokenType.REQUIRES,
          DelphiTokenType.RESOURCESTRING,
          DelphiTokenType.SAFECALL,
          DelphiTokenType.SEALED,
          DelphiTokenType.SET,
          DelphiTokenType.SHL,
          DelphiTokenType.SHR,
          DelphiTokenType.STATIC,
          DelphiTokenType.STDCALL,
          DelphiTokenType.STORED,
          DelphiTokenType.STRICT,
          DelphiTokenType.STRING,
          DelphiTokenType.THEN,
          DelphiTokenType.THREADVAR,
          DelphiTokenType.TO,
          DelphiTokenType.TRY,
          DelphiTokenType.TYPE,
          DelphiTokenType.UNIT,
          DelphiTokenType.UNSAFE,
          DelphiTokenType.UNTIL,
          DelphiTokenType.USES,
          DelphiTokenType.VAR,
          DelphiTokenType.VARARGS,
          DelphiTokenType.VIRTUAL,
          DelphiTokenType.WITH,
          DelphiTokenType.WRITE,
          DelphiTokenType.WRITEONLY,
          DelphiTokenType.XOR,
          DelphiTokenType.ABSOLUTE,
          DelphiTokenType.WHILE);

  private DelphiKeywords() {
    // const class
  }
}
