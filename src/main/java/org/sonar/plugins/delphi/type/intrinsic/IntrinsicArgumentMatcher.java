package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ImmutableType;
import org.sonar.plugins.delphi.type.TypeUtils;

@Immutable
public final class IntrinsicArgumentMatcher extends DelphiType implements ImmutableType {
  public static final ImmutableType ANY_DYNAMIC_ARRAY =
      new IntrinsicArgumentMatcher("<dynamic array>", Type::isDynamicArray);

  public static final ImmutableType ANY_ARRAY =
      new IntrinsicArgumentMatcher("<array>", Type::isArray);

  public static final ImmutableType ANY_SET =
      new IntrinsicArgumentMatcher("<set>", type -> type.isSet() || type.isArrayConstructor());

  public static final ImmutableType ANY_OBJECT =
      new IntrinsicArgumentMatcher(
          "<object>", type -> type.isStruct() && ((StructType) type).kind() == StructKind.OBJECT);

  public static final ImmutableType ANY_ORDINAL =
      new IntrinsicArgumentMatcher(
          "<ordinal>",
          type -> type.isInteger() || type.isBoolean() || type.isEnum() || type.isChar());

  public static final ImmutableType ANY_CLASS_REFERENCE =
      new IntrinsicArgumentMatcher("<class reference>", Type::isClassReference);

  public static final ImmutableType POINTER_MATH_OPERAND =
      new IntrinsicArgumentMatcher(
          "<pointer math operand>",
          type ->
              type.isPointer()
                  || (type.isArray()
                      && !type.isDynamicArray()
                      && ((CollectionType) type).elementType().isChar()));

  @Immutable
  @FunctionalInterface
  private interface Matcher {
    boolean matches(Type type);
  }

  private final Matcher matcher;

  private IntrinsicArgumentMatcher(String name, Matcher matcher) {
    super(name);
    this.matcher = matcher;
  }

  public boolean matches(Type type) {
    return matcher.matches(TypeUtils.findBaseType(type));
  }
}
