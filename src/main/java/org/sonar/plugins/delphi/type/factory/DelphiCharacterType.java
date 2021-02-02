package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.CharacterType;

class DelphiCharacterType extends DelphiType implements CharacterType {
  private final String image;
  private final int size;

  DelphiCharacterType(String image, int size) {
    this.image = image;
    this.size = size;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isChar() {
    return true;
  }
}
