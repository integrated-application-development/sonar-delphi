package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType;

public abstract class DelphiType implements Type {
  private static final DelphiType UNTYPED =
      new DelphiType("<Untyped>") {
        @Override
        public boolean isUntyped() {
          return true;
        }
      };

  private static final DelphiType UNKNOWN =
      new DelphiType("<Unknown>") {
        @Override
        public boolean isUnknown() {
          return true;
        }
      };

  private static final DelphiType VOID =
      new DelphiType("<Void>") {
        @Override
        public boolean isVoid() {
          return true;
        }
      };

  private final String image;

  public DelphiType(String image) {
    this.image = image;
  }

  public static Type unknownType() {
    return UNKNOWN;
  }

  public static Type untypedType() {
    return UNTYPED;
  }

  public static Type voidType() {
    return VOID;
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
    return isText() && (is(TextType.ANSISTRING.type) || is(TextType.SHORTSTRING.type));
  }

  @Override
  public final boolean isWideString() {
    return isText()
        && (is(TextType.STRING.type)
            || is(TextType.WIDESTRING.type)
            || is(TextType.UNICODESTRING.type));
  }

  @Override
  public final boolean isChar() {
    return isNarrowChar() || isWideChar();
  }

  @Override
  public final boolean isNarrowChar() {
    return is(TextType.ANSICHAR.type);
  }

  @Override
  public final boolean isWideChar() {
    return is(TextType.CHAR.type) || is(TextType.WIDECHAR.type);
  }

  @Override
  public boolean isObject() {
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
}
