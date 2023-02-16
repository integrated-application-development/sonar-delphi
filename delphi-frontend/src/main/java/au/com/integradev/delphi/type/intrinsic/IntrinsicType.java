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
package au.com.integradev.delphi.type.intrinsic;

import au.com.integradev.delphi.symbol.QualifiedNameImpl;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;

public enum IntrinsicType implements Qualifiable {
  BOOLEAN("Boolean"),
  BYTEBOOL("ByteBool"),
  WORDBOOL("WordBool"),
  LONGBOOL("LongBool"),
  SINGLE("Single"),
  DOUBLE("Double"),
  REAL("Real"),
  REAL48("Real48"),
  COMP("Comp"),
  CURRENCY("Currency"),
  EXTENDED("Extended"),
  SHORTINT("ShortInt"),
  BYTE("Byte"),
  SMALLINT("SmallInt"),
  WORD("Word"),
  INTEGER("Integer"),
  CARDINAL("Cardinal"),
  INT64("Int64"),
  UINT64("UInt64"),
  LONGINT("LongInt"),
  LONGWORD("LongWord"),
  NATIVEINT("NativeInt"),
  NATIVEUINT("NativeUInt"),
  ANSISTRING("AnsiString"),
  WIDESTRING("WideString"),
  UNICODESTRING("UnicodeString"),
  SHORTSTRING("ShortString"),
  STRING("String"),
  ANSICHAR("AnsiChar"),
  WIDECHAR("WideChar"),
  CHAR("Char"),
  POINTER("Pointer"),
  PWIDECHAR("PWideChar"),
  PANSICHAR("PAnsiChar"),
  PCHAR("PChar"),
  VARIANT("Variant"),
  OLEVARIANT("OleVariant"),
  TEXT("Text"),
  TEXTFILE("TextFile");

  private final QualifiedName qualifiedName;

  IntrinsicType(String name) {
    this.qualifiedName = QualifiedNameImpl.of("System", name);
  }

  @Override
  public QualifiedName getQualifiedName() {
    return qualifiedName;
  }
}
