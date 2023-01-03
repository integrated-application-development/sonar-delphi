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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.DelphiType.untypedType;
import static org.sonar.plugins.delphi.type.DelphiType.voidType;
import static org.sonar.plugins.delphi.type.StructKind.CLASS;
import static org.sonar.plugins.delphi.type.StructKind.INTERFACE;
import static org.sonar.plugins.delphi.type.StructKind.OBJECT;
import static org.sonar.plugins.delphi.type.StructKind.RECORD;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_DYNAMIC_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.type.ArrayOption;
import org.sonar.plugins.delphi.type.CodePages;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.AnsiStringType;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.Type.SubrangeType;
import org.sonar.plugins.delphi.type.Type.TypeType;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;
import org.sonar.plugins.delphi.utils.types.TypeMocker;

class TypeComparerTest {
  private static final TypeFactory FACTORY = TypeFactoryUtils.defaultFactory();

  private static void compare(Type from, Type to, EqualityType equality) {
    assertThat(TypeComparer.compare(from, to)).isEqualTo(equality);
  }

  private static Type toType(Object object) {
    if (object instanceof IntrinsicType) {
      return FACTORY.getIntrinsic((IntrinsicType) object);
    }

    if (object instanceof Type) {
      return (Type) object;
    }

    throw new AssertionError(object.toString() + " is not convertible to Type.");
  }

  private static void compare(Object from, Object to, EqualityType equality) {
    compare(toType(from), toType(to), equality);
  }

  @Test
  void testExactTypes() {
    compare(IntrinsicType.UNICODESTRING, IntrinsicType.UNICODESTRING, EXACT);
  }

