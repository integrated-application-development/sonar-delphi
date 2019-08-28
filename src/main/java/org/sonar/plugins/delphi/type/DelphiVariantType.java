package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.type.Type.VariantType;

public class DelphiVariantType extends DelphiIntrinsicType implements VariantType {
  private static final VariantType VARIANT =
      new DelphiVariantType("Variant", VariantKind.NORMAL_VARIANT);

  private static final VariantType OLE_VARIANT =
      new DelphiVariantType("OleVariant", VariantKind.OLE_VARIANT);

  private final VariantKind kind;

  private DelphiVariantType(String image, VariantKind kind) {
    super(image);
    this.kind = kind;
  }

  public static VariantType variant() {
    return VARIANT;
  }

  public static VariantType oleVariant() {
    return OLE_VARIANT;
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
