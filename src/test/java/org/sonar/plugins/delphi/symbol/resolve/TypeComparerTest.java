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
import static org.sonar.plugins.delphi.type.DelphiArrayConstructorType.arrayConstructor;
import static org.sonar.plugins.delphi.type.DelphiArrayType.ArrayOption.ARRAY_OF_CONST;
import static org.sonar.plugins.delphi.type.DelphiArrayType.ArrayOption.OPEN;
import static org.sonar.plugins.delphi.type.DelphiArrayType.array;
import static org.sonar.plugins.delphi.type.DelphiArrayType.dynamicArray;
import static org.sonar.plugins.delphi.type.DelphiArrayType.fixedArray;
import static org.sonar.plugins.delphi.type.DelphiArrayType.openArray;
import static org.sonar.plugins.delphi.type.DelphiClassReferenceType.classOf;
import static org.sonar.plugins.delphi.type.DelphiEnumerationType.enumeration;
import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiPointerType.nilPointer;
import static org.sonar.plugins.delphi.type.DelphiPointerType.pointerTo;
import static org.sonar.plugins.delphi.type.DelphiPointerType.untypedPointer;
import static org.sonar.plugins.delphi.type.DelphiProceduralType.anonymous;
import static org.sonar.plugins.delphi.type.DelphiProceduralType.procedure;
import static org.sonar.plugins.delphi.type.DelphiSetType.emptySet;
import static org.sonar.plugins.delphi.type.DelphiSetType.set;
import static org.sonar.plugins.delphi.type.DelphiSubrangeType.subRange;
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
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BYTEBOOL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.WORDBOOL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.COMP;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.CURRENCY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.DOUBLE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.SINGLE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.BYTE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.LONGINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.NATIVEINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.SHORTINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.SMALLINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicPointer.PANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicPointer.PCHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicPointer.POINTER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSISTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.SHORTSTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.STRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDECHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.OLE_VARIANT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.VARIANT;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.type.DelphiArrayType;
import org.sonar.plugins.delphi.type.DelphiEnumerationType;
import org.sonar.plugins.delphi.type.DelphiFileType;
import org.sonar.plugins.delphi.type.DelphiSetType;
import org.sonar.plugins.delphi.type.DelphiSubrangeType;
import org.sonar.plugins.delphi.type.DelphiTypeType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.Type.SubrangeType;
import org.sonar.plugins.delphi.utils.types.TypeMocker;

class TypeComparerTest {
  private static void compare(Type from, Type to, EqualityType equality) {
    assertThat(TypeComparer.compare(from, to)).isEqualTo(equality);
  }

  @Test
  void testExactTypes() {
    compare(UNICODESTRING.type, UNICODESTRING.type, EXACT);
  }

