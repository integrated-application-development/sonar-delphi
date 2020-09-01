package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public abstract class DelphiPointerType extends DelphiType implements PointerType {
  public static PointerType pointerTo(Type type) {
    return new MutableDelphiPointerType(type);
  }

  public static ImmutablePointerType pointerTo(ImmutableType type, boolean allowsPointerMath) {
    return new ImmutableDelphiPointerType(type, allowsPointerMath);
  }

  public static ImmutablePointerType untypedPointer() {
    return ImmutableDelphiPointerType.UNTYPED;
  }

  public static ImmutablePointerType nilPointer() {
    return ImmutableDelphiPointerType.NIL;
  }

  @Override
  public String getImage() {
    return "^" + dereferencedType().getImage();
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

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (dereferencedType().isTypeParameter()) {
      return pointerTo(dereferencedType().specialize(context));
    }
    return this;
  }

  private static class MutableDelphiPointerType extends DelphiPointerType {
    private Type dereferencedType;
    private boolean allowsPointerMath;

    MutableDelphiPointerType(Type dereferencedType) {
      this.dereferencedType = dereferencedType;
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
  }

  @Immutable
  private static class ImmutableDelphiPointerType extends DelphiPointerType
      implements ImmutablePointerType {
    private static final ImmutablePointerType UNTYPED =
        new ImmutableDelphiPointerType(DelphiType.untypedType(), false);

    private static final ImmutablePointerType NIL =
        new ImmutableDelphiPointerType(DelphiType.voidType(), false);

    private final ImmutableType dereferencedType;
    private final boolean allowsPointerMath;

    ImmutableDelphiPointerType(ImmutableType dereferencedType, boolean allowsPointerMath) {
      this.dereferencedType = dereferencedType;
      this.allowsPointerMath = allowsPointerMath;
    }

    @Override
    @NotNull
    public ImmutableType dereferencedType() {
      return dereferencedType;
    }

    @Override
    public boolean allowsPointerMath() {
      return allowsPointerMath;
    }

    @Override
    public void setDereferencedType(Type type) {
      throw new UnsupportedOperationException("Not allowed on immutable type!");
    }

    @Override
    public void setAllowsPointerMath() {
      throw new UnsupportedOperationException("Not allowed on immutable type!");
    }
  }
}
