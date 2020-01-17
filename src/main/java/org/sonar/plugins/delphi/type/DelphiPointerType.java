package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type.PointerType;

public abstract class DelphiPointerType extends DelphiType implements PointerType {
  private DelphiPointerType(String typeImage) {
    super("^" + typeImage);
  }

  public static PointerType pointerTo(Type type) {
    return new MutableDelphiPointerType(type);
  }

  public static ImmutablePointerType pointerTo(ImmutableType type) {
    return new ImmutableDelphiPointerType(type);
  }

  public static ImmutablePointerType untypedPointer() {
    return ImmutableDelphiPointerType.UNTYPED;
  }

  public static ImmutablePointerType nilPointer() {
    return ImmutableDelphiPointerType.NIL;
  }

  @Override
  public boolean isPointer() {
    return true;
  }

  @Override
  public boolean isNilPointer() {
    return this == ImmutableDelphiPointerType.NIL;
  }

  @Override
  public boolean isUntypedPointer() {
    return dereferencedType().isUntyped();
  }

  private static class MutableDelphiPointerType extends DelphiPointerType {
    private Type dereferencedType;

    MutableDelphiPointerType(Type dereferencedType) {
      super(dereferencedType.getImage());
      this.dereferencedType = dereferencedType;
    }

    @Override
    @NotNull
    public Type dereferencedType() {
      return dereferencedType;
    }

    @Override
    public void setDereferencedType(Type type) {
      this.dereferencedType = type;
    }
  }

  @Immutable
  private static class ImmutableDelphiPointerType extends DelphiPointerType
      implements ImmutablePointerType {
    private static final ImmutablePointerType UNTYPED =
        new ImmutableDelphiPointerType(DelphiType.untypedType());

    private static final ImmutablePointerType NIL =
        new ImmutableDelphiPointerType(DelphiType.voidType());

    private final ImmutableType dereferencedType;

    ImmutableDelphiPointerType(ImmutableType dereferencedType) {
      super(dereferencedType.getImage());
      this.dereferencedType = dereferencedType;
    }

    @Override
    @NotNull
    public ImmutableType dereferencedType() {
      return dereferencedType;
    }

    @Override
    public void setDereferencedType(Type type) {
      throw new UnsupportedOperationException("Not allowed on immutable type!");
    }
  }
}
