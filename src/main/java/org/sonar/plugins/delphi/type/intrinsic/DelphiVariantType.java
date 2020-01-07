package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.VariantType;

@Immutable
class DelphiVariantType extends DelphiType implements VariantType {

  private final VariantKind kind;

  DelphiVariantType(String image, VariantKind kind) {
    super(image);
    this.kind = kind;
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
