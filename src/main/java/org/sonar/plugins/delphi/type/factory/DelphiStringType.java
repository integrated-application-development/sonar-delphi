package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.StringType;

class DelphiStringType extends DelphiType implements StringType {
  private final String image;
  private final int size;
  private final CharacterType characterType;

  DelphiStringType(String image, int size, CharacterType characterType) {
    this.image = image;
    this.size = size;
    this.characterType = characterType;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public CharacterType characterType() {
    return characterType;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isString() {
    return true;
  }
}
