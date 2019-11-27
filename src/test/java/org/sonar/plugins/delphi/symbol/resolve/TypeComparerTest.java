package org.sonar.plugins.delphi.symbol.resolve;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.plugins.delphi.symbol.UnknownScope.unknownScope;
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
import static org.sonar.plugins.delphi.type.DelphiCollectionType.dynamicArray;
import static org.sonar.plugins.delphi.type.DelphiCollectionType.emptySet;
import static org.sonar.plugins.delphi.type.DelphiCollectionType.fixedArray;
import static org.sonar.plugins.delphi.type.DelphiCollectionType.openArray;
import static org.sonar.plugins.delphi.type.DelphiEnumerationType.enumeration;
import static org.sonar.plugins.delphi.type.DelphiEnumerationType.subRange;
import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType.BOOLEAN;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType.BYTEBOOL;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType.WORDBOOL;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.CURRENCY;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.DOUBLE;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.REAL;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType.SINGLE;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.INTEGER;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.LONGINT;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType.SMALLINT;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.ANSICHAR;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.ANSISTRING;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.CHAR;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.SHORTSTRING;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.STRING;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.UNICODESTRING;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.WIDECHAR;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.WIDESTRING;
import static org.sonar.plugins.delphi.type.DelphiPointerType.nilPointer;
import static org.sonar.plugins.delphi.type.DelphiPointerType.pointerTo;
import static org.sonar.plugins.delphi.type.DelphiPointerType.untypedPointer;
import static org.sonar.plugins.delphi.type.DelphiProceduralType.anonymous;
import static org.sonar.plugins.delphi.type.DelphiProceduralType.procedure;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.DelphiType.untypedType;
import static org.sonar.plugins.delphi.type.DelphiVariantType.oleVariant;
import static org.sonar.plugins.delphi.type.DelphiVariantType.variant;
import static org.sonar.plugins.delphi.type.StructKind.CLASS;
import static org.sonar.plugins.delphi.type.StructKind.INTERFACE;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.sonar.plugins.delphi.type.DelphiClassReferenceType;
import org.sonar.plugins.delphi.type.DelphiCollectionType;
import org.sonar.plugins.delphi.type.DelphiFileType;
import org.sonar.plugins.delphi.type.DelphiStructType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;

public class TypeComparerTest {
  private static void compare(Type from, Type to, EqualityType equality) {
    assertThat(TypeComparer.compare(from, to)).isEqualTo(equality);
  }

  @Test
  public void testExactTypes() {
    compare(STRING.type, STRING.type, EXACT);
  }

