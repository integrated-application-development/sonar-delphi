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
package au.com.integradev.delphi.symbol.resolve;

import static java.util.Objects.requireNonNullElse;

import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;

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
