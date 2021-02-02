package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.VariantType;

class DelphiVariantType extends DelphiType implements VariantType {
  private final String image;
  private final int size;
  private final VariantKind kind;

  DelphiVariantType(String image, int size, VariantKind kind) {
    this.image = image;
    this.size = size;
    this.kind = kind;
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
  public boolean isVariant() {
    return true;
  }

  @Override
  public VariantKind kind() {
    return kind;
  }
}
