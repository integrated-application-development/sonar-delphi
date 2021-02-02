package org.sonar.plugins.delphi.type.intrinsic;

import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;

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
  TEXT("Text");

  private final QualifiedName qualifiedName;

  IntrinsicType(String name) {
    this.qualifiedName = QualifiedName.of("System", name);
  }

  @Override
  public QualifiedName getQualifiedName() {
    return qualifiedName;
  }
}
