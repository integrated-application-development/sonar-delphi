package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.BooleanType;

@Immutable
class DelphiBooleanType extends DelphiType implements BooleanType {
  private final String image;
  private final int size;

  public DelphiBooleanType(String image, int size) {
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
  public boolean isBoolean() {
    return true;
  }
}
