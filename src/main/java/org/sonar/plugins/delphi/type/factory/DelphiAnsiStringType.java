package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.Type.AnsiStringType;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

class DelphiAnsiStringType extends DelphiStringType implements AnsiStringType {
  private final int codePage;

  DelphiAnsiStringType(int size, CharacterType characterType, int codePage) {
    super(null, size, characterType);
    this.codePage = codePage;
  }

  @Override
  public String getImage() {
    String image = IntrinsicType.ANSISTRING.fullyQualifiedName();
    if (codePage != 0) {
      image += "(" + codePage + ")";
    }
    return image;
  }

  @Override
  public boolean isAnsiString() {
    return true;
  }

  @Override
  public int codePage() {
    return codePage;
  }
}
