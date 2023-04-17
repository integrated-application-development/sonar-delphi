/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.symbol.resolve;

import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_1;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_2;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_3;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_4;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_5;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_6;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_7;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_LEVEL_8;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.EQUAL;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.EXACT;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.INCOMPATIBLE_VARIANT;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.List;
import org.sonar.plugins.delphi.type.CodePages;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.AnsiStringType;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;
import org.sonar.plugins.delphi.type.Type.BooleanType;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.Type.IntegerType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.StringType;
import org.sonar.plugins.delphi.type.Type.SubrangeType;
import org.sonar.plugins.delphi.type.Type.TypeType;
import org.sonar.plugins.delphi.type.Type.VariantType;
import org.sonar.plugins.delphi.type.Type.VariantType.VariantKind;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.type.parameter.Parameter;

class TypeComparer {
  private TypeComparer() {
    // Utility class
  }

  /**
   * Based directly off of compare_defs_ext from the FreePascal compiler.
   *
   * @param from The type we are comparing from
   * @param to The type we are comparing to
   * @return equality type
   * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/defcmp.pas#L199">
   *     compare_defs_ext</a>
   */
  static EqualityType compare(Type from, Type to) {
    if (from.is(to) && !from.isUntyped()) {
      return EXACT;
    }

    while (from.isTypeType()) {
      from = ((TypeType) from).originalType();
    }

    while (to.isTypeType()) {
      to = ((TypeType) to).originalType();
    }

    EqualityType result;

    if (to.isInteger()) {
      result = compareInteger(from, to);
    } else if (to.isDecimal()) {
      result = compareDecimal(from, to);
    } else if (to.isString()) {
      result = compareString(from, to);
    } else if (to.isChar()) {
      result = compareChar(from, to);
    } else if (to.isBoolean()) {
      result = compareBoolean(from, to);
    } else if (to.isEnum()) {
      result = compareEnum(from, to);
    } else if (to.isSubrange()) {
      result = compareSubrange(from, to);
    } else if (to.isArray()) {
      result = compareArray(from, to);
    } else if (to.isArrayConstructor()) {
      result = compareArrayConstructor(from, to);
    } else if (to.isSet()) {
      result = compareSet(from, to);
    } else if (to.isProcedural()) {
      result = compareProceduralType(from, to);
    } else if (to.isStruct()) {
      result = compareObject(from, to);
    } else if (to.isClassReference()) {
      result = compareClassReference(from, to);
    } else if (to.isFile()) {
      result = compareFile(from, to);
    } else if (to.isPointer()) {
      result = comparePointer(from, to);
    } else if (to.isUntyped()) {
      result = compareUntyped(from);
    } else if (to.isVariant()) {
      result = compareVariant(from);
    } else {
      result = tryIntrinsicArgumentTypes(from, to);
    }

    if (result == INCOMPATIBLE_TYPES && (from.isVariant() || to.isVariant())) {
      if (from.isVariant() && VariantConversionType.fromType(to) != INCOMPATIBLE_VARIANT) {
        result = CONVERT_LEVEL_7;
      } else {
        result = CONVERT_LEVEL_8;
      }
    }

    return result;
  }

