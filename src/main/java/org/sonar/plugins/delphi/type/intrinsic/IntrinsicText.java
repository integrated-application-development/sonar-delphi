package org.sonar.plugins.delphi.type.intrinsic;

import org.sonar.plugins.delphi.type.Type.TextType;

public enum IntrinsicText {
  ANSISTRING("AnsiString"),
  WIDESTRING("WideString"),
  UNICODESTRING("UnicodeString"),
  STRING("String", UNICODESTRING.type),
  SHORTSTRING("ShortString"),
  CHAR("Char"),
  WIDECHAR("WideChar"),
  ANSICHAR("AnsiChar");

  public final String image;
  public final TextType type;

  IntrinsicText(String image) {
    this(image, new DelphiTextType(image));
  }

  IntrinsicText(String image, TextType type) {
    this.image = image;
    this.type = type;
  }
}
