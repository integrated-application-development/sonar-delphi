package org.sonar.plugins.delphi.symbol.resolve;

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
import static org.sonar.plugins.delphi.symbol.resolve.VariantConversionType.INCOMPATIBLE_VARIANT;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.CURRENCY;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.SINGLE;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSISTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.SHORTSTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.STRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDESTRING;

import java.util.List;
import org.assertj.core.util.VisibleForTesting;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;
import org.sonar.plugins.delphi.type.Type.BooleanType;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.DecimalType;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.Type.IntegerType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.TextType;
import org.sonar.plugins.delphi.type.Type.TypeType;
import org.sonar.plugins.delphi.type.Type.VariantType;
import org.sonar.plugins.delphi.type.Type.VariantType.VariantKind;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicArgumentMatcher;

public class TypeComparer {
  private TypeComparer() {
    // Utility class
  }

  /**
   * Based directly off of compare_defs_ext from the FreePascal compiler.
   *
   * @param from The type we are trying to convert from
   * @param to The type we are trying to convert to
   * @return equality type
   * @see <a href="http://bit.ly/compare_defs_ext"/>
   */
  static EqualityType compare(Type from, Type to) {
    if (from.is(to) && !from.isUntyped()) {
      return EXACT;
    }

    if (from.isTypeType()) {
      from = ((TypeType) from).originalType();
    }

    if (to.isTypeType()) {
      to = ((TypeType) to).originalType();
    }

    EqualityType result;

    if (to.isInteger()) {
      result = compareInteger(from, to);
    } else if (to.isDecimal()) {
      result = compareDecimal(from, to);
    } else if (to.isText()) {
      result = compareText(from, to);
    } else if (to.isBoolean()) {
      result = compareBoolean(from, to);
    } else if (to.isEnum()) {
      result = compareEnum(from, to);
    } else if (to.isArray()) {
      result = compareArray(from, to);
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
        result = CONVERT_LEVEL_6;
      } else {
        result = CONVERT_OPERATOR;
      }
    }