  @Test
  void testToInteger() {
    compare(SMALLINT.type, INTEGER.type, CONVERT_LEVEL_1);
    compare(INTEGER.type, SMALLINT.type, CONVERT_LEVEL_3);
    compare(CURRENCY.type, INTEGER.type, CONVERT_LEVEL_2);
    compare(VARIANT.type, INTEGER.type, CONVERT_LEVEL_7);
    compare(subRange("0..5", SHORTINT.type), SHORTINT.type, EQUAL);
    compare(subRange("-100..100", BYTE.type), SHORTINT.type, CONVERT_LEVEL_1);
    compare(UNICODESTRING.type, INTEGER.type, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToDecimal() {
    compare(INTEGER.type, SINGLE.type, CONVERT_LEVEL_3);
    compare(INTEGER.type, DOUBLE.type, CONVERT_LEVEL_4);
    compare(DOUBLE.type, SINGLE.type, CONVERT_LEVEL_2);
    compare(SINGLE.type, DOUBLE.type, CONVERT_LEVEL_1);
    compare(DOUBLE.type, COMP.type, EQUAL);
    compare(UNICODESTRING.type, DOUBLE.type, INCOMPATIBLE_TYPES);
  }

  @Test
  void testStringToString() {
    compare(WIDESTRING.type, UNICODESTRING.type, CONVERT_LEVEL_1);
    compare(WIDESTRING.type, ANSISTRING.type, CONVERT_LEVEL_2);
    compare(WIDESTRING.type, SHORTSTRING.type, CONVERT_LEVEL_3);
    compare(WIDESTRING.type, CHAR.type, INCOMPATIBLE_TYPES);

    compare(UNICODESTRING.type, WIDESTRING.type, CONVERT_LEVEL_1);
    compare(UNICODESTRING.type, ANSISTRING.type, CONVERT_LEVEL_2);
    compare(UNICODESTRING.type, SHORTSTRING.type, CONVERT_LEVEL_3);
    compare(UNICODESTRING.type, CHAR.type, INCOMPATIBLE_TYPES);

    compare(SHORTSTRING.type, ANSISTRING.type, CONVERT_LEVEL_1);
    compare(SHORTSTRING.type, UNICODESTRING.type, CONVERT_LEVEL_2);
    compare(SHORTSTRING.type, WIDESTRING.type, CONVERT_LEVEL_3);
    compare(SHORTSTRING.type, CHAR.type, INCOMPATIBLE_TYPES);

    compare(ANSISTRING.type, UNICODESTRING.type, CONVERT_LEVEL_1);
    compare(ANSISTRING.type, WIDESTRING.type, CONVERT_LEVEL_2);
    compare(ANSISTRING.type, SHORTSTRING.type, CONVERT_LEVEL_3);
    compare(ANSISTRING.type, CHAR.type, INCOMPATIBLE_TYPES);

    compare(DelphiTypeType.create("Test", UNICODESTRING.type), UNICODESTRING.type, EQUAL);

    assertThatThrownBy(() -> TypeComparer.compareStringToString(unknownType(), UNICODESTRING.type))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testArrayToString() {
    Type ansiCharOpenArray = openArray(null, ANSICHAR.type);
    Type wideCharOpenArray = openArray(null, WIDECHAR.type);
    Type ansiCharFixedArray = fixedArray(null, ANSICHAR.type);
    Type wideCharFixedArray = fixedArray(null, WIDECHAR.type);

    compare(ansiCharOpenArray, ANSISTRING.type, CONVERT_LEVEL_2);
    compare(ansiCharOpenArray, WIDESTRING.type, CONVERT_LEVEL_3);
    compare(ansiCharOpenArray, UNICODESTRING.type, CONVERT_LEVEL_4);
    compare(wideCharOpenArray, UNICODESTRING.type, CONVERT_LEVEL_2);
    compare(wideCharOpenArray, WIDESTRING.type, CONVERT_LEVEL_3);
    compare(wideCharOpenArray, ANSISTRING.type, CONVERT_LEVEL_4);
    compare(openArray(null, unknownType()), ANSISTRING.type, INCOMPATIBLE_TYPES);

    compare(ansiCharFixedArray, ANSISTRING.type, CONVERT_LEVEL_2);
    compare(ansiCharFixedArray, WIDESTRING.type, CONVERT_LEVEL_3);
    compare(ansiCharFixedArray, UNICODESTRING.type, CONVERT_LEVEL_4);
    compare(wideCharFixedArray, UNICODESTRING.type, CONVERT_LEVEL_2);
    compare(wideCharFixedArray, WIDESTRING.type, CONVERT_LEVEL_3);
    compare(wideCharFixedArray, ANSISTRING.type, CONVERT_LEVEL_4);
    compare(fixedArray(null, unknownType()), ANSISTRING.type, INCOMPATIBLE_TYPES);

    compare(dynamicArray(null, ANSICHAR.type), ANSISTRING.type, INCOMPATIBLE_TYPES);
  }

  @Test
  void testCharToText() {
    compare(ANSICHAR.type, DelphiTypeType.create("_AnsiChar", ANSICHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, SHORTSTRING.type, CONVERT_LEVEL_2);
    compare(ANSICHAR.type, ANSISTRING.type, CONVERT_LEVEL_3);
    compare(ANSICHAR.type, UNICODESTRING.type, CONVERT_LEVEL_4);
    compare(ANSICHAR.type, UNICODESTRING.type, CONVERT_LEVEL_4);
    compare(ANSICHAR.type, WIDESTRING.type, CONVERT_LEVEL_5);
    assertThat(TypeComparer.compareAnsiCharToText(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    compare(CHAR.type, DelphiTypeType.create("_WideChar", WIDECHAR.type), CONVERT_LEVEL_1);
    compare(CHAR.type, UNICODESTRING.type, CONVERT_LEVEL_2);
    compare(CHAR.type, WIDESTRING.type, CONVERT_LEVEL_3);
    compare(CHAR.type, ANSICHAR.type, CONVERT_LEVEL_3);
    compare(CHAR.type, ANSISTRING.type, CONVERT_LEVEL_4);
    compare(CHAR.type, SHORTSTRING.type, CONVERT_LEVEL_5);
    assertThat(TypeComparer.compareWideCharToText(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    assertThatThrownBy(() -> TypeComparer.compareCharToText(unknownType(), unknownType()))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void testToBoolean() {
    compare(BOOLEAN.type, BYTEBOOL.type, CONVERT_LEVEL_1);
    compare(BOOLEAN.type, WORDBOOL.type, CONVERT_LEVEL_1);
    compare(WORDBOOL.type, BOOLEAN.type, CONVERT_LEVEL_3);
    compare(UNICODESTRING.type, BOOLEAN.type, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToEnum() {
    EnumType enumType = enumeration("Enum1", unknownScope());
    EnumType enumType2 = enumeration("Enum2", unknownScope());
    SubrangeType subrangeType = DelphiSubrangeType.subRange("Subrange", enumType);

    compare(subrangeType, enumType, CONVERT_LEVEL_1);
    compare(subrangeType, enumType2, INCOMPATIBLE_TYPES);
    compare(enumType, enumType2, INCOMPATIBLE_TYPES);
    compare(VARIANT.type, enumType, CONVERT_LEVEL_1);
    compare(INTEGER.type, enumType, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToSubrange() {
    SubrangeType subrangeOfShortInt = subRange("1..100", SHORTINT.type);
    SubrangeType subrangeOfInteger = subRange("5..High(Integer)", INTEGER.type);
    SubrangeType subrangeOfEnum = subRange("5..6", enumeration("Enum", unknownScope()));

    compare(subrangeOfShortInt, subrangeOfInteger, CONVERT_LEVEL_1);
    compare(subrangeOfInteger, subrangeOfShortInt, CONVERT_LEVEL_3);
    compare(subrangeOfShortInt, subrangeOfEnum, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToArray() {
    CollectionType fromOpenArray = openArray("Open", INTEGER.type);
    CollectionType fromDynamicArray = dynamicArray("Dynamic", INTEGER.type);
    CollectionType fromFixedArray = fixedArray("Fixed", INTEGER.type);
    CollectionType fromIncompatibleOpenArray = openArray("OpenString", UNICODESTRING.type);
    CollectionType fromIncompatibleDynamicArray = dynamicArray("DynamicString", UNICODESTRING.type);
    CollectionType fromIncompatibleFixedArray = fixedArray("FixedString", UNICODESTRING.type);
    CollectionType toDynamicArray = dynamicArray(null, INTEGER.type);
    CollectionType toFixedArray = fixedArray(null, INTEGER.type);
    CollectionType toOpenArray = openArray(null, INTEGER.type);
    CollectionType toSimilarOpenArray = openArray(null, NATIVEINT.type);

    compare(INTEGER.type, toOpenArray, CONVERT_LEVEL_3);
    compare(fromDynamicArray, toDynamicArray, EQUAL);
    compare(fromFixedArray, toDynamicArray, CONVERT_LEVEL_2);
    compare(dynamicArray(null, UNICODESTRING.type), toDynamicArray, INCOMPATIBLE_TYPES);

    compare(fromDynamicArray, toOpenArray, CONVERT_LEVEL_1);
    compare(fromDynamicArray, toSimilarOpenArray, CONVERT_LEVEL_2);
    compare(fromIncompatibleDynamicArray, toOpenArray, INCOMPATIBLE_TYPES);
    compare(fromOpenArray, toOpenArray, EXACT);
    compare(openArray(null, LONGINT.type), toOpenArray, EQUAL);
    compare(fromFixedArray, toOpenArray, EQUAL);
    compare(fromIncompatibleFixedArray, toOpenArray, INCOMPATIBLE_TYPES);
    compare(openArray(null, unknownType()), toOpenArray, INCOMPATIBLE_TYPES);
    compare(openArray(null, ANSICHAR.type), openArray(null, CHAR.type), CONVERT_LEVEL_5);

    compare(fromOpenArray, toFixedArray, EQUAL);
    compare(fromIncompatibleOpenArray, toFixedArray, INCOMPATIBLE_TYPES);
    compare(fromDynamicArray, toFixedArray, INCOMPATIBLE_TYPES);

    compare(pointerTo(INTEGER.type), toOpenArray, CONVERT_LEVEL_3);
    compare(nilPointer(), toDynamicArray, CONVERT_LEVEL_3);
    compare(untypedPointer(), toDynamicArray, CONVERT_LEVEL_3);
    compare(pointerTo(UNICODESTRING.type), toOpenArray, INCOMPATIBLE_TYPES);
    compare(pointerTo(unknownType()), toDynamicArray, INCOMPATIBLE_TYPES);

    compare(ANSICHAR.type, dynamicArray(null, ANSICHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, toDynamicArray, INCOMPATIBLE_TYPES);

    compare(VARIANT.type, toDynamicArray, CONVERT_LEVEL_1);
    compare(VARIANT.type, toOpenArray, CONVERT_LEVEL_8);

    compare(unknownType(), toOpenArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toDynamicArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toFixedArray, INCOMPATIBLE_TYPES);
  }

  @Test
  void testArrayConstructorToArray() {
    CollectionType toDynamicArray = dynamicArray(null, INTEGER.type);
    CollectionType toDynamicPointerArray = dynamicArray(null, POINTER.type);
    CollectionType toIncompatibleDynamicArray =
        dynamicArray(null, TypeMocker.struct("Test", CLASS));
    CollectionType toFixedArray = fixedArray(null, INTEGER.type);
    CollectionType toOpenArray = openArray(null, INTEGER.type);
    CollectionType toArrayOfConst = array(null, voidType(), Set.of(OPEN, ARRAY_OF_CONST));

    ArrayConstructorType emptyConstructor = arrayConstructor(emptyList());
    ArrayConstructorType byteConstructor = arrayConstructor(List.of(BYTE.type));
    ArrayConstructorType integerConstructor = arrayConstructor(List.of(INTEGER.type));
    ArrayConstructorType stringConstructor = arrayConstructor(List.of(UNICODESTRING.type));
    ArrayConstructorType variantConstructor = arrayConstructor(List.of(VARIANT.type));
    ArrayConstructorType heterogeneousConstructor =
        arrayConstructor(List.of(INTEGER.type, UNICODESTRING.type, BOOLEAN.type));

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
    CollectionType byteSet = set(BYTE.type);
    CollectionType classSet = set(TypeMocker.struct("Foo", CLASS));

    ArrayConstructorType emptyConstructor = arrayConstructor(emptyList());
    ArrayConstructorType byteConstructor = arrayConstructor(List.of(BYTE.type));
    ArrayConstructorType integerConstructor = arrayConstructor(List.of(INTEGER.type));
    ArrayConstructorType stringConstructor = arrayConstructor(List.of(UNICODESTRING.type));
    ArrayConstructorType variantConstructor = arrayConstructor(List.of(VARIANT.type));

    compare(emptyConstructor, byteSet, CONVERT_LEVEL_2);
    compare(byteConstructor, byteSet, CONVERT_LEVEL_2);
    compare(integerConstructor, byteSet, CONVERT_LEVEL_5);
    compare(variantConstructor, byteSet, CONVERT_LEVEL_6);
    compare(variantConstructor, classSet, CONVERT_LEVEL_7);
    compare(stringConstructor, byteSet, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToSet() {
    // NOTE: Sets can't actually have such large ordinal element types (and certainly not strings)
    // This is just for testing convenience.
    CollectionType integerSet = DelphiSetType.set(INTEGER.type);
    CollectionType longIntSet = DelphiSetType.set(LONGINT.type);
    CollectionType stringSet = DelphiSetType.set(UNICODESTRING.type);

    compare(emptySet(), integerSet, CONVERT_LEVEL_1);
    compare(integerSet, emptySet(), CONVERT_LEVEL_1);
    compare(integerSet, longIntSet, EQUAL);
    compare(integerSet, stringSet, INCOMPATIBLE_TYPES);
    compare(unknownType(), integerSet, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToProceduralType() {
    List<Type> parameters = Collections.singletonList(INTEGER.type);
    List<Type> similarParameters = Collections.singletonList(LONGINT.type);
    Type returnType = UNICODESTRING.type;

    ProceduralType fromProcedure = procedure(parameters, returnType);
    ProceduralType similarFromProcedure = procedure(similarParameters, returnType);
    ProceduralType incompatibleReturnTypeProcedure = procedure(parameters, INTEGER.type);
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

    compare(fromClass, toClass, CONVERT_LEVEL_1);
    compare(untypedPointer(), toClass, CONVERT_LEVEL_4);
    compare(nilPointer(), toClass, CONVERT_LEVEL_3);
    compare(classOf(fromInterface), toGUID, CONVERT_LEVEL_5);
    compare(VARIANT.type, toClass, CONVERT_LEVEL_8);
    compare(untypedPointer(), toRecord, INCOMPATIBLE_TYPES);
    compare(nilPointer(), toRecord, INCOMPATIBLE_TYPES);
    compare(pointerTo(toClass), toClass, INCOMPATIBLE_TYPES);
    compare(fromInterface, toGUID, INCOMPATIBLE_TYPES);
    compare(fromInterface, toRecord, INCOMPATIBLE_TYPES);
    compare(classOf(fromInterface), toRecord, INCOMPATIBLE_TYPES);
    compare(unknownType(), toClass, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToClassReference() {
    var toObject = TypeMocker.struct("Foo", CLASS);
    var fromObject = TypeMocker.struct("Bar", CLASS, toObject);
    var otherObject = TypeMocker.struct("Baz", CLASS);

    Type fromReference = classOf(fromObject);
    Type toReference = classOf(toObject);
    Type otherReference = classOf(otherObject);

    compare(fromReference, toReference, CONVERT_LEVEL_1);
    compare(otherReference, toReference, INCOMPATIBLE_TYPES);
    compare(untypedPointer(), toReference, CONVERT_LEVEL_5);
    compare(nilPointer(), toReference, CONVERT_LEVEL_4);
    compare(pointerTo(toObject), toReference, INCOMPATIBLE_TYPES);
    compare(unknownType(), toReference, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToFile() {
    Type fromFile = DelphiFileType.fileOf(INTEGER.type);
    Type toFile = DelphiFileType.fileOf(LONGINT.type);

    compare(fromFile, toFile, EQUAL);
    compare(untypedFile(), toFile, CONVERT_LEVEL_1);
    compare(fromFile, untypedFile(), CONVERT_LEVEL_1);
    compare(LONGINT.type, toFile, INCOMPATIBLE_TYPES);
    compare(DelphiFileType.fileOf(UNICODESTRING.type), toFile, INCOMPATIBLE_TYPES);
  }

  @Test
  void testToPointer() {
    var fooType = TypeMocker.struct("Foo", CLASS);
    var barType = TypeMocker.struct("Bar", CLASS, fooType);
    var arrayType = dynamicArray(null, INTEGER.type);

    compare(pointerTo(LONGINT.type), pointerTo(INTEGER.type), EQUAL);
    compare(pointerTo(barType), pointerTo(fooType), CONVERT_LEVEL_1);
    compare(UNICODESTRING.type, pointerTo(CHAR.type), CONVERT_LEVEL_2);
    compare(UNICODESTRING.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_3);
    compare(WIDECHAR.type, pointerTo(CHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, pointerTo(CHAR.type), CONVERT_LEVEL_1);
    compare(WIDECHAR.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_2);
    compare(ANSICHAR.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_2);
    compare(fooType, untypedPointer(), CONVERT_LEVEL_5);
    compare(INTEGER.type, pointerTo(UNICODESTRING.type), CONVERT_LEVEL_6);
    compare(untypedPointer(), pointerTo(CHAR.type), CONVERT_LEVEL_2);
    compare(untypedPointer(), pointerTo(INTEGER.type), CONVERT_LEVEL_1);
    compare(pointerTo(CHAR.type), untypedPointer(), CONVERT_LEVEL_2);
    compare(pointerTo(INTEGER.type), untypedPointer(), CONVERT_LEVEL_1);
    compare(arrayType, pointerTo(INTEGER.type), CONVERT_LEVEL_3);
    compare(arrayType, untypedPointer(), CONVERT_LEVEL_4);
    compare(PANSICHAR.type, ANSISTRING.type, CONVERT_LEVEL_3);
    compare(PCHAR.type, UNICODESTRING.type, CONVERT_LEVEL_3);
    compare(PANSICHAR.type, UNICODESTRING.type, CONVERT_LEVEL_4);
    compare(PCHAR.type, ANSISTRING.type, CONVERT_LEVEL_4);
    compare(pointerTo(INTEGER.type), UNICODESTRING.type, INCOMPATIBLE_TYPES);
    compare(fooType, pointerTo(fooType), INCOMPATIBLE_TYPES);
    compare(pointerTo(INTEGER.type), pointerTo(UNICODESTRING.type), INCOMPATIBLE_TYPES);
    compare(UNICODESTRING.type, pointerTo(INTEGER.type), INCOMPATIBLE_TYPES);
    compare(dynamicArray(null, STRING.type), pointerTo(INTEGER.type), INCOMPATIBLE_TYPES);
    compare(unknownType(), untypedPointer(), INCOMPATIBLE_TYPES);
  }

  @Test
  void testToUntyped() {
    compare(untypedType(), untypedType(), EQUAL);
    compare(UNICODESTRING.type, untypedType(), CONVERT_LEVEL_7);
  }

  @Test
  void testToVariant() {
    Type interfaceType = TypeMocker.struct("Test", INTERFACE);

    compare(enumeration("Enum", unknownScope()), VARIANT.type, CONVERT_LEVEL_1);
    compare(dynamicArray("MyArray", unknownType()), VARIANT.type, CONVERT_LEVEL_1);
    compare(interfaceType, VARIANT.type, CONVERT_LEVEL_1);
    compare(INTEGER.type, VARIANT.type, CONVERT_LEVEL_1);
    compare(DOUBLE.type, VARIANT.type, CONVERT_LEVEL_1);
    compare(STRING.type, VARIANT.type, CONVERT_LEVEL_1);
    compare(OLE_VARIANT.type, VARIANT.type, CONVERT_LEVEL_1);
    compare(unknownType(), VARIANT.type, CONVERT_LEVEL_8);
  }

  @Test
  void testToIntrinsicTypeArgument() {
    CollectionType fixedArray = DelphiArrayType.fixedArray(null, INTEGER.type);
    CollectionType dynamicArray = DelphiArrayType.dynamicArray(null, INTEGER.type);
    CollectionType openArray = DelphiArrayType.openArray(null, INTEGER.type);
    CollectionType set = DelphiSetType.set(BYTE.type);
    StructType object = TypeMocker.struct("MyObject", OBJECT);
    EnumType enumeration = DelphiEnumerationType.enumeration("MyEnum", unknownScope());

    compare(fixedArray, ANY_ARRAY, EQUAL);
    compare(dynamicArray, ANY_ARRAY, EQUAL);
    compare(openArray, ANY_ARRAY, EQUAL);

    compare(fixedArray, ANY_DYNAMIC_ARRAY, INCOMPATIBLE_TYPES);
    compare(dynamicArray, ANY_DYNAMIC_ARRAY, EQUAL);
    compare(openArray, ANY_DYNAMIC_ARRAY, INCOMPATIBLE_TYPES);

    compare(set, ANY_SET, EQUAL);

    compare(object, ANY_OBJECT, EQUAL);

    compare(INTEGER.type, ANY_ORDINAL, EQUAL);
    compare(BOOLEAN.type, ANY_ORDINAL, EQUAL);
    compare(enumeration, ANY_ORDINAL, EQUAL);
    compare(CHAR.type, ANY_ORDINAL, EQUAL);
    compare(DOUBLE.type, ANY_ORDINAL, INCOMPATIBLE_TYPES);
  }
}
