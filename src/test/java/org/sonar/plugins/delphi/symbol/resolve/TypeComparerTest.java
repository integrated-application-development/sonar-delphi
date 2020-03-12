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
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.CONVERT_OPERATOR;
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
import static org.sonar.plugins.delphi.type.DelphiEnumerationType.enumeration;
import static org.sonar.plugins.delphi.type.DelphiEnumerationType.subRange;
import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiPointerType.nilPointer;
import static org.sonar.plugins.delphi.type.DelphiPointerType.pointerTo;
import static org.sonar.plugins.delphi.type.DelphiPointerType.untypedPointer;
import static org.sonar.plugins.delphi.type.DelphiProceduralType.anonymous;
import static org.sonar.plugins.delphi.type.DelphiProceduralType.procedure;
import static org.sonar.plugins.delphi.type.DelphiSetType.emptySet;
import static org.sonar.plugins.delphi.type.DelphiSetType.set;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.DelphiType.untypedType;
import static org.sonar.plugins.delphi.type.DelphiType.voidType;
import static org.sonar.plugins.delphi.type.StructKind.CLASS;
import static org.sonar.plugins.delphi.type.StructKind.INTERFACE;
import static org.sonar.plugins.delphi.type.StructKind.OBJECT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_DYNAMIC_ARRAY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_OBJECT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_ORDINAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher.ANY_SET;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BYTEBOOL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.WORDBOOL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.CURRENCY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.DOUBLE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.REAL;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.SINGLE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.BYTE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.INTEGER;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.LONGINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicInteger.SMALLINT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSISTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.SHORTSTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDECHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.OLE_VARIANT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicVariant.VARIANT;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.sonar.plugins.delphi.type.DelphiArrayType;
import org.sonar.plugins.delphi.type.DelphiClassReferenceType;
import org.sonar.plugins.delphi.type.DelphiEnumerationType;
import org.sonar.plugins.delphi.type.DelphiFileType;
import org.sonar.plugins.delphi.type.DelphiSetType;
import org.sonar.plugins.delphi.type.DelphiTypeType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.utils.types.TypeMocker;

public class TypeComparerTest {
  private static void compare(Type from, Type to, EqualityType equality) {
    assertThat(TypeComparer.compare(from, to)).isEqualTo(equality);
  }

  @Test
  public void testExactTypes() {
    compare(UNICODESTRING.type, UNICODESTRING.type, EXACT);
  }

