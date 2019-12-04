package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.DecimalType;

@Immutable
class DelphiDecimalType extends DelphiType implements DecimalType {

  private final int size;

  DelphiDecimalType(String image, int size) {
    super(image);
    this.size = size;
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
