package org.sonar.plugins.delphi.symbol.resolve;

import java.util.Map;
import java.util.TreeMap;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType;
import org.sonar.plugins.delphi.type.Type;

enum VariantEqualityType {
  INCOMPATIBLE_VARIANT,
  CHARI64,
  SHORTSTRING,
  ANSISTRING,
  WIDESTRING,
  UNICODESTRING,
  FORMAL_BOOLEAN,
  EXTENDED,
  DOUBLE_CURRENCY,
  SINGLE,
  CARDINAL,
  LONGINT,
  SMALLINT,
  WORD,
  SHORTINT,
  BYTE;

  private static final Map<String, VariantEqualityType> integerTypeMap =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private static final Map<String, VariantEqualityType> decimalTypeMap =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private static final Map<String, VariantEqualityType> textTypeMap =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  static {
    add(IntegerType.BYTE, BYTE);
    add(IntegerType.SMALLINT, WORD);
    add(IntegerType.WORD, WORD);
    add(IntegerType.CARDINAL, CARDINAL);
    add(IntegerType.LONGWORD, CARDINAL);
    add(IntegerType.FIXEDUINT, CARDINAL);
    add(IntegerType.UINT64, CHARI64);
    add(IntegerType.SHORTINT, SHORTINT);
    add(IntegerType.SMALLINT, SMALLINT);
    add(IntegerType.INTEGER, LONGINT);
    add(IntegerType.LONGINT, LONGINT);
    add(IntegerType.FIXEDINT, LONGINT);
    add(IntegerType.INT64, CHARI64);

    add(DecimalType.SINGLE, SINGLE);
    add(DecimalType.DOUBLE, DOUBLE_CURRENCY);
    add(DecimalType.EXTENDED, EXTENDED);
    add(DecimalType.COMP, DOUBLE_CURRENCY);
    add(DecimalType.CURRENCY, DOUBLE_CURRENCY);

    add(TextType.SHORTSTRING, SHORTSTRING);
    add(TextType.STRING, UNICODESTRING);
    add(TextType.ANSISTRING, ANSISTRING);
    add(TextType.WIDESTRING, WIDESTRING);
    add(TextType.UNICODESTRING, UNICODESTRING);
  }

  private static void add(IntegerType integerType, VariantEqualityType variantType) {
    integerTypeMap.put(integerType.type.getImage(), variantType);
  }

  private static void add(DecimalType decimalType, VariantEqualityType variantType) {
    decimalTypeMap.put(decimalType.type.getImage(), variantType);
  }

  private static void add(TextType textType, VariantEqualityType variantType) {
    textTypeMap.put(textType.type.getImage(), variantType);
  }

  static VariantEqualityType fromType(Type type) {
    VariantEqualityType result = null;
    if (type.isInteger()) {
      result = integerTypeMap.get(type.getImage());
    } else if (type.isDecimal()) {
      result = decimalTypeMap.get(type.getImage());
    } else if (type.isText()) {
      result = textTypeMap.get(type.getImage());
    } else if (type.isBoolean() || type.isUntyped()) {
      result = FORMAL_BOOLEAN;
    }

    if (result == null) {
      result = INCOMPATIBLE_VARIANT;
    }

    return result;
  }

  static boolean isChari64Str(VariantEqualityType type) {
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
