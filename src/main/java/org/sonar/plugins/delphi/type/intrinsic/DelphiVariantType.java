package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.VariantType;

@Immutable
class DelphiVariantType extends DelphiType implements VariantType {
  private final String image;
  private final VariantKind kind;

  DelphiVariantType(String image, VariantKind kind) {
    this.image = image;
    this.kind = kind;
  }

  @Override
  public String getImage() {
    return image;
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