    return result;
  }

  private static EqualityType compareInteger(Type from, Type to) {
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
    } else if (from.is(CURRENCY.type)) {
      return CONVERT_LEVEL_2;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareDecimal(Type from, Type to) {
    if (from.isInteger()) {
      if (to.is(SINGLE.type)) {
        // prefer single over others
        return CONVERT_LEVEL_3;
      } else {
        return CONVERT_LEVEL_4;
      }
    } else if (from.isDecimal()) {
      DecimalType fromDecimal = (DecimalType) from;
      DecimalType toDecimal = (DecimalType) to;
      if (fromDecimal.size() == toDecimal.size()) {
        return EQUAL;
      } else if (toDecimal.size() < fromDecimal.size()) {
        // Penalty for lost precision
        return CONVERT_LEVEL_2;
      } else {
        return CONVERT_LEVEL_1;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareText(Type from, Type to) {
    if (from.isString()) {
      return compareStringToText(from, to);
    } else if (from.isChar()) {
      return compareCharToText(from, to);
    }
    return INCOMPATIBLE_TYPES;
  }

  @VisibleForTesting
  static EqualityType compareStringToText(Type from, Type to) {
    if (from.is(to)) {
      return EQUAL;
    } else if (from.is(STRING.type)) {
      return compareNormalStringToText(to);
    } else if (from.is(WIDESTRING.type)) {
      return compareWideStringToText(to);
    } else if (from.is(UNICODESTRING.type)) {
      return compareUnicodeStringToText(to);
    } else if (from.is(SHORTSTRING.type)) {
      return compareShortStringToText(to);
    } else if (from.is(ANSISTRING.type)) {
      return CONVERT_LEVEL_1;
    }

    throw new AssertionError("Unhandled string type!");
  }

  private static EqualityType compareNormalStringToText(Type to) {
    if (to.is(UNICODESTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(WIDESTRING.type)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(ANSISTRING.type)) {
      return CONVERT_LEVEL_3;
    } else {
      return CONVERT_LEVEL_4;
    }
  }

  private static EqualityType compareWideStringToText(Type to) {
    if (to.is(UNICODESTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(ANSISTRING.type)) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  private static EqualityType compareUnicodeStringToText(Type to) {
    if (to.is(WIDESTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(ANSISTRING.type)) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  private static EqualityType compareShortStringToText(Type to) {
    if (to.is(ANSISTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(UNICODESTRING.type)) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  @VisibleForTesting
  static EqualityType compareCharToText(Type from, Type to) {
    if (from.isNarrowChar()) {
      return compareAnsiCharToText(to);
    }

    if (from.isWideChar()) {
      return compareWideCharToText(to);
    }

    throw new AssertionError("Unhandled char type!");
  }

  @VisibleForTesting
  static EqualityType compareAnsiCharToText(Type to) {
    if (to.isChar()) {
      return CONVERT_LEVEL_1;
    } else if (to.is(SHORTSTRING.type)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(ANSISTRING.type)) {
      return CONVERT_LEVEL_3;
    } else if (to.is(STRING.type) || to.is(UNICODESTRING.type)) {
      return CONVERT_LEVEL_4;
    } else if (to.is(WIDESTRING.type)) {
      return CONVERT_LEVEL_5;
    }
    return INCOMPATIBLE_TYPES;
  }

  @VisibleForTesting
  static EqualityType compareWideCharToText(Type to) {
    if (to.isWideChar()) {
      return CONVERT_LEVEL_1;
    } else if (to.is(STRING.type) || to.is(UNICODESTRING.type)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(WIDESTRING.type) || to.isNarrowChar()) {
      return CONVERT_LEVEL_3;
    } else if (to.is(ANSISTRING.type)) {
      return CONVERT_LEVEL_4;
    } else if (to.is(SHORTSTRING.type)) {
      return CONVERT_LEVEL_5;
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
    if (from.isEnum()) {
      Type fromBaseType = from;

      while (fromBaseType instanceof EnumType) {
        Type base = ((EnumType) fromBaseType).baseType();
        if (base == null) {
          break;
        }
        fromBaseType = base;
      }

      Type toBaseType = to;

      while (toBaseType instanceof EnumType) {
        Type base = ((EnumType) toBaseType).baseType();
        if (base == null) {
          break;
        }
        toBaseType = base;
      }

      if (fromBaseType.is(toBaseType)) {
        // because of packenum they can have different sizes
        return CONVERT_LEVEL_1;
      }
    } else if (from.isVariant()) {
      return CONVERT_LEVEL_1;
    }
    return INCOMPATIBLE_TYPES;
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
    } else if (from.isNarrowChar()) {
      return compareCharToArray(toArray);
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

  private static EqualityType compareArrayConstructorToDynamicArray(
      ArrayConstructorType from, CollectionType to) {
    if (from.isEmpty()) {
      // Only needs to lose to [] -> open array
      return CONVERT_LEVEL_2;
    }

    // this should lose to the array constructor -> open array conversions,
    // but it might happen that the end of the convert levels is reached
    var subEquality = compare(from.elementTypes().get(0), to.elementType());
    if (subEquality.ordinal() >= EQUAL.ordinal()) {
      return CONVERT_LEVEL_2;
    } else if (subEquality.ordinal() > CONVERT_LEVEL_5.ordinal()) {
      // an array constructor is not a dynamic array, so use a lower level of compatibility
      // than that one of the elements
      return EqualityType.values()[subEquality.ordinal() - 2];
    } else if (subEquality == CONVERT_LEVEL_6) {
      return CONVERT_LEVEL_5;
    } else if (subEquality == CONVERT_OPERATOR) {
      return CONVERT_LEVEL_6;
    } else {
      return subEquality;
    }
  }

  private static EqualityType compareArrayConstructorToOpenArray(
      ArrayConstructorType from, CollectionType to) {
    if (from.isEmpty()) {
      return CONVERT_LEVEL_1;
    } else {
      var subEquality = compare(from.elementTypes().get(0), to.elementType());
      if (subEquality.ordinal() >= EQUAL.ordinal()) {
        return CONVERT_LEVEL_1;
      } else if (subEquality.ordinal() > CONVERT_LEVEL_6.ordinal()) {
        // an array constructor is not an open array, so use a lower level of compatibility
        // than that one of the elements
        return EqualityType.values()[subEquality.ordinal() - 1];
      } else {
        return subEquality;
      }
    }
  }

  private static EqualityType compareDynamicArray(CollectionType from, CollectionType to) {
    if (equals(from.elementType(), to.elementType())) {
      return from.isDynamicArray() ? EQUAL : CONVERT_LEVEL_2;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareOpenArray(CollectionType from, CollectionType to) {
    if (from.isDynamicArray() && equals(from.elementType(), to.elementType())) {
      return CONVERT_LEVEL_2;
    } else if (from.isOpenArray() && equals(from.elementType(), to.elementType())) {
      if (from.elementType().is(to.elementType())) {
        return EXACT;
      }
      return EQUAL;
    } else if (from.isFixedArray() && equals(from.elementType(), to.elementType())) {
      return EQUAL;
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
    if (equals(from.dereferencedType(), to.elementType())
        || (to.isDynamicArray() && (from.isNilPointer() || from.isUntypedPointer()))) {
      return CONVERT_LEVEL_1;
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareCharToArray(CollectionType to) {
    return to.elementType().isNarrowChar() ? CONVERT_LEVEL_1 : INCOMPATIBLE_TYPES;
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
      // Automatic array constructor -> set conversion
      return CONVERT_LEVEL_1;
    }

    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareProceduralType(Type from, Type to) {
    if (from.isProcedural()) {
      return procToProcVarEqual((ProceduralType) from, (ProceduralType) to);
    } else if (from.isPointer()) {
      PointerType fromPointer = (PointerType) from;
      if (fromPointer.isNilPointer() || fromPointer.isUntypedPointer()) {
        return CONVERT_LEVEL_1;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType procToProcVarEqual(ProceduralType from, ProceduralType to) {
    if (equals(from.returnType(), to.returnType())) {
      var paramEquality = compareParameters(from.parameterTypes(), to.parameterTypes());
      if (paramEquality == EXACT) {
        return EQUAL;
      } else if (paramEquality == EQUAL) {
        return CONVERT_LEVEL_1;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareParameters(List<Type> from, List<Type> to) {
    if (from.size() != to.size()) {
      return INCOMPATIBLE_TYPES;
    }

    EqualityType lowestEquality = EXACT;

    for (int i = 0; i < to.size(); ++i) {
      EqualityType equality = compare(from.get(i), to.get(i));
      if (equality.ordinal() < lowestEquality.ordinal()) {
        lowestEquality = equality;
      }
    }

    return lowestEquality;
  }

  private static EqualityType compareObject(Type from, Type to) {
    if (from.isStruct() && from.isSubTypeOf(to)) {
      return CONVERT_LEVEL_3;
    } else if (from.isPointer()) {
      PointerType fromPointer = (PointerType) from;
      if (fromPointer.isUntypedPointer()) {
        return CONVERT_LEVEL_2;
      } else if (fromPointer.isNilPointer()) {
        return CONVERT_LEVEL_1;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareClassReference(Type from, Type to) {
    if (from.isClassReference()) {
      Type fromReference = ((ClassReferenceType) from).classType();
      Type toReference = ((ClassReferenceType) to).classType();
      if (fromReference.isSubTypeOf(toReference)) {
        return CONVERT_LEVEL_1;
      }
    } else if (from.isPointer()) {
      PointerType fromPointer = (PointerType) from;
      if (fromPointer.isUntypedPointer()) {
        return CONVERT_LEVEL_2;
      } else if (fromPointer.isNilPointer()) {
        return CONVERT_LEVEL_1;
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
    } else if (from.isText()) {
      return compareTextToPointer((TextType) from, pointerTo);
    } else if (from.isInteger()) {
      return CONVERT_LEVEL_5;
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
    } else if (from.isUntypedPointer()) {
      // All pointers can be assigned from untyped pointer
      if (to.dereferencedType().isWideChar()) {
        return CONVERT_LEVEL_2;
      }
      return CONVERT_LEVEL_1;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareTextToPointer(TextType from, PointerType to) {
    if (from.is(SHORTSTRING.type) && to.dereferencedType().isChar()) {
      if (to.dereferencedType().isWideChar()) {
        return CONVERT_LEVEL_2;
      } else {
        return CONVERT_LEVEL_3;
      }
    } else if (from.isChar() && to.dereferencedType().isChar()) {
      if (to.dereferencedType().isWideChar()) {
        return CONVERT_LEVEL_1;
      } else {
        return CONVERT_LEVEL_2;
      }
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareUntyped(Type from) {
    return from.isUntyped() ? EQUAL : CONVERT_LEVEL_6;
  }

  private static EqualityType compareVariant(Type from) {
    boolean oleVariant = from.isVariant() && ((VariantType) from).kind() == VariantKind.OLE_VARIANT;

    if (oleVariant || from.isEnum() || from.isDynamicArray() || from.isInterface()) {
      return CONVERT_LEVEL_1;
    }

    return INCOMPATIBLE_TYPES;
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
