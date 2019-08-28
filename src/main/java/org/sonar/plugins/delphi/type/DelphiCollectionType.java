package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.type.DelphiCollectionType.DelphiArrayType.ArrayKind;
import org.sonar.plugins.delphi.type.Type.CollectionType;

public abstract class DelphiCollectionType extends DelphiType implements CollectionType {
  protected static class DelphiArrayType extends DelphiCollectionType {
    protected enum ArrayKind {
      FIXED,
      DYNAMIC,
      OPEN
    }

    private final ArrayKind kind;

    DelphiArrayType(String image, Type elementType, ArrayKind kind) {
      super(image, elementType);
      this.kind = kind;
    }

    @Override
    public boolean isArray() {
      return true;
    }

    @Override
    public boolean isFixedArray() {
      return kind == ArrayKind.FIXED;
    }

    @Override
    public boolean isDynamicArray() {
      return kind == ArrayKind.DYNAMIC;
    }

    @Override
    public boolean isOpenArray() {
      return kind == ArrayKind.OPEN;
    }
  }

  private static class DelphiSetType extends DelphiCollectionType {
    protected static final CollectionType EMPTY_SET = new DelphiSetType(DelphiType.voidType());

    DelphiSetType(Type elementType) {
      super("set of " + elementType.getImage(), elementType);
    }

    @Override
    public boolean isSet() {
      return true;
    }
  }

  private final Type elementType;

  private DelphiCollectionType(String image, Type elementType) {
    super(image);
    this.elementType = elementType;
  }

  public static CollectionType fixedArray(@Nullable String image, Type elementType) {
    return array(image, elementType, ArrayKind.FIXED);
  }

  public static CollectionType dynamicArray(@Nullable String image, Type elementType) {
    return array(image, elementType, ArrayKind.DYNAMIC);
  }

  public static CollectionType openArray(@Nullable String image, Type elementType) {
    return array(image, elementType, ArrayKind.OPEN);
  }

  public static CollectionType multiDimensionalArray(
      @Nullable String image, Type elementType, int indices) {
    CollectionType type = fixedArray(null, elementType);
    for (int i = 1; i < indices; ++i) {
      type = fixedArray(((i == indices - 1) ? image : null), type);
    }
    return type;
  }

  private static CollectionType array(@Nullable String image, Type elementType, ArrayKind kind) {
    if (image == null) {
      image = kind.name().toLowerCase() + " array of " + elementType.getImage();
    }
    return new DelphiArrayType(image, elementType, kind);
  }

  public static CollectionType set(Type elementType) {
    return new DelphiSetType(elementType);
  }

  public static CollectionType emptySet() {
    return DelphiSetType.EMPTY_SET;
  }

  @Override
  @NotNull
  public Type elementType() {
    return elementType;
  }
}
