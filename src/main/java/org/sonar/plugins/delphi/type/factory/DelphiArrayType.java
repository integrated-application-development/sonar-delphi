package org.sonar.plugins.delphi.type.factory;

import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.type.ArrayOption;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.generic.DelphiGenerifiableType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

class DelphiArrayType extends DelphiGenerifiableType implements CollectionType {
  private final String image;
  private final int size;
  private final Type elementType;
  private final Set<ArrayOption> options;

  DelphiArrayType(@Nullable String image, int size, Type elementType, Set<ArrayOption> options) {
    if (image == null) {
      image = createImage(elementType, options);
    }
    this.image = image;
    this.size = size;
    this.elementType = elementType;
    this.options = options;
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
  public int size() {
    return size;
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
    String specializedImage = createImage(specializedElement, options);
    return new DelphiArrayType(specializedImage, size, specializedElement, options);
  }
}
