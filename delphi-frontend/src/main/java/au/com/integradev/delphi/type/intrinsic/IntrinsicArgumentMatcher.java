/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.type.intrinsic;

import au.com.integradev.delphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeUtils;

public final class IntrinsicArgumentMatcher extends DelphiType {
  public static final Type ANY_DYNAMIC_ARRAY =
      new IntrinsicArgumentMatcher("<dynamic array>", Type::isDynamicArray);

  public static final Type ANY_ARRAY = new IntrinsicArgumentMatcher("<array>", Type::isArray);

  public static final Type ANY_SET =
      new IntrinsicArgumentMatcher("<set>", type -> type.isSet() || type.isArrayConstructor());

  public static final Type ANY_OBJECT =
      new IntrinsicArgumentMatcher(
          "<object>", type -> type.isStruct() && ((StructType) type).kind() == StructKind.OBJECT);

  public static final Type ANY_ORDINAL =
      new IntrinsicArgumentMatcher(
          "<ordinal>",
          type -> type.isInteger() || type.isBoolean() || type.isEnum() || type.isChar());

  public static final Type ANY_CLASS_REFERENCE =
      new IntrinsicArgumentMatcher("<class reference>", Type::isClassReference);

  public static final Type POINTER_MATH_OPERAND =
      new IntrinsicArgumentMatcher(
          "<pointer math operand>",
          type ->
              type.isPointer()
                  || (type.isArray()
                      && !type.isDynamicArray()
                      && ((CollectionType) type).elementType().isChar()));

  @FunctionalInterface
  private interface Matcher {
    boolean matches(Type type);
  }

  private final String image;
  private final Matcher matcher;

  private IntrinsicArgumentMatcher(String image, Matcher matcher) {
    this.image = image;
    this.matcher = matcher;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  public boolean matches(Type type) {
    return matcher.matches(TypeUtils.findBaseType(type));
  }
}
