package org.sonar.plugins.delphi.type;

import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public class DelphiArrayType extends DelphiCollectionType {
  public enum ArrayOption {
    FIXED,
    DYNAMIC,
    OPEN,
    ARRAY_OF_CONST
  }

  private final Set<ArrayOption> options;

  private DelphiArrayType(String image, Type elementType, Set<ArrayOption> options) {
    super(image, elementType);
    this.options = options;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public boolean isFixedArray() {
    return options.contains(ArrayOption.FIXED);
  }

  @Override
  public boolean isDynamicArray() {
    return options.contains(ArrayOption.DYNAMIC);
  }

  @Override
  public boolean isOpenArray() {
    return options.contains(ArrayOption.OPEN);
  }

  @Override
  public boolean isArrayOfConst() {
    return options.contains(ArrayOption.ARRAY_OF_CONST);
  }

  public static CollectionType fixedArray(@Nullable String image, Type elementType) {
    return array(image, elementType, Set.of(ArrayOption.FIXED));
  }

  public static CollectionType dynamicArray(@Nullable String image, Type elementType) {
    return array(image, elementType, Set.of(ArrayOption.DYNAMIC));
  }

  public static CollectionType openArray(@Nullable String image, Type elementType) {
    return array(image, elementType, Set.of(ArrayOption.OPEN));
  }

  public static CollectionType multiDimensionalArray(
      @Nullable String image, Type elementType, int indices, Set<ArrayOption> options) {
    CollectionType type = array(null, elementType, options);
    for (int i = 1; i < indices; ++i) {
      type = array(((i == indices - 1) ? image : null), type, Set.of(ArrayOption.FIXED));
    }
    return type;
  }

  public static CollectionType array(
      @Nullable String image, Type elementType, Set<ArrayOption> options) {
    if (image == null) {
      image =
          "array of "
              + elementType.getImage()
              + " <"
              + options.stream().map(ArrayOption::name).collect(Collectors.joining(","))
              + ">";
    }
    return new DelphiArrayType(image, elementType, options);
  }
}
