package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type.PointerType;

public class DelphiPointerType extends DelphiType implements PointerType {
  private static final PointerType UNTYPED = new DelphiPointerType(DelphiType.untypedType());
  private static final PointerType NIL = new DelphiPointerType(DelphiType.voidType());

  private final Type dereferencedType;

  private DelphiPointerType(Type dereferencedType) {
    super("^" + dereferencedType.getImage());
    this.dereferencedType = dereferencedType;
  }

  public static PointerType pointerTo(Type type) {
    return new DelphiPointerType(type);
  }

  public static PointerType untypedPointer() {
    return UNTYPED;
  }

  public static PointerType nilPointer() {
    return NIL;
  }

  @Override
  @NotNull
  public Type dereferencedType() {
    return dereferencedType;
  }

  @Override
  public boolean isPointer() {
    return true;
  }

  @Override
  public boolean isNilPointer() {
    return this == NIL;
  }

  @Override
  public boolean isUntypedPointer() {
    return dereferencedType.isUntyped();
  }
}
