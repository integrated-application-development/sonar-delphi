package org.sonar.plugins.delphi.type;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSISTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.SHORTSTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.STRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDECHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDESTRING;

import java.util.Collections;
import java.util.Set;

public abstract class DelphiType implements Type {
  private final String image;

  protected DelphiType(String image) {
    this.image = image;
  }

  public static ImmutableType unknownType() {
    return UnknownType.instance();
  }

  public static ImmutableType untypedType() {
    return UntypedType.instance();
  }

  public static ImmutableType voidType() {
    return VoidType.instance();
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public Type superType() {
    return unknownType();
  }

  @Override
  public Set<Type> parents() {
    return Collections.emptySet();
  }

  @Override
  public boolean is(String image) {
    return getImage().equalsIgnoreCase(image);
  }

  @Override
  public final boolean is(Type type) {
    return is(type.getImage());
  }

  @Override
  public boolean isSubTypeOf(String image) {
    return false;
  }

  @Override
  public boolean isSubTypeOf(Type type) {
    return false;
  }

  @Override
  public boolean isUntyped() {
    return false;
  }

  @Override
  public boolean isUnknown() {
    return false;
  }

  @Override
  public boolean isUnresolved() {
    return false;
  }

  @Override
  public boolean isVoid() {
    return false;
  }

  @Override
  public boolean isInteger() {
    return false;
  }

  @Override
  public boolean isDecimal() {
    return false;
  }

  @Override
  public boolean isText() {
    return false;
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  @Override
  public final boolean isString() {
    return isText() && !isChar();
  }

  @Override
  public final boolean isNarrowString() {
    return isText() && (is(ANSISTRING.type) || is(SHORTSTRING.type));
  }

  @Override
  public final boolean isWideString() {
    return isText() && (is(STRING.type) || is(WIDESTRING.type) || is(UNICODESTRING.type));
  }

  @Override
  public final boolean isChar() {
    return isNarrowChar() || isWideChar();
  }

  @Override
  public final boolean isNarrowChar() {
    return is(ANSICHAR.type);
  }

  @Override
  public final boolean isWideChar() {
    return is(CHAR.type) || is(WIDECHAR.type);
  }

  @Override
  public boolean isStruct() {
    return false;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isRecord() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  @Override
  public boolean isFile() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isFixedArray() {
    return false;
  }

  @Override
  public boolean isDynamicArray() {
    return false;
  }

  @Override
  public boolean isOpenArray() {
    return false;
  }

  @Override
  public boolean isPointer() {
    return false;
  }

  @Override
  public boolean isSet() {
    return false;
  }

  @Override
  public boolean isProcedural() {
    return false;
  }

  @Override
  public boolean isMethod() {
    return false;
  }

  @Override
  public boolean isClassReference() {
    return false;
  }

  @Override
  public boolean isVariant() {
    return false;
  }

  @Override
  public boolean isTypeType() {
    return false;
  }

  @Override
  public boolean isArrayConstructor() {
    return false;
  }

  @Override
  public boolean isArrayOfConst() {
    return false;
  }
}
