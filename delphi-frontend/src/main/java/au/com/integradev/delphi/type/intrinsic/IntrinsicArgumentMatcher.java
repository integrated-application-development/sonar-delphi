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
package au.com.integradev.delphi.type.intrinsic;

import au.com.integradev.delphi.type.TypeImpl;
import au.com.integradev.delphi.type.TypeUtils;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class IntrinsicArgumentMatcher extends TypeImpl {
  public static final Type LIKE_DYNAMIC_ARRAY =
      new IntrinsicArgumentMatcher(
          "<dynamic array, array constructor, string, or char>",
          type ->
              type.isDynamicArray()
                  || type.isArrayConstructor()
                  || type.isString()
                  || type.isChar());

  public static final Type ANY_STRING =
      new IntrinsicArgumentMatcher("<string or char>", type -> type.isString() || type.isChar());

  public static final Type ANY_FILE = new IntrinsicArgumentMatcher("<file>", Type::isFile);

  public static final Type ANY_TEXT_FILE =
      new IntrinsicArgumentMatcher("<text file>", type -> type.is(IntrinsicType.TEXT));

  public static final Type ANY_VARIANT = new IntrinsicArgumentMatcher("<variant>", Type::isVariant);

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

  public static final Type ANY_32_BIT_INTEGER =
      new IntrinsicArgumentMatcher(
          "<32-bit integer>", type -> type.isInteger() && type.size() == 4);

  public static final Type ANY_POINTER = new IntrinsicArgumentMatcher("<pointer>", Type::isPointer);

  public static final Type ANY_TYPED_POINTER =
      new IntrinsicArgumentMatcher(
          "<typed pointer>",
          type ->
              type.isPointer()
                  && !((PointerType) type).dereferencedType().isUntyped()
                  && !((PointerType) type).dereferencedType().isVoid());

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
