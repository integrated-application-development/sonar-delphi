package org.sonar.plugins.delphi.type;

import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.generic.DelphiGenerifiableType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public class DelphiArrayType extends DelphiGenerifiableType implements CollectionType {
  public enum ArrayOption {
    FIXED,
    DYNAMIC,
    OPEN,
    ARRAY_OF_CONST
  }

  private final String image;
  private final Type elementType;
  private final Set<ArrayOption> options;

  private DelphiArrayType(String image, Type elementType, Set<ArrayOption> options) {
    this.image = image;
    this.elementType = elementType;
    this.options = options;
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
      image = createImage(elementType, options);
    }
    return new DelphiArrayType(image, elementType, options);
  }

  private static String createImage(Type elementType, Set<ArrayOption> options) {
    return "array of "
        + elementType.getImage()
        + " <"
        + options.stream().map(ArrayOption::name).collect(Collectors.joining(","))
        + ">";
  }

  @Override
  public String getImage() {
    return image;
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

  @Override
  @NotNull
  public Type elementType() {
    return elementType;
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    return elementType.canBeSpecialized(context);
  }

  @Override
  public DelphiGenerifiableType doSpecialization(TypeSpecializationContext context) {
    Type specializedElement = elementType.specialize(context);
    String image = createImage(specializedElement, options);
    return new DelphiArrayType(image, specializedElement, options);
  }
}
