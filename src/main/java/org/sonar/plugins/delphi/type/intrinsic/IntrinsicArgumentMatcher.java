package org.sonar.plugins.delphi.type.intrinsic;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.TypeUtils;

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
