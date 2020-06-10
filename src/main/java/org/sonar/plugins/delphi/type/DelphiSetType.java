package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type.CollectionType;

public abstract class DelphiSetType extends DelphiType implements CollectionType {
  private DelphiSetType(Type elementType) {
    super("set of " + elementType.getImage());
  }

  public static CollectionType set(Type elementType) {
    return new MutableDelphiSetType(elementType);
  }

  public static ImmutableCollectionType emptySet() {
    return ImmutableDelphiSetType.EMPTY_SET;
  }

  @Override
  public boolean isSet() {
    return true;
  }

  private static class MutableDelphiSetType extends DelphiSetType {
    private final Type elementType;

    private MutableDelphiSetType(Type elementType) {
      super(elementType);
      this.elementType = elementType;
    }

    @Override
    @NotNull
    public Type elementType() {
      return elementType;
    }
  }

  @Immutable
  private static class ImmutableDelphiSetType extends DelphiSetType
      implements ImmutableCollectionType {
    private static final ImmutableCollectionType EMPTY_SET =
        new ImmutableDelphiSetType(DelphiType.voidType());

    private final ImmutableType elementType;

    private ImmutableDelphiSetType(ImmutableType elementType) {
      super(elementType);
      this.elementType = elementType;
    }

    @Override
    @NotNull
    public ImmutableType elementType() {
      return elementType;
    }
  }
}
