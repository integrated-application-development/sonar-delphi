package org.sonar.plugins.delphi.symbol.resolve;

import static java.util.Objects.requireNonNullElse;

import java.util.Map;
import java.util.TreeMap;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicText;

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
  LONGINT,
  SMALLINT,
  WORD,
  SHORTINT,
  BYTE;

  private static final Map<String, VariantConversionType> integerTypeMap =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private static final Map<String, VariantConversionType> decimalTypeMap =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private static final Map<String, VariantConversionType> textTypeMap =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  static {
    add(IntrinsicInteger.BYTE, BYTE);
    add(IntrinsicInteger.WORD, WORD);
    add(IntrinsicInteger.CARDINAL, CARDINAL);
    add(IntrinsicInteger.LONGWORD, CARDINAL);
    add(IntrinsicInteger.FIXEDUINT, CARDINAL);
    add(IntrinsicInteger.UINT64, CHARI64);

    add(IntrinsicInteger.SHORTINT, SHORTINT);
    add(IntrinsicInteger.SMALLINT, SMALLINT);
    add(IntrinsicInteger.INTEGER, LONGINT);
    add(IntrinsicInteger.LONGINT, LONGINT);
    add(IntrinsicInteger.FIXEDINT, LONGINT);
    add(IntrinsicInteger.INT64, CHARI64);

    add(IntrinsicDecimal.SINGLE, SINGLE);
    add(IntrinsicDecimal.REAL48, DOUBLE_CURRENCY);
    add(IntrinsicDecimal.REAL, DOUBLE_CURRENCY);
    add(IntrinsicDecimal.DOUBLE, DOUBLE_CURRENCY);
    add(IntrinsicDecimal.COMP, DOUBLE_CURRENCY);
    add(IntrinsicDecimal.CURRENCY, DOUBLE_CURRENCY);
    add(IntrinsicDecimal.EXTENDED, EXTENDED);

    add(IntrinsicText.ANSICHAR, CHARI64);
    add(IntrinsicText.WIDECHAR, CHARI64);
    add(IntrinsicText.CHAR, CHARI64);
    add(IntrinsicText.SHORTSTRING, SHORTSTRING);
    add(IntrinsicText.ANSISTRING, ANSISTRING);
    add(IntrinsicText.WIDESTRING, WIDESTRING);
    add(IntrinsicText.UNICODESTRING, UNICODESTRING);
  }

  private static void add(IntrinsicInteger integerType, VariantConversionType variantType) {
    integerTypeMap.put(integerType.type.getImage(), variantType);
  }

  private static void add(IntrinsicDecimal decimalType, VariantConversionType variantType) {
    decimalTypeMap.put(decimalType.type.getImage(), variantType);
  }

  private static void add(IntrinsicText textType, VariantConversionType variantType) {
    textTypeMap.put(textType.type.getImage(), variantType);
  }

  static VariantConversionType fromType(Type type) {
    VariantConversionType result = null;

    if (type.isVariant()) {
      result = NO_CONVERSION_REQUIRED;
    } else if (type.isInteger()) {
      result = integerTypeMap.get(type.getImage());
    } else if (type.isDecimal()) {
      result = decimalTypeMap.get(type.getImage());
    } else if (type.isText()) {
      result = textTypeMap.get(type.getImage());
    } else if (type.isEnum()) {
      result = ENUM;
    } else if (type.isDynamicArray()) {
      result = DYNAMIC_ARRAY;
    } else if (type.isBoolean() || type.isUntyped()) {
      result = FORMAL_BOOLEAN;
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
