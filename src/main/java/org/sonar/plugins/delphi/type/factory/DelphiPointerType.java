package org.sonar.plugins.delphi.type.factory;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

class DelphiPointerType extends DelphiType implements PointerType {
  private Type dereferencedType;
  private final int size;
  private boolean allowsPointerMath;

  DelphiPointerType(Type dereferencedType, int size, boolean allowsPointerMath) {
    this.dereferencedType = dereferencedType;
    this.size = size;
    this.allowsPointerMath = allowsPointerMath;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String getImage() {
    return "^" + dereferencedType.getImage();
  }

  @Override
  @NotNull
  public Type dereferencedType() {
    return dereferencedType;
  }

  @Override
  public boolean allowsPointerMath() {
    return allowsPointerMath;
  }

  @Override
  public void setDereferencedType(Type type) {
    this.dereferencedType = type;
  }

  @Override
  public void setAllowsPointerMath() {
    this.allowsPointerMath = true;
  }

  @Override
  public boolean isPointer() {
    return true;
  }

  @Override
  public boolean isNilPointer() {
    return dereferencedType.isVoid();
  }

  @Override
  public boolean isUntypedPointer() {
    return dereferencedType.isUntyped();
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (dereferencedType().isTypeParameter()) {
      return new DelphiPointerType(dereferencedType().specialize(context), size, allowsPointerMath);
    }
    return this;
  }
}
