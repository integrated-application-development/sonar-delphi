package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.type.Type.TypeType;

public class DelphiTypeType extends DelphiType implements TypeType {
  private final Type originalType;

  private DelphiTypeType(String image, Type originalType) {
    super(image);
    this.originalType = originalType;
  }

  public static DelphiTypeType create(String image, Type originalType) {
    return new DelphiTypeType(image, originalType);
  }

  @Override
  public Type originalType() {
    return originalType;
  }

  @Override
  public boolean isTypeType() {
    return true;
  }
}
