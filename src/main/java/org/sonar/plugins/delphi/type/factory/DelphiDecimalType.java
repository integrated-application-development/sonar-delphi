package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.DecimalType;

class DelphiDecimalType extends DelphiType implements DecimalType {
  private final String image;
  private final int size;

  DelphiDecimalType(String image, int size) {
    this.image = image;
    this.size = size;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public boolean isDecimal() {
    return true;
  }

  @Override
  public int size() {
    return size;
  }
}