  @Test
  public void testToInteger() {
    compare(SMALLINT.type, INTEGER.type, CONVERT_LEVEL_1);
    compare(INTEGER.type, SMALLINT.type, CONVERT_LEVEL_3);
    compare(CURRENCY.type, INTEGER.type, CONVERT_LEVEL_2);
    compare(VARIANT.type, INTEGER.type, CONVERT_LEVEL_6);
    compare(UNICODESTRING.type, INTEGER.type, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToDecimal() {
    compare(INTEGER.type, SINGLE.type, CONVERT_LEVEL_3);
    compare(INTEGER.type, DOUBLE.type, CONVERT_LEVEL_4);
    compare(DOUBLE.type, SINGLE.type, CONVERT_LEVEL_2);
    compare(SINGLE.type, DOUBLE.type, CONVERT_LEVEL_1);
    compare(DOUBLE.type, REAL.type, EQUAL);
    compare(UNICODESTRING.type, DOUBLE.type, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testStringToText() {
    compare(WIDESTRING.type, UNICODESTRING.type, CONVERT_LEVEL_1);
    compare(WIDESTRING.type, ANSISTRING.type, CONVERT_LEVEL_2);
    compare(WIDESTRING.type, CHAR.type, CONVERT_LEVEL_3);
    compare(WIDESTRING.type, unknownType(), INCOMPATIBLE_TYPES);

    compare(UNICODESTRING.type, WIDESTRING.type, CONVERT_LEVEL_1);
    compare(UNICODESTRING.type, ANSISTRING.type, CONVERT_LEVEL_2);
    compare(UNICODESTRING.type, CHAR.type, CONVERT_LEVEL_3);
    compare(UNICODESTRING.type, unknownType(), INCOMPATIBLE_TYPES);

    compare(SHORTSTRING.type, ANSISTRING.type, CONVERT_LEVEL_1);
    compare(SHORTSTRING.type, UNICODESTRING.type, CONVERT_LEVEL_2);
    compare(SHORTSTRING.type, CHAR.type, CONVERT_LEVEL_3);
    compare(SHORTSTRING.type, unknownType(), INCOMPATIBLE_TYPES);

    compare(ANSISTRING.type, CHAR.type, CONVERT_LEVEL_1);
    compare(ANSISTRING.type, unknownType(), INCOMPATIBLE_TYPES);

    compare(DelphiTypeType.create("Test", UNICODESTRING.type), UNICODESTRING.type, EQUAL);

    assertThatThrownBy(() -> TypeComparer.compareStringToText(unknownType(), UNICODESTRING.type))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  public void testCharToText() {
    compare(ANSICHAR.type, DelphiTypeType.create("_AnsiChar", ANSICHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, SHORTSTRING.type, CONVERT_LEVEL_2);
    compare(ANSICHAR.type, ANSISTRING.type, CONVERT_LEVEL_3);
    compare(ANSICHAR.type, UNICODESTRING.type, CONVERT_LEVEL_4);
    compare(ANSICHAR.type, UNICODESTRING.type, CONVERT_LEVEL_4);
    compare(ANSICHAR.type, WIDESTRING.type, CONVERT_LEVEL_5);
    assertThat(TypeComparer.compareAnsiCharToText(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    compare(CHAR.type, DelphiTypeType.create("_WideChar", WIDECHAR.type), CONVERT_LEVEL_1);
    compare(CHAR.type, UNICODESTRING.type, CONVERT_LEVEL_2);
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
  public void testToBoolean() {
    compare(BOOLEAN.type, BYTEBOOL.type, CONVERT_LEVEL_1);
    compare(BOOLEAN.type, WORDBOOL.type, CONVERT_LEVEL_1);
    compare(WORDBOOL.type, BOOLEAN.type, CONVERT_LEVEL_3);
    compare(UNICODESTRING.type, BOOLEAN.type, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToEnum() {
    EnumType enumType = enumeration("Enum1", unknownScope());
    EnumType enumType2 = enumeration("Enum2", unknownScope());
    EnumType subRange1 = subRange("1..2", enumType);
    EnumType subRange2 = subRange("3..4", enumType);
    EnumType subRange3 = subRange("5..6", enumType2);

    compare(enumType, enumType2, INCOMPATIBLE_TYPES);
    compare(subRange1, subRange2, CONVERT_LEVEL_1);
    compare(subRange1, subRange3, INCOMPATIBLE_TYPES);
    compare(VARIANT.type, enumType, CONVERT_LEVEL_1);
    compare(INTEGER.type, enumType, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToArray() {
    CollectionType fromOpenArray = openArray("Open", INTEGER.type);
    CollectionType fromDynamicArray = dynamicArray("Dynamic", INTEGER.type);
    CollectionType fromFixedArray = fixedArray("Fixed", INTEGER.type);
    CollectionType fromIncompatibleOpenArray = openArray("OpenString", UNICODESTRING.type);
    CollectionType fromIncompatibleDynamicArray = dynamicArray("DynamicString", UNICODESTRING.type);
    CollectionType fromIncompatibleFixedArray = fixedArray("FixedString", UNICODESTRING.type);
    CollectionType toDynamicArray = dynamicArray(null, INTEGER.type);
    CollectionType toFixedArray = fixedArray(null, INTEGER.type);
    CollectionType toOpenArray = openArray(null, INTEGER.type);

    compare(INTEGER.type, toOpenArray, CONVERT_LEVEL_3);
    compare(fromDynamicArray, toDynamicArray, EQUAL);
    compare(fromFixedArray, toDynamicArray, CONVERT_LEVEL_2);
    compare(dynamicArray(null, UNICODESTRING.type), toDynamicArray, INCOMPATIBLE_TYPES);

    compare(fromDynamicArray, toOpenArray, CONVERT_LEVEL_2);
    compare(fromIncompatibleDynamicArray, toOpenArray, INCOMPATIBLE_TYPES);
    compare(fromOpenArray, toOpenArray, EXACT);
    compare(openArray(null, LONGINT.type), toOpenArray, EQUAL);
    compare(fromFixedArray, toOpenArray, EQUAL);
    compare(fromIncompatibleFixedArray, toOpenArray, INCOMPATIBLE_TYPES);
    compare(openArray(null, unknownType()), toOpenArray, INCOMPATIBLE_TYPES);

    compare(fromOpenArray, toFixedArray, EQUAL);
    compare(fromIncompatibleOpenArray, toFixedArray, INCOMPATIBLE_TYPES);
    compare(fromDynamicArray, toFixedArray, INCOMPATIBLE_TYPES);

    compare(pointerTo(INTEGER.type), toOpenArray, CONVERT_LEVEL_1);
    compare(nilPointer(), toDynamicArray, CONVERT_LEVEL_1);
    compare(untypedPointer(), toDynamicArray, CONVERT_LEVEL_1);
    compare(pointerTo(UNICODESTRING.type), toOpenArray, INCOMPATIBLE_TYPES);
    compare(pointerTo(unknownType()), toDynamicArray, INCOMPATIBLE_TYPES);

    compare(ANSICHAR.type, dynamicArray(null, ANSICHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, toDynamicArray, INCOMPATIBLE_TYPES);

    compare(VARIANT.type, toDynamicArray, CONVERT_LEVEL_1);
    compare(VARIANT.type, toOpenArray, CONVERT_OPERATOR);

    compare(unknownType(), toOpenArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toDynamicArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toFixedArray, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testArrayConstructorToArray() {
    CollectionType toDynamicArray = dynamicArray(null, INTEGER.type);
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

    compare(emptyConstructor, toDynamicArray, CONVERT_LEVEL_2);
    compare(emptyConstructor, toOpenArray, CONVERT_LEVEL_1);
    compare(emptyConstructor, toFixedArray, INCOMPATIBLE_TYPES);

    compare(byteConstructor, toDynamicArray, CONVERT_LEVEL_3);
    compare(byteConstructor, toOpenArray, CONVERT_LEVEL_2);

    compare(integerConstructor, toDynamicArray, CONVERT_LEVEL_2);
    compare(integerConstructor, toOpenArray, CONVERT_LEVEL_1);

    compare(stringConstructor, toDynamicArray, INCOMPATIBLE_TYPES);
    compare(stringConstructor, toOpenArray, INCOMPATIBLE_TYPES);

    compare(variantConstructor, toDynamicArray, CONVERT_LEVEL_5);
    compare(variantConstructor, toIncompatibleDynamicArray, CONVERT_LEVEL_6);
    compare(variantConstructor, toOpenArray, CONVERT_LEVEL_6);

    compare(byteConstructor, toArrayOfConst, EQUAL);
    compare(integerConstructor, toArrayOfConst, EQUAL);
    compare(heterogeneousConstructor, toArrayOfConst, EQUAL);
  }

  @Test
  public void testArrayConstructorToSet() {
    CollectionType toSet = set(INTEGER.type);

    ArrayConstructorType emptyConstructor = arrayConstructor(emptyList());
    ArrayConstructorType byteConstructor = arrayConstructor(List.of(BYTE.type));
    ArrayConstructorType integerConstructor = arrayConstructor(List.of(INTEGER.type));
    ArrayConstructorType stringConstructor = arrayConstructor(List.of(UNICODESTRING.type));
    ArrayConstructorType variantConstructor = arrayConstructor(List.of(VARIANT.type));
    ArrayConstructorType heterogeneousConstructor =
        arrayConstructor(List.of(INTEGER.type, UNICODESTRING.type, BOOLEAN.type));

    compare(emptyConstructor, toSet, CONVERT_LEVEL_1);
    compare(byteConstructor, toSet, CONVERT_LEVEL_1);
    compare(integerConstructor, toSet, CONVERT_LEVEL_1);
    compare(stringConstructor, toSet, CONVERT_LEVEL_1);
    compare(variantConstructor, toSet, CONVERT_LEVEL_1);
    compare(heterogeneousConstructor, toSet, CONVERT_LEVEL_1);
  }

  @Test
  public void testToSet() {
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
  public void testToProceduralType() {
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

    compare(nilPointer(), toProcedure, CONVERT_LEVEL_1);
    compare(untypedPointer(), toProcedure, CONVERT_LEVEL_1);
    compare(pointerTo(returnType), toProcedure, INCOMPATIBLE_TYPES);

    compare(unknownType(), toProcedure, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToObject() {
    Type toObject = TypeMocker.struct("To", CLASS);
    Type fromObject = TypeMocker.struct("From", CLASS, toObject);

    compare(fromObject, toObject, CONVERT_LEVEL_3);
    compare(untypedPointer(), toObject, CONVERT_LEVEL_2);
    compare(nilPointer(), toObject, CONVERT_LEVEL_1);
    compare(pointerTo(toObject), toObject, INCOMPATIBLE_TYPES);
    compare(VARIANT.type, toObject, CONVERT_OPERATOR);
    compare(unknownType(), toObject, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToClassReference() {
    var toObject = TypeMocker.struct("Foo", CLASS);
    var fromObject = TypeMocker.struct("Bar", CLASS, toObject);
    var otherObject = TypeMocker.struct("Baz", CLASS);

    Type fromReference = DelphiClassReferenceType.classOf(fromObject);
    Type toReference = DelphiClassReferenceType.classOf(toObject);
    Type otherReference = DelphiClassReferenceType.classOf(otherObject);

    compare(fromReference, toReference, CONVERT_LEVEL_1);
    compare(otherReference, toReference, INCOMPATIBLE_TYPES);
    compare(untypedPointer(), toReference, CONVERT_LEVEL_2);
    compare(nilPointer(), toReference, CONVERT_LEVEL_1);
    compare(pointerTo(toObject), toReference, INCOMPATIBLE_TYPES);
    compare(unknownType(), toReference, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToFile() {
    Type fromFile = DelphiFileType.fileOf(INTEGER.type);
    Type toFile = DelphiFileType.fileOf(LONGINT.type);

    compare(fromFile, toFile, EQUAL);
    compare(untypedFile(), toFile, CONVERT_LEVEL_1);
    compare(fromFile, untypedFile(), CONVERT_LEVEL_1);
    compare(LONGINT.type, toFile, INCOMPATIBLE_TYPES);
    compare(DelphiFileType.fileOf(UNICODESTRING.type), toFile, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToPointer() {
    var parentType = TypeMocker.struct("Foo", CLASS);
    var subType = TypeMocker.struct("Bar", CLASS, parentType);

    compare(pointerTo(LONGINT.type), pointerTo(INTEGER.type), EQUAL);
    compare(pointerTo(subType), pointerTo(parentType), CONVERT_LEVEL_1);
    compare(SHORTSTRING.type, pointerTo(CHAR.type), CONVERT_LEVEL_2);
    compare(SHORTSTRING.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_3);
    compare(WIDECHAR.type, pointerTo(CHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, pointerTo(CHAR.type), CONVERT_LEVEL_1);
    compare(WIDECHAR.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_2);
    compare(ANSICHAR.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_2);
    compare(INTEGER.type, pointerTo(UNICODESTRING.type), CONVERT_LEVEL_5);
    compare(untypedPointer(), pointerTo(CHAR.type), CONVERT_LEVEL_2);
    compare(untypedPointer(), pointerTo(INTEGER.type), CONVERT_LEVEL_1);
    compare(pointerTo(CHAR.type), untypedPointer(), CONVERT_LEVEL_2);
    compare(pointerTo(INTEGER.type), untypedPointer(), CONVERT_LEVEL_1);
    compare(pointerTo(INTEGER.type), pointerTo(UNICODESTRING.type), INCOMPATIBLE_TYPES);
    compare(UNICODESTRING.type, pointerTo(INTEGER.type), INCOMPATIBLE_TYPES);
    compare(unknownType(), untypedPointer(), INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToUntyped() {
    compare(untypedType(), untypedType(), EQUAL);
    compare(UNICODESTRING.type, untypedType(), CONVERT_LEVEL_6);
  }

  @Test
  public void testToVariant() {
    Type interfaceType = TypeMocker.struct("Test", INTERFACE);

    compare(enumeration("Enum", unknownScope()), VARIANT.type, CONVERT_LEVEL_1);
    compare(dynamicArray("MyArray", unknownType()), VARIANT.type, CONVERT_LEVEL_1);
    compare(interfaceType, VARIANT.type, CONVERT_LEVEL_1);
    compare(OLE_VARIANT.type, VARIANT.type, CONVERT_LEVEL_1);
    compare(unknownType(), VARIANT.type, CONVERT_OPERATOR);
  }

  @Test
  public void testToIntrinsicTypeArgument() {
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