  @Test
  void testToInteger() {
    compare(IntrinsicType.SMALLINT, IntrinsicType.INTEGER, CONVERT_LEVEL_1);
    compare(IntrinsicType.INTEGER, IntrinsicType.SMALLINT, CONVERT_LEVEL_3);
    compare(IntrinsicType.VARIANT, IntrinsicType.INTEGER, CONVERT_LEVEL_7);
    compare(subRange("0..5", IntrinsicType.SHORTINT), IntrinsicType.SHORTINT, EQUAL);
    compare(subRange("-100..100", IntrinsicType.BYTE), IntrinsicType.SHORTINT, CONVERT_LEVEL_1);
    compare(IntrinsicType.CURRENCY, IntrinsicType.INTEGER, INCOMPATIBLE_TYPES);
    compare(IntrinsicType.UNICODESTRING, IntrinsicType.INTEGER, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToDecimal() {
    compare(IntrinsicType.INTEGER, IntrinsicType.SINGLE, CONVERT_LEVEL_1);
    compare(IntrinsicType.INTEGER, IntrinsicType.REAL48, CONVERT_LEVEL_2);
    compare(IntrinsicType.INTEGER, IntrinsicType.DOUBLE, CONVERT_LEVEL_3);
    compare(IntrinsicType.INTEGER, IntrinsicType.EXTENDED, CONVERT_LEVEL_4);
    compare(IntrinsicType.INTEGER, IntrinsicType.COMP, CONVERT_LEVEL_5);

    compare(IntrinsicType.INT64, IntrinsicType.EXTENDED, CONVERT_LEVEL_1);
    compare(IntrinsicType.INT64, IntrinsicType.DOUBLE, CONVERT_LEVEL_2);
    compare(IntrinsicType.INT64, IntrinsicType.REAL48, CONVERT_LEVEL_3);
    compare(IntrinsicType.INT64, IntrinsicType.SINGLE, CONVERT_LEVEL_4);
    compare(IntrinsicType.INT64, IntrinsicType.COMP, CONVERT_LEVEL_5);

    compare(IntrinsicType.UINT64, IntrinsicType.EXTENDED, CONVERT_LEVEL_1);
    compare(IntrinsicType.UINT64, IntrinsicType.DOUBLE, CONVERT_LEVEL_2);
    compare(IntrinsicType.UINT64, IntrinsicType.REAL48, CONVERT_LEVEL_3);
    compare(IntrinsicType.UINT64, IntrinsicType.SINGLE, CONVERT_LEVEL_4);
    compare(IntrinsicType.UINT64, IntrinsicType.COMP, CONVERT_LEVEL_5);

    compare(IntrinsicType.UNICODESTRING, IntrinsicType.DOUBLE, INCOMPATIBLE_TYPES);
  }

  @Test
  void testDecimalToDecimal() {
    compare(IntrinsicType.SINGLE, typeType("Single_", IntrinsicType.SINGLE), EQUAL);
    compare(IntrinsicType.SINGLE, IntrinsicType.REAL48, CONVERT_LEVEL_1);
    compare(IntrinsicType.SINGLE, IntrinsicType.DOUBLE, CONVERT_LEVEL_2);
    compare(IntrinsicType.SINGLE, IntrinsicType.EXTENDED, CONVERT_LEVEL_3);
    compare(IntrinsicType.SINGLE, IntrinsicType.CURRENCY, CONVERT_LEVEL_6);
    compare(IntrinsicType.SINGLE, IntrinsicType.COMP, CONVERT_LEVEL_6);

    compare(IntrinsicType.REAL48, IntrinsicType.DOUBLE, CONVERT_LEVEL_1);
    compare(IntrinsicType.REAL48, IntrinsicType.EXTENDED, CONVERT_LEVEL_2);
    compare(IntrinsicType.REAL48, IntrinsicType.SINGLE, CONVERT_LEVEL_5);
    compare(IntrinsicType.REAL48, IntrinsicType.CURRENCY, CONVERT_LEVEL_6);
    compare(IntrinsicType.REAL48, IntrinsicType.COMP, CONVERT_LEVEL_6);

    compare(IntrinsicType.DOUBLE, IntrinsicType.EXTENDED, CONVERT_LEVEL_1);
    compare(IntrinsicType.DOUBLE, IntrinsicType.REAL48, CONVERT_LEVEL_4);
    compare(IntrinsicType.DOUBLE, IntrinsicType.SINGLE, CONVERT_LEVEL_5);
    compare(IntrinsicType.DOUBLE, IntrinsicType.CURRENCY, CONVERT_LEVEL_6);
    compare(IntrinsicType.DOUBLE, IntrinsicType.COMP, CONVERT_LEVEL_6);

    compare(IntrinsicType.EXTENDED, IntrinsicType.DOUBLE, CONVERT_LEVEL_4);
    compare(IntrinsicType.EXTENDED, IntrinsicType.REAL48, CONVERT_LEVEL_5);
    compare(IntrinsicType.EXTENDED, IntrinsicType.SINGLE, CONVERT_LEVEL_6);
    compare(IntrinsicType.EXTENDED, IntrinsicType.CURRENCY, CONVERT_LEVEL_7);
    compare(IntrinsicType.EXTENDED, IntrinsicType.COMP, CONVERT_LEVEL_7);

    assertThatThrownBy(
            () -> TypeComparer.compareDecimalToDecimal(unknownType(), toType(IntrinsicType.SINGLE)))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testIntegerRealsToDecimal() {
    compare(IntrinsicType.CURRENCY, IntrinsicType.EXTENDED, CONVERT_LEVEL_1);
    compare(IntrinsicType.CURRENCY, IntrinsicType.DOUBLE, CONVERT_LEVEL_2);
    compare(IntrinsicType.CURRENCY, IntrinsicType.REAL48, CONVERT_LEVEL_3);
    compare(IntrinsicType.CURRENCY, IntrinsicType.SINGLE, CONVERT_LEVEL_4);
    compare(IntrinsicType.CURRENCY, IntrinsicType.COMP, CONVERT_LEVEL_5);

    compare(IntrinsicType.COMP, IntrinsicType.EXTENDED, CONVERT_LEVEL_1);
    compare(IntrinsicType.COMP, IntrinsicType.DOUBLE, CONVERT_LEVEL_2);
    compare(IntrinsicType.COMP, IntrinsicType.REAL48, CONVERT_LEVEL_3);
    compare(IntrinsicType.COMP, IntrinsicType.SINGLE, CONVERT_LEVEL_4);
    compare(IntrinsicType.COMP, IntrinsicType.CURRENCY, CONVERT_LEVEL_5);
  }

  @Test
  void testStringToString() {
    compare(IntrinsicType.WIDESTRING, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_1);
    compare(IntrinsicType.WIDESTRING, IntrinsicType.ANSISTRING, CONVERT_LEVEL_2);
    compare(IntrinsicType.WIDESTRING, IntrinsicType.SHORTSTRING, CONVERT_LEVEL_3);
    compare(IntrinsicType.WIDESTRING, IntrinsicType.CHAR, INCOMPATIBLE_TYPES);

    compare(IntrinsicType.UNICODESTRING, IntrinsicType.WIDESTRING, CONVERT_LEVEL_1);
    compare(IntrinsicType.UNICODESTRING, IntrinsicType.ANSISTRING, CONVERT_LEVEL_2);
    compare(IntrinsicType.UNICODESTRING, IntrinsicType.SHORTSTRING, CONVERT_LEVEL_3);
    compare(IntrinsicType.UNICODESTRING, IntrinsicType.CHAR, INCOMPATIBLE_TYPES);

    compare(IntrinsicType.SHORTSTRING, IntrinsicType.ANSISTRING, CONVERT_LEVEL_1);
    compare(IntrinsicType.SHORTSTRING, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_2);
    compare(IntrinsicType.SHORTSTRING, IntrinsicType.WIDESTRING, CONVERT_LEVEL_3);
    compare(IntrinsicType.SHORTSTRING, IntrinsicType.CHAR, INCOMPATIBLE_TYPES);

    compare(IntrinsicType.ANSISTRING, typeType("_", IntrinsicType.ANSISTRING), EQUAL);
    compare(typeType("_", IntrinsicType.ANSISTRING), IntrinsicType.ANSISTRING, EQUAL);
    compare(IntrinsicType.ANSISTRING, ansiString(CodePages.CP_NONE), EQUAL);
    compare(ansiString(CodePages.CP_NONE), typeType("_", ansiString(CodePages.CP_NONE)), EQUAL);
    compare(IntrinsicType.ANSISTRING, ansiString(CodePages.CP_UTF8), CONVERT_LEVEL_1);
    compare(ansiString(CodePages.CP_1252), IntrinsicType.ANSISTRING, CONVERT_LEVEL_2);
    compare(ansiString(CodePages.CP_NONE), IntrinsicType.ANSISTRING, CONVERT_LEVEL_2);
    compare(ansiString(CodePages.CP_1252), ansiString(50937), CONVERT_LEVEL_3);
    compare(IntrinsicType.ANSISTRING, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_4);
    compare(IntrinsicType.ANSISTRING, IntrinsicType.WIDESTRING, CONVERT_LEVEL_5);
    compare(IntrinsicType.ANSISTRING, IntrinsicType.SHORTSTRING, CONVERT_LEVEL_6);
    compare(IntrinsicType.ANSISTRING, IntrinsicType.CHAR, INCOMPATIBLE_TYPES);

    compare(typeType("Test", IntrinsicType.UNICODESTRING), IntrinsicType.UNICODESTRING, EQUAL);

    assertThatThrownBy(
            () ->
                TypeComparer.compareStringToString(
                    unknownType(), toType(IntrinsicType.UNICODESTRING)))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testArrayToString() {
    Type ansiCharOpenArray = openArray(null, IntrinsicType.ANSICHAR);
    Type wideCharOpenArray = openArray(null, IntrinsicType.WIDECHAR);
    Type ansiCharFixedArray = fixedArray(null, IntrinsicType.ANSICHAR);
    Type wideCharFixedArray = fixedArray(null, IntrinsicType.WIDECHAR);

    compare(ansiCharOpenArray, IntrinsicType.ANSISTRING, CONVERT_LEVEL_2);
    compare(ansiCharOpenArray, IntrinsicType.WIDESTRING, CONVERT_LEVEL_3);
    compare(ansiCharOpenArray, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_4);
    compare(ansiCharOpenArray, IntrinsicType.INTEGER, INCOMPATIBLE_TYPES);
    compare(wideCharOpenArray, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_2);
    compare(wideCharOpenArray, IntrinsicType.WIDESTRING, CONVERT_LEVEL_3);
    compare(wideCharOpenArray, IntrinsicType.ANSISTRING, CONVERT_LEVEL_4);
    compare(wideCharOpenArray, IntrinsicType.INTEGER, INCOMPATIBLE_TYPES);
    compare(openArray(null, unknownType()), IntrinsicType.ANSISTRING, INCOMPATIBLE_TYPES);

    compare(ansiCharFixedArray, IntrinsicType.ANSISTRING, CONVERT_LEVEL_2);
    compare(ansiCharFixedArray, IntrinsicType.WIDESTRING, CONVERT_LEVEL_3);
    compare(ansiCharFixedArray, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_4);
    compare(ansiCharFixedArray, IntrinsicType.INTEGER, INCOMPATIBLE_TYPES);
    compare(wideCharFixedArray, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_2);
    compare(wideCharFixedArray, IntrinsicType.WIDESTRING, CONVERT_LEVEL_3);
    compare(wideCharFixedArray, IntrinsicType.ANSISTRING, CONVERT_LEVEL_4);
    compare(wideCharFixedArray, IntrinsicType.INTEGER, INCOMPATIBLE_TYPES);
    compare(fixedArray(null, unknownType()), IntrinsicType.ANSISTRING, INCOMPATIBLE_TYPES);

    compare(
        dynamicArray(null, IntrinsicType.ANSICHAR), IntrinsicType.ANSISTRING, INCOMPATIBLE_TYPES);
  }

  @Test
  void testPointerToString() {
    compare(IntrinsicType.PANSICHAR, IntrinsicType.ANSISTRING, CONVERT_LEVEL_3);
    compare(IntrinsicType.PCHAR, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_3);
    compare(IntrinsicType.PANSICHAR, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_4);
    compare(IntrinsicType.PCHAR, IntrinsicType.ANSISTRING, CONVERT_LEVEL_4);
    compare(pointerTo(IntrinsicType.INTEGER), IntrinsicType.UNICODESTRING, INCOMPATIBLE_TYPES);
  }

  @Test
  void testCharToString() {
    compare(IntrinsicType.ANSICHAR, IntrinsicType.SHORTSTRING, CONVERT_LEVEL_2);
    compare(IntrinsicType.ANSICHAR, IntrinsicType.ANSISTRING, CONVERT_LEVEL_3);
    compare(IntrinsicType.ANSICHAR, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_4);
    compare(IntrinsicType.ANSICHAR, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_4);
    compare(IntrinsicType.ANSICHAR, IntrinsicType.WIDESTRING, CONVERT_LEVEL_5);
    assertThat(TypeComparer.compareAnsiCharToString(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    compare(IntrinsicType.CHAR, IntrinsicType.UNICODESTRING, CONVERT_LEVEL_3);
    compare(IntrinsicType.CHAR, IntrinsicType.WIDESTRING, CONVERT_LEVEL_4);
    compare(IntrinsicType.CHAR, IntrinsicType.ANSISTRING, CONVERT_LEVEL_5);
    compare(IntrinsicType.CHAR, IntrinsicType.SHORTSTRING, CONVERT_LEVEL_6);
    assertThat(TypeComparer.compareWideCharToString(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    assertThatThrownBy(() -> TypeComparer.compareCharToString(unknownType(), unknownType()))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testCharToChar() {
    compare(IntrinsicType.ANSICHAR, typeType("_AnsiChar", IntrinsicType.ANSICHAR), CONVERT_LEVEL_1);
    compare(IntrinsicType.CHAR, typeType("_WideChar", IntrinsicType.WIDECHAR), CONVERT_LEVEL_1);
    compare(IntrinsicType.WIDECHAR, IntrinsicType.ANSICHAR, CONVERT_LEVEL_2);
    assertThat(TypeComparer.compareWideCharToChar(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    assertThatThrownBy(() -> TypeComparer.compareCharToString(unknownType(), unknownType()))
        .isInstanceOf(AssertionError.class);

    assertThatThrownBy(() -> TypeComparer.compareCharToChar(unknownType(), unknownType()))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testToBoolean() {
    compare(IntrinsicType.BOOLEAN, IntrinsicType.BYTEBOOL, CONVERT_LEVEL_1);
    compare(IntrinsicType.BOOLEAN, IntrinsicType.WORDBOOL, CONVERT_LEVEL_1);
    compare(IntrinsicType.WORDBOOL, IntrinsicType.BOOLEAN, CONVERT_LEVEL_3);
    compare(IntrinsicType.UNICODESTRING, IntrinsicType.BOOLEAN, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToEnum() {
    EnumType enumType = enumeration("Enum1");
    EnumType enumType2 = enumeration("Enum2");
    SubrangeType subrangeType = subRange("Subrange", enumType);

    compare(subrangeType, enumType, CONVERT_LEVEL_1);
    compare(subrangeType, enumType2, INCOMPATIBLE_TYPES);
    compare(enumType, enumType2, INCOMPATIBLE_TYPES);
    compare(IntrinsicType.VARIANT, enumType, CONVERT_LEVEL_1);
    compare(IntrinsicType.INTEGER, enumType, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToSubrange() {
    SubrangeType subrangeOfShortInt = subRange("1..100", IntrinsicType.SHORTINT);
    SubrangeType subrangeOfInteger = subRange("5..High(Integer)", IntrinsicType.INTEGER);
    SubrangeType subrangeOfEnum = subRange("5..6", enumeration("Enum"));

    compare(subrangeOfShortInt, typeType("Foo", subrangeOfShortInt), CONVERT_LEVEL_1);
    compare(IntrinsicType.VARIANT, subrangeOfShortInt, CONVERT_LEVEL_1);
    compare(subrangeOfShortInt, subrangeOfInteger, CONVERT_LEVEL_1);
    compare(subrangeOfInteger, subrangeOfShortInt, CONVERT_LEVEL_3);
    compare(subrangeOfShortInt, subrangeOfEnum, INCOMPATIBLE_TYPES);
    compare(subrangeOfEnum, subrangeOfShortInt, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToArray() {
    CollectionType fromOpenArray = openArray("Open", IntrinsicType.INTEGER);
    CollectionType fromDynamicArray = dynamicArray("Dynamic", IntrinsicType.INTEGER);
    CollectionType fromFixedArray = fixedArray("Fixed", IntrinsicType.INTEGER);
    CollectionType fromIncompatibleOpenArray = openArray("OpenString", IntrinsicType.UNICODESTRING);
    CollectionType fromIncompatibleDynamicArray =
        dynamicArray("DynamicString", IntrinsicType.UNICODESTRING);
    CollectionType fromIncompatibleFixedArray =
        fixedArray("FixedString", IntrinsicType.UNICODESTRING);
    CollectionType toDynamicArray = dynamicArray(null, IntrinsicType.INTEGER);
    CollectionType toFixedArray = fixedArray(null, IntrinsicType.INTEGER);
    CollectionType toOpenArray = openArray(null, IntrinsicType.INTEGER);
    CollectionType toSimilarOpenArray = openArray(null, IntrinsicType.NATIVEINT);

    compare(IntrinsicType.INTEGER, toOpenArray, CONVERT_LEVEL_3);
    compare(fromDynamicArray, toDynamicArray, EQUAL);
    compare(fromFixedArray, toDynamicArray, CONVERT_LEVEL_2);
    compare(dynamicArray(null, IntrinsicType.UNICODESTRING), toDynamicArray, INCOMPATIBLE_TYPES);

    compare(fromDynamicArray, toOpenArray, CONVERT_LEVEL_1);
    compare(fromDynamicArray, toSimilarOpenArray, CONVERT_LEVEL_2);
    compare(fromIncompatibleDynamicArray, toOpenArray, INCOMPATIBLE_TYPES);
    compare(fromOpenArray, toOpenArray, EXACT);
    compare(openArray(null, IntrinsicType.NATIVEINT), toOpenArray, EQUAL);
    compare(fromFixedArray, toOpenArray, EQUAL);
    compare(fromIncompatibleFixedArray, toOpenArray, INCOMPATIBLE_TYPES);
    compare(openArray(null, unknownType()), toOpenArray, INCOMPATIBLE_TYPES);
    compare(
        openArray(null, IntrinsicType.ANSICHAR),
        openArray(null, IntrinsicType.CHAR),
        CONVERT_LEVEL_5);
    compare(
        openArray(null, IntrinsicType.ANSICHAR),
        openArray(null, IntrinsicType.INTEGER),
        INCOMPATIBLE_TYPES);

    compare(fromOpenArray, toFixedArray, EQUAL);
    compare(fromIncompatibleOpenArray, toFixedArray, INCOMPATIBLE_TYPES);
    compare(fromDynamicArray, toFixedArray, INCOMPATIBLE_TYPES);

    compare(pointerTo(IntrinsicType.INTEGER), toOpenArray, CONVERT_LEVEL_3);
    compare(nilPointer(), toDynamicArray, CONVERT_LEVEL_5);
    compare(untypedPointer(), toDynamicArray, CONVERT_LEVEL_5);
    compare(pointerTo(IntrinsicType.UNICODESTRING), toOpenArray, INCOMPATIBLE_TYPES);
    compare(pointerTo(unknownType()), toDynamicArray, INCOMPATIBLE_TYPES);

    compare(IntrinsicType.ANSICHAR, dynamicArray(null, IntrinsicType.ANSICHAR), CONVERT_LEVEL_1);
    compare(IntrinsicType.ANSICHAR, toDynamicArray, INCOMPATIBLE_TYPES);

    compare(IntrinsicType.VARIANT, toDynamicArray, CONVERT_LEVEL_1);
    compare(IntrinsicType.VARIANT, toOpenArray, CONVERT_LEVEL_8);

    compare(unknownType(), toOpenArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toDynamicArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toFixedArray, INCOMPATIBLE_TYPES);

    assertThat(TypeComparer.compareOpenArray(set(IntrinsicType.INTEGER), toOpenArray))
        .isEqualTo(INCOMPATIBLE_TYPES);
  }

  @Test
  void testArrayConstructorToArray() {
    CollectionType toDynamicArray = dynamicArray(null, IntrinsicType.INTEGER);
    CollectionType toDynamicPointerArray = dynamicArray(null, IntrinsicType.POINTER);
    CollectionType toIncompatibleDynamicArray =
        dynamicArray(null, TypeMocker.struct("Test", CLASS));
    CollectionType toFixedArray = fixedArray(null, IntrinsicType.INTEGER);
    CollectionType toOpenArray = openArray(null, IntrinsicType.INTEGER);
    CollectionType toArrayOfConst = arrayOfConst();

    ArrayConstructorType emptyConstructor = arrayConstructor(emptyList());
    ArrayConstructorType byteConstructor = arrayConstructor(List.of(IntrinsicType.BYTE));
    ArrayConstructorType integerConstructor = arrayConstructor(List.of(IntrinsicType.INTEGER));
    ArrayConstructorType stringConstructor = arrayConstructor(List.of(IntrinsicType.UNICODESTRING));
    ArrayConstructorType variantConstructor = arrayConstructor(List.of(IntrinsicType.VARIANT));
    ArrayConstructorType heterogeneousConstructor =
        arrayConstructor(
            List.of(IntrinsicType.INTEGER, IntrinsicType.UNICODESTRING, IntrinsicType.BOOLEAN));

    compare(emptyConstructor, toDynamicArray, CONVERT_LEVEL_3);
    compare(emptyConstructor, toOpenArray, CONVERT_LEVEL_1);
    compare(emptyConstructor, toFixedArray, INCOMPATIBLE_TYPES);

    compare(byteConstructor, toDynamicArray, CONVERT_LEVEL_4);
    compare(byteConstructor, toOpenArray, CONVERT_LEVEL_2);

    compare(integerConstructor, toDynamicPointerArray, CONVERT_LEVEL_5);
    compare(integerConstructor, toDynamicArray, CONVERT_LEVEL_3);
    compare(integerConstructor, toOpenArray, CONVERT_LEVEL_1);

    compare(stringConstructor, toDynamicArray, INCOMPATIBLE_TYPES);
    compare(stringConstructor, toOpenArray, INCOMPATIBLE_TYPES);

    compare(variantConstructor, toDynamicArray, CONVERT_LEVEL_6);
    compare(variantConstructor, toIncompatibleDynamicArray, CONVERT_LEVEL_7);
    compare(variantConstructor, toOpenArray, CONVERT_LEVEL_7);

    compare(byteConstructor, toArrayOfConst, EQUAL);
    compare(integerConstructor, toArrayOfConst, EQUAL);
    compare(heterogeneousConstructor, toArrayOfConst, EQUAL);
  }

  @Test
  void testArrayConstructorToSet() {
    CollectionType byteSet = set(IntrinsicType.BYTE);
    CollectionType classSet = set(TypeMocker.struct("Foo", CLASS));

    ArrayConstructorType emptyConstructor = arrayConstructor(emptyList());
    ArrayConstructorType byteConstructor = arrayConstructor(List.of(IntrinsicType.BYTE));
    ArrayConstructorType integerConstructor = arrayConstructor(List.of(IntrinsicType.INTEGER));
    ArrayConstructorType stringConstructor = arrayConstructor(List.of(IntrinsicType.UNICODESTRING));
    ArrayConstructorType variantConstructor = arrayConstructor(List.of(IntrinsicType.VARIANT));

    compare(emptyConstructor, byteSet, CONVERT_LEVEL_2);
    compare(byteConstructor, byteSet, CONVERT_LEVEL_2);
    compare(integerConstructor, byteSet, CONVERT_LEVEL_5);
    compare(variantConstructor, byteSet, CONVERT_LEVEL_6);
    compare(variantConstructor, classSet, CONVERT_LEVEL_7);
    compare(stringConstructor, byteSet, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToArrayConstructor() {
    ArrayConstructorType byteConstructor = arrayConstructor(List.of(IntrinsicType.BYTE));
    ArrayConstructorType int64Constructor = arrayConstructor(List.of(IntrinsicType.INT64));
    ArrayConstructorType emptyConstructor = arrayConstructor(Collections.emptyList());

    compare(byteConstructor, byteConstructor, EXACT);
    compare(byteConstructor, int64Constructor, CONVERT_LEVEL_1);
    compare(dynamicArray(null, IntrinsicType.INTEGER), byteConstructor, INCOMPATIBLE_TYPES);
    compare(emptyConstructor, byteConstructor, INCOMPATIBLE_TYPES);
    compare(set(IntrinsicType.BYTE), byteConstructor, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToSet() {
    // NOTE: Sets can't actually have such large ordinal element types (and certainly not strings)
    // This is just for testing convenience.
    CollectionType integerSet = set(IntrinsicType.INTEGER);
    CollectionType nativeIntSet = set(IntrinsicType.NATIVEINT);
    CollectionType stringSet = set(IntrinsicType.UNICODESTRING);

    compare(emptySet(), integerSet, CONVERT_LEVEL_1);
    compare(integerSet, emptySet(), CONVERT_LEVEL_1);
    compare(integerSet, nativeIntSet, EQUAL);
    compare(integerSet, stringSet, INCOMPATIBLE_TYPES);
    compare(unknownType(), integerSet, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToProceduralType() {
    List<Type> parameters = List.of(toType(IntrinsicType.INTEGER));
    List<Type> similarParameters = List.of(toType(IntrinsicType.NATIVEINT));
    Type returnType = toType(IntrinsicType.UNICODESTRING);
    Type incompatibleReturnType = toType(IntrinsicType.INTEGER);

    ProceduralType fromProcedure = procedure(parameters, returnType);
    ProceduralType similarFromProcedure = procedure(similarParameters, returnType);
    ProceduralType incompatibleReturnTypeProcedure = procedure(parameters, incompatibleReturnType);
    ProceduralType incompatibleParametersProcedure = procedure(emptyList(), returnType);
    ProceduralType toProcedure = anonymous(parameters, returnType);

    compare(fromProcedure, toProcedure, EQUAL);
    compare(similarFromProcedure, toProcedure, CONVERT_LEVEL_1);
    compare(incompatibleReturnTypeProcedure, toProcedure, INCOMPATIBLE_TYPES);
    compare(incompatibleParametersProcedure, toProcedure, INCOMPATIBLE_TYPES);

    compare(nilPointer(), toProcedure, CONVERT_LEVEL_2);
    compare(untypedPointer(), toProcedure, CONVERT_LEVEL_3);
    compare(pointerTo(returnType), toProcedure, INCOMPATIBLE_TYPES);

    compare(unknownType(), toProcedure, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToObject() {
    Type toClass = TypeMocker.struct("Foo", CLASS);
    Type fromClass = TypeMocker.struct("Bar", CLASS, toClass);
    Type toRecord = TypeMocker.struct("Baz", RECORD);
    Type fromInterface = TypeMocker.struct("System.IInterface", INTERFACE);
    Type toGUID = TypeMocker.struct("System.TGUID", RECORD);
    Type toNotGUID = TypeMocker.struct("System.TNotGUID", RECORD);

    compare(fromClass, toClass, CONVERT_LEVEL_1);
    compare(untypedPointer(), toClass, CONVERT_LEVEL_4);
    compare(nilPointer(), toClass, CONVERT_LEVEL_3);
    compare(classOf(fromInterface), toGUID, CONVERT_LEVEL_5);
    compare(IntrinsicType.VARIANT, toClass, CONVERT_LEVEL_8);
    compare(untypedPointer(), toRecord, INCOMPATIBLE_TYPES);
    compare(nilPointer(), toRecord, INCOMPATIBLE_TYPES);
    compare(pointerTo(toClass), toClass, INCOMPATIBLE_TYPES);
    compare(classOf(fromInterface), toNotGUID, INCOMPATIBLE_TYPES);
    compare(fromInterface, toGUID, INCOMPATIBLE_TYPES);
    compare(fromInterface, toRecord, INCOMPATIBLE_TYPES);
    compare(classOf(fromInterface), toRecord, INCOMPATIBLE_TYPES);
    compare(unknownType(), toClass, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToClassReference() {
    Type foo = TypeMocker.struct("Foo", CLASS);
    Type bar = TypeMocker.struct("Bar", CLASS, foo);
    Type baz = TypeMocker.struct("Baz", CLASS);

    Type fooReference = classOf(foo);
    Type namedFooReference = classOf("Flarp.Flimflam", foo);
    Type barReference = classOf(bar);
    Type bazReference = classOf(baz);

    compare(fooReference, namedFooReference, EQUAL);
    compare(barReference, fooReference, CONVERT_LEVEL_1);
    compare(bazReference, fooReference, INCOMPATIBLE_TYPES);
    compare(untypedPointer(), fooReference, CONVERT_LEVEL_5);
    compare(nilPointer(), fooReference, CONVERT_LEVEL_4);
    compare(pointerTo(foo), fooReference, INCOMPATIBLE_TYPES);
    compare(unknownType(), fooReference, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToFile() {
    Type fromFile = fileOf(IntrinsicType.INTEGER);
    Type toFile = fileOf(IntrinsicType.NATIVEINT);

    compare(fromFile, toFile, EQUAL);
    compare(untypedFile(), toFile, CONVERT_LEVEL_1);
    compare(fromFile, untypedFile(), CONVERT_LEVEL_1);
    compare(IntrinsicType.NATIVEINT, toFile, INCOMPATIBLE_TYPES);
    compare(fileOf(IntrinsicType.UNICODESTRING), toFile, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToPointer() {
    Type fooType = TypeMocker.struct("Foo", CLASS);
    Type barType = TypeMocker.struct("Bar", CLASS, fooType);
    Type bazType = TypeMocker.struct("Baz", RECORD);
    Type arrayType = dynamicArray(null, IntrinsicType.INTEGER);

    compare(pointerTo("Baz", fooType), pointerTo(fooType), EQUAL);
    compare(pointerTo(IntrinsicType.NATIVEINT), pointerTo(IntrinsicType.INTEGER), EQUAL);
    compare(pointerTo(barType), pointerTo(fooType), CONVERT_LEVEL_1);
    compare(IntrinsicType.UNICODESTRING, pointerTo(IntrinsicType.CHAR), CONVERT_LEVEL_2);
    compare(IntrinsicType.UNICODESTRING, pointerTo(IntrinsicType.ANSICHAR), CONVERT_LEVEL_3);
    compare(IntrinsicType.WIDECHAR, pointerTo(IntrinsicType.CHAR), CONVERT_LEVEL_1);
    compare(IntrinsicType.ANSICHAR, pointerTo(IntrinsicType.CHAR), CONVERT_LEVEL_1);
    compare(IntrinsicType.WIDECHAR, pointerTo(IntrinsicType.ANSICHAR), CONVERT_LEVEL_2);
    compare(IntrinsicType.ANSICHAR, pointerTo(IntrinsicType.ANSICHAR), CONVERT_LEVEL_2);
    compare(IntrinsicType.ANSICHAR, pointerTo(IntrinsicType.INTEGER), INCOMPATIBLE_TYPES);
    compare(fooType, untypedPointer(), CONVERT_LEVEL_5);
    compare(classOf(fooType), untypedPointer(), CONVERT_LEVEL_5);
    compare(IntrinsicType.INTEGER, pointerTo(IntrinsicType.UNICODESTRING), CONVERT_LEVEL_6);
    compare(untypedPointer(), pointerTo(IntrinsicType.CHAR), CONVERT_LEVEL_2);
    compare(untypedPointer(), pointerTo(IntrinsicType.INTEGER), CONVERT_LEVEL_1);
    compare(nilPointer(), pointerTo(IntrinsicType.CHAR), CONVERT_LEVEL_2);
    compare(nilPointer(), pointerTo(IntrinsicType.INTEGER), CONVERT_LEVEL_1);
    compare(pointerTo(IntrinsicType.CHAR), untypedPointer(), CONVERT_LEVEL_2);
    compare(pointerTo(IntrinsicType.INTEGER), untypedPointer(), CONVERT_LEVEL_1);
    compare(arrayType, pointerTo(IntrinsicType.INTEGER), CONVERT_LEVEL_3);
    compare(arrayType, untypedPointer(), CONVERT_LEVEL_4);
    compare(fooType, pointerTo(fooType), INCOMPATIBLE_TYPES);
    compare(
        pointerTo(IntrinsicType.INTEGER),
        pointerTo(IntrinsicType.UNICODESTRING),
        INCOMPATIBLE_TYPES);
    compare(IntrinsicType.UNICODESTRING, pointerTo(IntrinsicType.INTEGER), INCOMPATIBLE_TYPES);
    compare(IntrinsicType.ANSISTRING, pointerTo(IntrinsicType.CHAR), INCOMPATIBLE_TYPES);
    compare(IntrinsicType.ANSISTRING, pointerTo(IntrinsicType.ANSICHAR), INCOMPATIBLE_TYPES);
    compare(
        dynamicArray(null, IntrinsicType.STRING),
        pointerTo(IntrinsicType.INTEGER),
        INCOMPATIBLE_TYPES);
    compare(bazType, untypedPointer(), INCOMPATIBLE_TYPES);
    compare(unknownType(), untypedPointer(), INCOMPATIBLE_TYPES);
  }

  @Test
  void testToUntyped() {
    compare(untypedType(), untypedType(), EQUAL);
    compare(IntrinsicType.UNICODESTRING, untypedType(), CONVERT_LEVEL_7);
  }

  @Test
  void testToVariant() {
    Type interfaceType = TypeMocker.struct("Test", INTERFACE);

    compare(enumeration("Enum"), IntrinsicType.VARIANT, CONVERT_LEVEL_1);
    compare(dynamicArray("MyArray", unknownType()), IntrinsicType.VARIANT, CONVERT_LEVEL_1);
    compare(interfaceType, IntrinsicType.VARIANT, CONVERT_LEVEL_1);
    compare(IntrinsicType.INTEGER, IntrinsicType.VARIANT, CONVERT_LEVEL_1);
    compare(IntrinsicType.DOUBLE, IntrinsicType.VARIANT, CONVERT_LEVEL_1);
    compare(IntrinsicType.STRING, IntrinsicType.VARIANT, CONVERT_LEVEL_1);
    compare(IntrinsicType.VARIANT, IntrinsicType.OLEVARIANT, CONVERT_LEVEL_1);
    compare(IntrinsicType.OLEVARIANT, IntrinsicType.VARIANT, CONVERT_LEVEL_1);
    compare(unknownType(), IntrinsicType.VARIANT, CONVERT_LEVEL_8);
  }

  @Test
  void testToIntrinsicTypeArgument() {
    CollectionType fixedArray = fixedArray(null, IntrinsicType.INTEGER);
    CollectionType dynamicArray = dynamicArray(null, IntrinsicType.INTEGER);
    CollectionType openArray = openArray(null, IntrinsicType.INTEGER);
    CollectionType set = set(IntrinsicType.BYTE);
    StructType object = TypeMocker.struct("MyObject", OBJECT);
    EnumType enumeration = enumeration("MyEnum");

    compare(fixedArray, ANY_ARRAY, EQUAL);
    compare(dynamicArray, ANY_ARRAY, EQUAL);
    compare(openArray, ANY_ARRAY, EQUAL);

    compare(fixedArray, ANY_DYNAMIC_ARRAY, INCOMPATIBLE_TYPES);
    compare(dynamicArray, ANY_DYNAMIC_ARRAY, EQUAL);
    compare(openArray, ANY_DYNAMIC_ARRAY, INCOMPATIBLE_TYPES);

    compare(set, ANY_SET, EQUAL);

    compare(object, ANY_OBJECT, EQUAL);

    compare(IntrinsicType.INTEGER, ANY_ORDINAL, EQUAL);
    compare(IntrinsicType.BOOLEAN, ANY_ORDINAL, EQUAL);
    compare(enumeration, ANY_ORDINAL, EQUAL);
    compare(IntrinsicType.CHAR, ANY_ORDINAL, EQUAL);
    compare(IntrinsicType.DOUBLE, ANY_ORDINAL, INCOMPATIBLE_TYPES);
  }

  private static CollectionType openArray(String image, Type type) {
    return FACTORY.array(image, type, Set.of(ArrayOption.OPEN));
  }

  private static CollectionType fixedArray(String image, Type type) {
    return FACTORY.array(image, type, Set.of(ArrayOption.FIXED));
  }

  private static CollectionType dynamicArray(String image, Type type) {
    return FACTORY.array(image, type, Set.of(ArrayOption.DYNAMIC));
  }

  private static CollectionType arrayOfConst() {
    return FACTORY.array(null, voidType(), Set.of(ArrayOption.OPEN, ArrayOption.ARRAY_OF_CONST));
  }

  private static CollectionType openArray(String image, IntrinsicType intrinsic) {
    return openArray(image, toType(intrinsic));
  }

  private static CollectionType fixedArray(String image, IntrinsicType intrinsic) {
    return fixedArray(image, toType(intrinsic));
  }

  private static CollectionType dynamicArray(String image, IntrinsicType intrinsic) {
    return dynamicArray(image, toType(intrinsic));
  }

  private static ArrayConstructorType arrayConstructor(List<Object> types) {
    return FACTORY.arrayConstructor(
        types.stream().map(TypeComparerTest::toType).collect(Collectors.toList()));
  }

  private static CollectionType set(Type type) {
    return FACTORY.set(type);
  }

  private static CollectionType set(IntrinsicType intrinsic) {
    return set(toType(intrinsic));
  }

  private static CollectionType emptySet() {
    return FACTORY.emptySet();
  }

  private static TypeType typeType(String image, IntrinsicType intrinsic) {
    return FACTORY.typeType(image, toType(intrinsic));
  }

  private static TypeType typeType(String image, Type type) {
    return FACTORY.typeType(image, type);
  }

  private static EnumType enumeration(String image) {
    return FACTORY.enumeration(image, unknownScope());
  }

  private static SubrangeType subRange(String image, Type type) {
    return FACTORY.subRange(image, type);
  }

  private static SubrangeType subRange(String image, IntrinsicType intrinsic) {
    return subRange(image, toType(intrinsic));
  }

  private static PointerType pointerTo(Type type) {
    return FACTORY.pointerTo(null, type);
  }

  private static PointerType pointerTo(IntrinsicType intrinsic) {
    return pointerTo(toType(intrinsic));
  }

  private static PointerType pointerTo(String image, Type type) {
    return FACTORY.pointerTo(image, type);
  }

  private static PointerType untypedPointer() {
    return FACTORY.untypedPointer();
  }

  private static PointerType nilPointer() {
    return FACTORY.nilPointer();
  }

  private static FileType fileOf(Type type) {
    return FACTORY.fileOf(type);
  }

  private static FileType fileOf(IntrinsicType intrinsic) {
    return fileOf(toType(intrinsic));
  }

  private static FileType untypedFile() {
    return FACTORY.untypedFile();
  }

  private static ClassReferenceType classOf(Type type) {
    return FACTORY.classOf(null, type);
  }

  private static ClassReferenceType classOf(String image, Type type) {
    return FACTORY.classOf(image, type);
  }

  private static ProceduralType procedure(List<Type> parameterTypes, Type returnType) {
    return FACTORY.procedure(
        parameterTypes.stream().map(TypeMocker::parameter).collect(Collectors.toUnmodifiableList()),
        returnType);
  }

  private static ProceduralType anonymous(List<Type> parameterTypes, Type returnType) {
    return FACTORY.anonymous(
        parameterTypes.stream().map(TypeMocker::parameter).collect(Collectors.toUnmodifiableList()),
        returnType);
  }

  private static AnsiStringType ansiString(int codePage) {
    return FACTORY.ansiString(codePage);
  }
}
