package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import java.util.function.Function;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
public class IntrinsicArgumentMatcher extends DelphiType implements ImmutableType {
  public static final ImmutableType ANY_DYNAMIC_ARRAY =
      new IntrinsicArgumentMatcher("<dynamic array>", Type::isDynamicArray);

  public static final ImmutableType ANY_ARRAY =
      new IntrinsicArgumentMatcher("<array>", Type::isArray);

  public static final ImmutableType ANY_SET = new IntrinsicArgumentMatcher("<set>", Type::isSet);

  public static final ImmutableType ANY_OBJECT =
      new IntrinsicArgumentMatcher(
          "<object>", type -> type.isStruct() && ((StructType) type).kind() == StructKind.OBJECT);

  public static final ImmutableType ANY_ORDINAL =
      new IntrinsicArgumentMatcher(
          "<Ordinal>",
          type -> type.isInteger() || type.isBoolean() || type.isEnum() || type.isChar());

  @Immutable
  private interface Matcher extends Function<Type, Boolean> {}

  private final Matcher matcher;

  private IntrinsicArgumentMatcher(String name, Matcher matcher) {
    super(name);
    this.matcher = matcher;
  }

  public boolean matches(Type type) {
    return matcher.apply(type);
  }
}