  private static EqualityType compareInteger(Type from, Type to) {
    if (from.isSubrange()) {
      from = ((SubrangeType) from).hostType();
    }

    if (from.isInteger()) {
      IntegerType fromInteger = (IntegerType) from;
      IntegerType toInteger = (IntegerType) to;
      if (fromInteger.isSameRange(toInteger)) {
        return EQUAL;
      } else if (fromInteger.isWithinLimit(toInteger)) {
        return CONVERT_LEVEL_1;
      } else {
        // Penalty for bad type conversion
        return CONVERT_LEVEL_3;
      }
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareDecimal(Type from, Type to) {
    if (from.isInteger()) {
      return compareIntegerToDecimal(from, to);
    } else if (from.isDecimal()) {
      return compareDecimalToDecimal(from, to);
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareIntegerToDecimal(Type from, Type to) {
    if (from.is(IntrinsicType.INT64) || from.is(IntrinsicType.UINT64)) {
      if (to.is(IntrinsicType.EXTENDED)) {
        return CONVERT_LEVEL_1;
      } else if (to.is(IntrinsicType.DOUBLE)) {
        return CONVERT_LEVEL_2;
      } else if (to.is(IntrinsicType.REAL48)) {
        return CONVERT_LEVEL_3;
      } else if (to.is(IntrinsicType.SINGLE)) {
        return CONVERT_LEVEL_4;
      } else {
        return CONVERT_LEVEL_5;
      }
    } else {
      if (to.is(IntrinsicType.SINGLE)) {
        return CONVERT_LEVEL_1;
      } else if (to.is(IntrinsicType.REAL48)) {
        return CONVERT_LEVEL_2;
      } else if (to.is(IntrinsicType.DOUBLE)) {
        return CONVERT_LEVEL_3;
      } else if (to.is(IntrinsicType.EXTENDED)) {
        return CONVERT_LEVEL_4;
      } else {
        return CONVERT_LEVEL_5;
      }
    }
  }

  @VisibleForTesting
  static EqualityType compareDecimalToDecimal(Type from, Type to) {
    if (from.is(to)) {
      return EQUAL;
    } else if (from.is(IntrinsicType.SINGLE)) {
      return compareSingleToDecimal(to);
    } else if (from.is(IntrinsicType.REAL48)) {
      return compareReal48ToDecimal(to);
    } else if (from.is(IntrinsicType.DOUBLE)) {
      return compareDoubleToDecimal(to);
    } else if (from.is(IntrinsicType.EXTENDED)) {
      return compareExtendedToDecimal(to);
    } else if (from.is(IntrinsicType.CURRENCY) || from.is(IntrinsicType.COMP)) {
      return compareIntegerRealsToDecimal(to);
    }

    throw new AssertionError("Unhandled decimal type");
  }

  private static EqualityType compareSingleToDecimal(Type to) {
    if (to.is(IntrinsicType.REAL48)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(IntrinsicType.DOUBLE)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(IntrinsicType.EXTENDED)) {
      return CONVERT_LEVEL_3;
    } else {
      return CONVERT_LEVEL_6;
    }
  }

  private static EqualityType compareReal48ToDecimal(Type to) {
    if (to.is(IntrinsicType.DOUBLE)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(IntrinsicType.EXTENDED)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(IntrinsicType.SINGLE)) {
      return CONVERT_LEVEL_5;
    } else {
      return CONVERT_LEVEL_6;
    }
  }

  private static EqualityType compareDoubleToDecimal(Type to) {
    if (to.is(IntrinsicType.EXTENDED)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(IntrinsicType.REAL48)) {
      return CONVERT_LEVEL_4;
    } else if (to.is(IntrinsicType.SINGLE)) {
      return CONVERT_LEVEL_5;
    } else {
      return CONVERT_LEVEL_6;
    }
  }

  private static EqualityType compareExtendedToDecimal(Type to) {
    if (to.is(IntrinsicType.DOUBLE)) {
      return CONVERT_LEVEL_4;
    } else if (to.is(IntrinsicType.REAL48)) {
      return CONVERT_LEVEL_5;
    } else if (to.is(IntrinsicType.SINGLE)) {
      return CONVERT_LEVEL_6;
    } else {
      return CONVERT_LEVEL_7;
    }
  }

  private static EqualityType compareIntegerRealsToDecimal(Type to) {
    if (to.is(IntrinsicType.EXTENDED)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(IntrinsicType.DOUBLE)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(IntrinsicType.REAL48)) {
      return CONVERT_LEVEL_3;
    } else if (to.is(IntrinsicType.SINGLE)) {
      return CONVERT_LEVEL_4;
    } else {
      return CONVERT_LEVEL_5;
    }
  }

  private static EqualityType compareString(Type from, Type to) {
    if (from.isString()) {
      return compareStringToString(from, to);
    } else if (from.isChar()) {
      return compareCharToString(from, to);
    } else if (from.isPointer()) {
      return comparePointerToString((PointerType) from, (StringType) to);
    } else if (from.isArray()) {
      return compareArrayToString((CollectionType) from, to);
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareChar(Type from, Type to) {
    if (from.isChar()) {
      return compareCharToChar(from, to);
    }
    return INCOMPATIBLE_TYPES;
  }

  @VisibleForTesting
  static EqualityType compareStringToString(Type from, Type to) {
    if (from.is(to)) {
      return EQUAL;
    } else if (from.is(IntrinsicType.WIDESTRING)) {
      return compareWideStringToString(to);
    } else if (from.is(IntrinsicType.UNICODESTRING)) {
      return compareUnicodeStringToString(to);
    } else if (from.is(IntrinsicType.SHORTSTRING)) {
      return compareShortStringToString(to);
    } else if (from.isAnsiString()) {
      return compareAnsiStringToString((AnsiStringType) from, to);
    }

    throw new AssertionError("Unhandled string type");
  }

  private static EqualityType compareWideStringToString(Type to) {
    if (to.is(IntrinsicType.UNICODESTRING)) {
      return CONVERT_LEVEL_1;
    } else if (to.isAnsiString()) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  private static EqualityType compareUnicodeStringToString(Type to) {
    if (to.is(IntrinsicType.WIDESTRING)) {
      return CONVERT_LEVEL_1;
    } else if (to.isAnsiString()) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  private static EqualityType compareShortStringToString(Type to) {
    if (to.isAnsiString()) {
      return CONVERT_LEVEL_1;
    } else if (to.is(IntrinsicType.UNICODESTRING)) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  private static EqualityType compareAnsiStringToString(AnsiStringType from, Type to) {
    if (to.isAnsiString()) {
      AnsiStringType toAnsiString = (AnsiStringType) to;
      if (from.codePage() == toAnsiString.codePage()
          || toAnsiString.codePage() == CodePages.CP_NONE) {
        return EQUAL;
      } else if (toAnsiString.codePage() == CodePages.CP_UTF8) {
        return CONVERT_LEVEL_1;
      } else if (toAnsiString.codePage() == CodePages.CP_ACP) {
        return CONVERT_LEVEL_2;
      } else {
        return CONVERT_LEVEL_3;
      }
    } else if (to.is(IntrinsicType.UNICODESTRING)) {
      return CONVERT_LEVEL_4;
    } else if (to.is(IntrinsicType.WIDESTRING)) {
      return CONVERT_LEVEL_5;
    } else {
      return CONVERT_LEVEL_6;
    }
  }

  @VisibleForTesting
  static EqualityType compareCharToString(Type from, Type to) {
    if (from.is(IntrinsicType.ANSICHAR)) {
      return compareAnsiCharToString(to);
    }

    if (from.is(IntrinsicType.WIDECHAR)) {
      return compareWideCharToString(to);
    }

    throw new AssertionError("Unhandled char type");
  }

  @VisibleForTesting
  static EqualityType compareCharToChar(Type from, Type to) {
    if (from.is(IntrinsicType.ANSICHAR)) {
      return CONVERT_LEVEL_1;
    }

    if (from.is(IntrinsicType.WIDECHAR)) {
      return compareWideCharToChar(to);
    }

    throw new AssertionError("Unhandled char type");
  }

  @VisibleForTesting
  static EqualityType compareAnsiCharToString(Type to) {
    if (to.is(IntrinsicType.SHORTSTRING)) {
      return CONVERT_LEVEL_2;
    } else if (to.isAnsiString()) {
      return CONVERT_LEVEL_3;
    } else if (to.is(IntrinsicType.UNICODESTRING)) {
      return CONVERT_LEVEL_4;
    } else if (to.is(IntrinsicType.WIDESTRING)) {
      return CONVERT_LEVEL_5;
    }
    return INCOMPATIBLE_TYPES;
  }

  @VisibleForTesting
  static EqualityType compareWideCharToString(Type to) {
    if (to.is(IntrinsicType.UNICODESTRING)) {
      return CONVERT_LEVEL_3;
    } else if (to.is(IntrinsicType.WIDESTRING)) {
      return CONVERT_LEVEL_4;
    } else if (to.isAnsiString()) {
      return CONVERT_LEVEL_5;
    } else if (to.is(IntrinsicType.SHORTSTRING)) {
      return CONVERT_LEVEL_6;
    }
    return INCOMPATIBLE_TYPES;
  }

  @VisibleForTesting
  static EqualityType compareWideCharToChar(Type to) {
    if (to.is(IntrinsicType.WIDECHAR)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(IntrinsicType.ANSICHAR)) {
      return CONVERT_LEVEL_2;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType comparePointerToString(PointerType from, StringType to) {
    if (from.dereferencedType().isChar()) {
      if (from.dereferencedType().is(to.characterType())) {
        return CONVERT_LEVEL_3;
      } else {
        return CONVERT_LEVEL_4;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareArrayToString(CollectionType from, Type to) {
    if (from.isDynamicArray()) {
      return INCOMPATIBLE_TYPES;
    }

    if (from.elementType().is(IntrinsicType.ANSICHAR)) {
      if (to.isAnsiString()) {
        return CONVERT_LEVEL_2;
      } else if (to.is(IntrinsicType.WIDESTRING)) {
        return CONVERT_LEVEL_3;
      } else if (to.is(IntrinsicType.UNICODESTRING)) {
        return CONVERT_LEVEL_4;
      }
    } else if (from.elementType().is(IntrinsicType.WIDECHAR)) {
      if (to.is(IntrinsicType.UNICODESTRING)) {
        return CONVERT_LEVEL_2;
      } else if (to.is(IntrinsicType.WIDESTRING)) {
        return CONVERT_LEVEL_3;
      } else if (to.isAnsiString()) {
        return CONVERT_LEVEL_4;
      }
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareBoolean(Type from, Type to) {
    if (from.isBoolean()) {
      BooleanType fromBoolean = (BooleanType) from;
      BooleanType toBoolean = (BooleanType) to;
      if (fromBoolean.size() > toBoolean.size()) {
        // Penalty for bad type conversion
        return CONVERT_LEVEL_3;
      }
      return CONVERT_LEVEL_1;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareEnum(Type from, Type to) {
    if (from.isSubrange()) {
      from = ((SubrangeType) from).hostType();
    }
    if ((from.isEnum() && from.is(to)) || from.isVariant()) {
      return CONVERT_LEVEL_1;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareSubrange(Type from, Type to) {
    to = ((SubrangeType) to).hostType();
    if (from.isSubrange()) {
      from = ((SubrangeType) from).hostType();
    }

    if (from.is(to) || from.isVariant()) {
      return CONVERT_LEVEL_1;
    } else if (from.isInteger() && to.isInteger()) {
      return compareInteger(from, to);
    } else {
      return INCOMPATIBLE_TYPES;
    }
  }

  private static EqualityType compareArray(Type from, Type to) {
    CollectionType toArray = ((CollectionType) to);

    if (to.isOpenArray() && equals(from, toArray.elementType())) {
      // Open array is also compatible with a single element of its base type.
      return CONVERT_LEVEL_3;
    }

    if (from.isArray()) {
      CollectionType fromArray = ((CollectionType) from);
      if (to.isDynamicArray()) {
        return compareDynamicArray(fromArray, toArray);
      } else if (to.isOpenArray()) {
        return compareOpenArray(fromArray, toArray);
      } else {
        return compareFixedArray(fromArray, toArray);
      }
    } else if (from.isArrayConstructor()) {
      return compareArrayConstructorToArray((ArrayConstructorType) from, toArray);
    } else if (from.isPointer()) {
      return comparePointerToArray((PointerType) from, toArray);
    } else if (from.isChar()) {
      return compareCharToArray(from, toArray);
    } else if (from.isVariant()) {
      return compareVariantToArray(toArray);
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareArrayConstructorToArray(
      ArrayConstructorType from, CollectionType to) {
    if (to.isArrayOfConst()) {
      return EQUAL;
    } else if (to.isDynamicArray()) {
      return compareArrayConstructorToDynamicArray(from, to);
    } else if (to.isOpenArray()) {
      return compareArrayConstructorToOpenArray(from, to);
    }
    return INCOMPATIBLE_TYPES;
  }

  private static Type getLargestType(List<Type> types) {
    return types.stream().max(Comparator.comparingInt(Type::size)).orElseThrow();
  }

  private static EqualityType compareArrayConstructorToDynamicArray(
      ArrayConstructorType from, CollectionType to) {
    if (from.isEmpty()) {
      return CONVERT_LEVEL_3;
    }

    // this should lose to the array constructor -> open array / set conversions,
    // but it might happen that the end of the convert levels is reached
    var subEquality = compare(getLargestType(from.elementTypes()), to.elementType());
    if (subEquality.ordinal() >= EQUAL.ordinal()) {
      return CONVERT_LEVEL_3;
    } else if (subEquality.ordinal() > CONVERT_LEVEL_5.ordinal()) {
      // an array constructor is not a dynamic array, so use a lower level of compatibility
      // than that one of the elements
      return EqualityType.values()[subEquality.ordinal() - 3];
    } else if (subEquality == CONVERT_LEVEL_6) {
      return CONVERT_LEVEL_5;
    } else if (subEquality == CONVERT_LEVEL_7) {
      return CONVERT_LEVEL_6;
    } else if (subEquality == CONVERT_LEVEL_8) {
      return CONVERT_LEVEL_7;
    } else {
      return subEquality;
    }
  }

  private static EqualityType compareArrayConstructorToSet(
      ArrayConstructorType from, CollectionType to) {
    if (from.isEmpty()) {
      // Only needs to lose to [] -> open array
      return CONVERT_LEVEL_2;
    }

    // this should lose to the array constructor -> open array conversions,
    // but it might happen that the end of the convert levels is reached
    var subEquality = compare(getLargestType(from.elementTypes()), to.elementType());
    if (subEquality.ordinal() >= EQUAL.ordinal()) {
      return CONVERT_LEVEL_2;
    } else if (subEquality.ordinal() > CONVERT_LEVEL_6.ordinal()) {
      // an array constructor is not a dynamic array, so use a lower level of compatibility
      // than that one of the elements
      return EqualityType.values()[subEquality.ordinal() - 2];
    } else if (subEquality == CONVERT_LEVEL_7) {
      return CONVERT_LEVEL_6;
    } else if (subEquality == CONVERT_LEVEL_8) {
      return CONVERT_LEVEL_7;
    } else {
      return subEquality;
    }
  }

  private static EqualityType compareArrayConstructorToOpenArray(
      ArrayConstructorType from, CollectionType to) {
    if (from.isEmpty()) {
      return CONVERT_LEVEL_1;
    } else {
      var subEquality = compare(getLargestType(from.elementTypes()), to.elementType());
      if (subEquality.ordinal() >= EQUAL.ordinal()) {
        return CONVERT_LEVEL_1;
      } else if (subEquality.ordinal() > CONVERT_LEVEL_7.ordinal()) {
        // an array constructor is not an open array, so use a lower level of compatibility
        // than that one of the elements
        return EqualityType.values()[subEquality.ordinal() - 1];
      } else {
        return subEquality;
      }
    }
  }

  private static EqualityType compareArrayConstructor(Type from, Type to) {
    if (from.isArrayConstructor()) {
      var fromArrayConstructor = (ArrayConstructorType) from;
      var toArrayConstructor = (ArrayConstructorType) to;

      if (!fromArrayConstructor.isEmpty()) {
        return compare(
            getLargestType(fromArrayConstructor.elementTypes()),
            getLargestType(toArrayConstructor.elementTypes()));
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareDynamicArray(CollectionType from, CollectionType to) {
    if (equals(from.elementType(), to.elementType())) {
      return from.isDynamicArray() ? EQUAL : CONVERT_LEVEL_5;
    }
    return INCOMPATIBLE_TYPES;
  }

  @VisibleForTesting
  static EqualityType compareOpenArray(CollectionType from, CollectionType to) {
    if (equals(from.elementType(), to.elementType())) {
      if (from.isDynamicArray()) {
        if (from.elementType().is(to.elementType())) {
          return CONVERT_LEVEL_1;
        }
        return CONVERT_LEVEL_2;
      } else if (from.isOpenArray()) {
        if (from.elementType().is(to.elementType())) {
          return EXACT;
        }
        return EQUAL;
      } else if (from.isFixedArray()) {
        return EQUAL;
      }
    }

    if (from.isOpenArray()
        && from.elementType().is(IntrinsicType.ANSICHAR)
        && to.elementType().is(IntrinsicType.WIDECHAR)) {
      return CONVERT_LEVEL_5;
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareFixedArray(CollectionType from, CollectionType to) {
    if (from.isOpenArray() && equals(from.elementType(), to.elementType())) {
      return EQUAL;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType comparePointerToArray(PointerType from, CollectionType to) {
    if (equals(from.dereferencedType(), to.elementType())) {
      return CONVERT_LEVEL_3;
    } else if (to.isDynamicArray() && (from.isNilPointer() || from.isUntypedPointer())) {
      return CONVERT_LEVEL_5;
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareCharToArray(Type from, CollectionType to) {
    boolean ansiCharToArray =
        from.is(IntrinsicType.ANSICHAR) && to.elementType().is(IntrinsicType.ANSICHAR);

    return ansiCharToArray ? CONVERT_LEVEL_1 : INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareVariantToArray(CollectionType to) {
    return to.isDynamicArray() ? CONVERT_LEVEL_1 : INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareSet(Type from, Type to) {
    CollectionType toSet = ((CollectionType) to);
    if (from.isSet()) {
      CollectionType fromSet = (CollectionType) from;
      if (fromSet.elementType().isVoid() || toSet.elementType().isVoid()) {
        // Empty set is compatible with everything
        return CONVERT_LEVEL_1;
      } else if (equals(fromSet.elementType(), toSet.elementType())) {
        return EQUAL;
      }
    } else if (from.isArrayConstructor()) {
      return compareArrayConstructorToSet((ArrayConstructorType) from, toSet);
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareProceduralType(Type from, Type to) {
    if (from.isProcedural()) {
      return procToProcVarEqual((ProceduralType) from, (ProceduralType) to);
    } else if (from.isPointer()) {
      PointerType fromPointer = (PointerType) from;
      if (fromPointer.isUntypedPointer()) {
        return CONVERT_LEVEL_3;
      } else if (fromPointer.isNilPointer()) {
        return CONVERT_LEVEL_2;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType procToProcVarEqual(ProceduralType from, ProceduralType to) {
    if (equals(from.returnType(), to.returnType())) {
      var paramEquality = compareParameters(from.parameters(), to.parameters());
      if (paramEquality == EXACT) {
        return EQUAL;
      } else if (paramEquality == EQUAL) {
        return CONVERT_LEVEL_1;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareParameters(List<Parameter> from, List<Parameter> to) {
    if (from.size() != to.size()) {
      return INCOMPATIBLE_TYPES;
    }

    EqualityType lowestEquality = EXACT;

    for (int i = 0; i < to.size(); ++i) {
      EqualityType equality = compare(from.get(i).getType(), to.get(i).getType());
      if (equality.ordinal() < lowestEquality.ordinal()) {
        lowestEquality = equality;
      }
    }

    return lowestEquality;
  }

  private static EqualityType compareObject(Type from, Type to) {
    if (from.isStruct() && from.isSubTypeOf(to)) {
      return CONVERT_LEVEL_1;
    } else if (from.isPointer() && !to.isRecord()) {
      PointerType fromPointer = (PointerType) from;
      if (fromPointer.isUntypedPointer()) {
        return CONVERT_LEVEL_4;
      } else if (fromPointer.isNilPointer()) {
        return CONVERT_LEVEL_3;
      }
    } else if (isInterfaceReference(from) && isTGUID(to)) {
      return CONVERT_LEVEL_5;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static boolean isInterfaceReference(Type type) {
    return type.isClassReference() && ((ClassReferenceType) type).classType().isInterface();
  }

  private static boolean isTGUID(Type type) {
    return type.isRecord() && type.is("System.TGUID");
  }

  private static EqualityType compareClassReference(Type from, Type to) {
    if (from.isClassReference()) {
      Type fromReference = ((ClassReferenceType) from).classType();
      Type toReference = ((ClassReferenceType) to).classType();
      if (fromReference.is(toReference)) {
        return EQUAL;
      } else if (fromReference.isSubTypeOf(toReference)) {
        return CONVERT_LEVEL_1;
      }
    } else if (from.isPointer()) {
      PointerType fromPointer = (PointerType) from;
      if (fromPointer.isUntypedPointer()) {
        return CONVERT_LEVEL_5;
      } else if (fromPointer.isNilPointer()) {
        return CONVERT_LEVEL_4;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareFile(Type from, Type to) {
    if (from.isFile()) {
      Type fromFileType = ((FileType) from).fileType();
      Type toFileType = ((FileType) to).fileType();

      if (fromFileType.isUntyped() != toFileType.isUntyped()) {
        return CONVERT_LEVEL_1;
      } else if (equals(fromFileType, toFileType)) {
        return EQUAL;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType comparePointer(Type from, Type to) {
    PointerType pointerTo = (PointerType) to;
    if (from.isPointer()) {
      return comparePointerToPointer((PointerType) from, pointerTo);
    } else if (from.isArray()) {
      return compareArrayToPointer((CollectionType) from, pointerTo);
    } else if (from.isString()) {
      return compareStringToPointer((StringType) from, pointerTo);
    } else if (from.isChar()) {
      return compareCharToPointer(pointerTo);
    } else if (from.isStruct() && !from.isRecord() && pointerTo.isUntypedPointer()) {
      return CONVERT_LEVEL_5;
    } else if (from.isClassReference() && pointerTo.isUntypedPointer()) {
      return CONVERT_LEVEL_5;
    } else if (from.isInteger()) {
      return CONVERT_LEVEL_6;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType comparePointerToPointer(PointerType from, PointerType to) {
    if (equals(from.dereferencedType(), to.dereferencedType())) {
      return EQUAL;
    } else if (from.dereferencedType().isSubTypeOf(to.dereferencedType())) {
      return CONVERT_LEVEL_1;
    } else if (to.isUntypedPointer()) {
      // All pointers can be assigned to untyped pointer
      if (from.dereferencedType().isChar()) {
        return CONVERT_LEVEL_2;
      }
      return CONVERT_LEVEL_1;
    } else if (from.isNilPointer() || from.isUntypedPointer()) {
      // All pointers can be assigned from nil or untyped pointers
      if (to.dereferencedType().is(IntrinsicType.WIDECHAR)) {
        return CONVERT_LEVEL_2;
      }
      return CONVERT_LEVEL_1;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareArrayToPointer(CollectionType from, PointerType to) {
    if (equals(from.elementType(), to.dereferencedType())) {
      return CONVERT_LEVEL_3;
    } else if (to.isUntypedPointer()) {
      return CONVERT_LEVEL_4;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareStringToPointer(StringType from, PointerType to) {
    if (from.is(IntrinsicType.UNICODESTRING) && to.dereferencedType().isChar()) {
      if (to.dereferencedType().is(from.characterType())) {
        return CONVERT_LEVEL_2;
      } else {
        return CONVERT_LEVEL_3;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareCharToPointer(PointerType to) {
    if (to.dereferencedType().isChar()) {
      if (to.dereferencedType().is(IntrinsicType.WIDECHAR)) {
        return CONVERT_LEVEL_1;
      } else {
        return CONVERT_LEVEL_2;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareUntyped(Type from) {
    return from.isUntyped() ? EQUAL : CONVERT_LEVEL_7;
  }

  private static EqualityType compareVariant(Type from) {
    boolean oleVariant = from.isVariant() && ((VariantType) from).kind() == VariantKind.OLE_VARIANT;

    if (oleVariant || isConvertibleToVariant(from)) {
      return CONVERT_LEVEL_1;
    }

    return INCOMPATIBLE_TYPES;
  }

  private static boolean isConvertibleToVariant(Type type) {
    return type.isEnum()
        || type.isDynamicArray()
        || type.isInterface()
        || VariantConversionType.fromType(type) != INCOMPATIBLE_VARIANT;
  }

  private static EqualityType tryIntrinsicArgumentTypes(Type from, Type to) {
    if (to instanceof IntrinsicArgumentMatcher && ((IntrinsicArgumentMatcher) to).matches(from)) {
      return EQUAL;
    }

    return INCOMPATIBLE_TYPES;
  }

  static boolean equals(Type from, Type to) {
    return compare(from, to).ordinal() >= EQUAL.ordinal();
  }
}
