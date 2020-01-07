package org.sonar.plugins.delphi.type.intrinsic;

import org.sonar.plugins.delphi.type.Type.VariantType;
import org.sonar.plugins.delphi.type.Type.VariantType.VariantKind;

public enum IntrinsicVariant {
  VARIANT("Variant", VariantKind.NORMAL_VARIANT),
  OLE_VARIANT("OleVariant", VariantKind.OLE_VARIANT);

  public final VariantType type;

  IntrinsicVariant(String image, VariantKind kind) {
    this.type = new DelphiVariantType(image, kind);
  }
}