  @Test
  public void testToInteger() {
    compare(SMALLINT.type, INTEGER.type, CONVERT_LEVEL_1);
    compare(INTEGER.type, SMALLINT.type, CONVERT_LEVEL_3);
    compare(CURRENCY.type, INTEGER.type, CONVERT_LEVEL_2);
    compare(variant(), INTEGER.type, CONVERT_LEVEL_6);
    compare(STRING.type, INTEGER.type, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToDecimal() {
    compare(INTEGER.type, SINGLE.type, CONVERT_LEVEL_3);
    compare(INTEGER.type, DOUBLE.type, CONVERT_LEVEL_4);
    compare(DOUBLE.type, SINGLE.type, CONVERT_LEVEL_2);
    compare(SINGLE.type, DOUBLE.type, CONVERT_LEVEL_1);
    compare(DOUBLE.type, REAL.type, EQUAL);
    compare(STRING.type, DOUBLE.type, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testStringToText() {
    compare(STRING.type, UNICODESTRING.type, CONVERT_LEVEL_1);
    compare(STRING.type, WIDESTRING.type, CONVERT_LEVEL_2);
    compare(STRING.type, ANSISTRING.type, CONVERT_LEVEL_3);
    compare(STRING.type, CHAR.type, CONVERT_LEVEL_4);
    compare(STRING.type, unknownType(), INCOMPATIBLE_TYPES);

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

    assertThatThrownBy(() -> TypeComparer.compareStringToText(unknownType(), unknownType()))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  public void testCharToText() {
    compare(ANSICHAR.type, SHORTSTRING.type, CONVERT_LEVEL_1);
    compare(ANSICHAR.type, ANSISTRING.type, CONVERT_LEVEL_2);
    compare(ANSICHAR.type, STRING.type, CONVERT_LEVEL_3);
    compare(ANSICHAR.type, UNICODESTRING.type, CONVERT_LEVEL_3);
    compare(ANSICHAR.type, WIDESTRING.type, CONVERT_LEVEL_4);
    assertThat(TypeComparer.compareAnsiCharToText(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    compare(CHAR.type, STRING.type, CONVERT_LEVEL_1);
    compare(CHAR.type, UNICODESTRING.type, CONVERT_LEVEL_1);
    compare(CHAR.type, WIDESTRING.type, CONVERT_LEVEL_2);
    compare(CHAR.type, ANSISTRING.type, CONVERT_LEVEL_3);
    compare(CHAR.type, SHORTSTRING.type, CONVERT_LEVEL_4);
    assertThat(TypeComparer.compareWideCharToText(unknownType())).isEqualTo(INCOMPATIBLE_TYPES);

    assertThatThrownBy(() -> TypeComparer.compareCharToText(unknownType(), unknownType()))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  public void testToBoolean() {
    compare(BOOLEAN.type, BYTEBOOL.type, CONVERT_LEVEL_1);
    compare(BOOLEAN.type, WORDBOOL.type, CONVERT_LEVEL_1);
    compare(WORDBOOL.type, BOOLEAN.type, CONVERT_LEVEL_3);
    compare(STRING.type, BOOLEAN.type, INCOMPATIBLE_TYPES);
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
    compare(variant(), enumType, CONVERT_LEVEL_1);
    compare(INTEGER.type, enumType, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToArray() {
    CollectionType fromOpenArray = openArray("Open", INTEGER.type);
    CollectionType fromDynamicArray = dynamicArray("Dynamic", INTEGER.type);
    CollectionType fromFixedArray = fixedArray("Fixed", INTEGER.type);
    CollectionType fromIncompatibleOpenArray = openArray("OpenString", STRING.type);
    CollectionType fromIncompatibleDynamicArray = dynamicArray("DynamicString", STRING.type);
    CollectionType fromIncompatibleFixedArray = fixedArray("FixedString", STRING.type);
    CollectionType toDynamicArray = dynamicArray(null, INTEGER.type);
    CollectionType toFixedArray = fixedArray(null, INTEGER.type);
    CollectionType toOpenArray = openArray(null, INTEGER.type);

    compare(INTEGER.type, toOpenArray, CONVERT_LEVEL_3);
    compare(fromDynamicArray, toDynamicArray, EQUAL);
    compare(fromFixedArray, toDynamicArray, CONVERT_LEVEL_2);
    compare(dynamicArray(null, STRING.type), toDynamicArray, INCOMPATIBLE_TYPES);

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
    compare(pointerTo(STRING.type), toOpenArray, INCOMPATIBLE_TYPES);
    compare(pointerTo(unknownType()), toDynamicArray, INCOMPATIBLE_TYPES);

    compare(ANSICHAR.type, dynamicArray(null, ANSICHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, toDynamicArray, INCOMPATIBLE_TYPES);

    compare(variant(), toDynamicArray, CONVERT_LEVEL_1);
    compare(variant(), toOpenArray, CONVERT_OPERATOR);

    compare(unknownType(), toOpenArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toDynamicArray, INCOMPATIBLE_TYPES);
    compare(unknownType(), toFixedArray, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToSet() {
    // NOTE: Sets can't actually have such large ordinal element types (and certainly not strings)
    // This is just for testing convenience.
    CollectionType integerSet = DelphiCollectionType.set(INTEGER.type);
    CollectionType longIntSet = DelphiCollectionType.set(LONGINT.type);
    CollectionType stringSet = DelphiCollectionType.set(STRING.type);

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
    Type returnType = STRING.type;

    ProceduralType fromProcedure = procedure(parameters, returnType);
    ProceduralType similarFromProcedure = procedure(similarParameters, returnType);
    ProceduralType incompatibleProcedure = procedure(parameters, INTEGER.type);
    ProceduralType toProcedure = anonymous(parameters, returnType);

    compare(fromProcedure, toProcedure, EQUAL);
    compare(similarFromProcedure, toProcedure, CONVERT_LEVEL_1);
    compare(incompatibleProcedure, toProcedure, INCOMPATIBLE_TYPES);

    compare(nilPointer(), toProcedure, CONVERT_LEVEL_1);
    compare(untypedPointer(), toProcedure, CONVERT_LEVEL_1);
    compare(pointerTo(returnType), toProcedure, INCOMPATIBLE_TYPES);

    compare(unknownType(), toProcedure, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToObject() {
    Type toObject = DelphiStructType.from("To", unknownScope(), Collections.emptySet(), CLASS);
    Type fromObject = DelphiStructType.from("From", unknownScope(), singleton(toObject), CLASS);

    compare(fromObject, toObject, CONVERT_LEVEL_3);
    compare(untypedPointer(), toObject, CONVERT_LEVEL_2);
    compare(nilPointer(), toObject, CONVERT_LEVEL_1);
    compare(pointerTo(toObject), toObject, INCOMPATIBLE_TYPES);
    compare(variant(), toObject, CONVERT_OPERATOR);
    compare(unknownType(), toObject, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToClassReference() {
    var toObject = DelphiStructType.from("Foo", unknownScope(), Collections.emptySet(), CLASS);
    var fromObject = DelphiStructType.from("Bar", unknownScope(), singleton(toObject), CLASS);
    var otherObject = DelphiStructType.from("Baz", unknownScope(), Collections.emptySet(), CLASS);

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
    compare(DelphiFileType.fileOf(STRING.type), toFile, INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToPointer() {
    compare(SHORTSTRING.type, pointerTo(CHAR.type), CONVERT_LEVEL_2);
    compare(SHORTSTRING.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_3);
    compare(WIDECHAR.type, pointerTo(CHAR.type), CONVERT_LEVEL_1);
    compare(ANSICHAR.type, pointerTo(CHAR.type), CONVERT_LEVEL_1);
    compare(WIDECHAR.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_2);
    compare(ANSICHAR.type, pointerTo(ANSICHAR.type), CONVERT_LEVEL_2);
    compare(INTEGER.type, pointerTo(STRING.type), CONVERT_LEVEL_5);
    compare(unknownType(), untypedPointer(), INCOMPATIBLE_TYPES);
  }

  @Test
  public void testToUntyped() {
    compare(untypedType(), untypedType(), EQUAL);
    compare(STRING.type, untypedType(), CONVERT_LEVEL_6);
  }

  @Test
  public void testToVariant() {
    Type interfaceType =
        DelphiStructType.from("Test", unknownScope(), Collections.emptySet(), INTERFACE);

    compare(enumeration("Enum", unknownScope()), variant(), CONVERT_LEVEL_1);
    compare(dynamicArray("MyArray", unknownType()), variant(), CONVERT_LEVEL_1);
    compare(interfaceType, variant(), CONVERT_LEVEL_1);
    compare(oleVariant(), variant(), CONVERT_LEVEL_1);
    compare(unknownType(), variant(), CONVERT_OPERATOR);
  }
}
