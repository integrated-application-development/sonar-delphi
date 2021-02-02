package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.TypeType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

class DelphiTypeType extends DelphiType implements TypeType {
  private final String image;
  private final Type originalType;

  DelphiTypeType(String image, Type originalType) {
    this.image = image;
    this.originalType = originalType;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    return originalType.size();
  }

  @Override
  public Type originalType() {
    return originalType;
  }

  @Override
  public boolean isTypeType() {
    return true;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (originalType.isTypeParameter()) {
      return new DelphiTypeType(getImage(), originalType.specialize(context));
    }
    return this;
  }
}
