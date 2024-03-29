/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.type.factory;

import au.com.integradev.delphi.type.generic.GenerifiableTypeImpl;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public final class ArrayTypeImpl extends GenerifiableTypeImpl implements CollectionType {
  private final String image;
  private final int size;
  private final Type elementType;
  private final Set<ArrayOption> options;

  ArrayTypeImpl(@Nullable String image, int size, Type elementType, Set<ArrayOption> options) {
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
  public Type elementType() {
    return elementType;
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    return elementType.canBeSpecialized(context);
  }

  @Override
  public GenerifiableTypeImpl doSpecialization(TypeSpecializationContext context) {
    Type specializedElement = elementType.specialize(context);
    String specializedImage = createImage(specializedElement, options);
    return new ArrayTypeImpl(specializedImage, size, specializedElement, options);
  }
}
