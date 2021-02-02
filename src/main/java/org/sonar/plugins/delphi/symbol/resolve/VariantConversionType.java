package org.sonar.plugins.delphi.symbol.resolve;

import static java.util.Objects.requireNonNullElse;

import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

enum VariantConversionType {
  NO_CONVERSION_REQUIRED,
  INCOMPATIBLE_VARIANT,
  CHARI64,
  SHORTSTRING,
  ANSISTRING,
  WIDESTRING,
  UNICODESTRING,
  FORMAL_BOOLEAN,
  ENUM,
  DYNAMIC_ARRAY,
  EXTENDED,
  DOUBLE_CURRENCY,
  SINGLE,
  CARDINAL,
  INTEGER,
  SMALLINT,
  WORD,
  SHORTINT,
  BYTE;

  private static final Map<String, VariantConversionType> intrinsicTypeMap = new HashMap<>();

  static {
    add(IntrinsicType.BYTE, BYTE);
    add(IntrinsicType.WORD, WORD);
    add(IntrinsicType.CARDINAL, CARDINAL);
    add(IntrinsicType.UINT64, CHARI64);

    add(IntrinsicType.SHORTINT, SHORTINT);
    add(IntrinsicType.SMALLINT, SMALLINT);
    add(IntrinsicType.INTEGER, INTEGER);
    add(IntrinsicType.INT64, CHARI64);

    add(IntrinsicType.SINGLE, SINGLE);
    add(IntrinsicType.REAL48, DOUBLE_CURRENCY);
    add(IntrinsicType.REAL, DOUBLE_CURRENCY);
    add(IntrinsicType.DOUBLE, DOUBLE_CURRENCY);
    add(IntrinsicType.COMP, DOUBLE_CURRENCY);
    add(IntrinsicType.CURRENCY, DOUBLE_CURRENCY);
    add(IntrinsicType.EXTENDED, EXTENDED);

    add(IntrinsicType.ANSICHAR, CHARI64);
    add(IntrinsicType.WIDECHAR, CHARI64);
    add(IntrinsicType.SHORTSTRING, SHORTSTRING);
    add(IntrinsicType.ANSISTRING, ANSISTRING);
    add(IntrinsicType.WIDESTRING, WIDESTRING);
    add(IntrinsicType.UNICODESTRING, UNICODESTRING);
  }

  private static void add(IntrinsicType intrinsic, VariantConversionType variantType) {
    intrinsicTypeMap.put(intrinsic.fullyQualifiedName(), variantType);
  }

  static VariantConversionType fromType(Type type) {
    VariantConversionType result;

    if (type.isVariant()) {
      result = NO_CONVERSION_REQUIRED;
    } else if (type.isEnum()) {
      result = ENUM;
    } else if (type.isDynamicArray()) {
      result = DYNAMIC_ARRAY;
    } else if (type.isBoolean() || type.isUntyped()) {
      result = FORMAL_BOOLEAN;
    } else {
      result = intrinsicTypeMap.get(type.getImage());
    }

    return requireNonNullElse(result, INCOMPATIBLE_VARIANT);
  }

  static boolean isChari64Str(VariantConversionType type) {
    switch (type) {
      case CHARI64:
      case SHORTSTRING:
      case ANSISTRING:
      case WIDESTRING:
      case UNICODESTRING:
        return true;
      default:
        return false;
    }
  }
}
