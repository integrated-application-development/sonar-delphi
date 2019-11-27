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
import static org.sonar.plugins.delphi.symbol.resolve.VariantEqualityType.INCOMPATIBLE_VARIANT;

import java.util.List;
import org.assertj.core.util.VisibleForTesting;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.IntegerType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.FileType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.VariantType;
import org.sonar.plugins.delphi.type.Type.VariantType.VariantKind;

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

    EqualityType result = INCOMPATIBLE_TYPES;

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
    } else if (to.isObject()) {
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
    }

    if (result == INCOMPATIBLE_TYPES && (from.isVariant() || to.isVariant())) {
      if (from.isVariant() && VariantEqualityType.fromType(to) != INCOMPATIBLE_VARIANT) {
        result = CONVERT_LEVEL_6;
      } else {
        result = CONVERT_OPERATOR;
      }
    }

    return result;
  }

  private static EqualityType compareInteger(Type from, Type to) {
    if (from.isInteger()) {
      IntegerType fromData = IntegerType.fromType(from);
      IntegerType toData = IntegerType.fromType(to);
      if (fromData.isSameRange(toData)) {
        return EQUAL;
      } else if (fromData.isWithinLimit(toData)) {
        return CONVERT_LEVEL_1;
      } else {
        // Penalty for bad type conversion
        return CONVERT_LEVEL_3;
      }
    } else if (from.is(DecimalType.CURRENCY.type)) {
      return CONVERT_LEVEL_2;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareDecimal(Type from, Type to) {
    if (from.isInteger()) {
      if (to.is(DecimalType.SINGLE.type)) {
        // prefer single over others
        return CONVERT_LEVEL_3;
      } else {
        return CONVERT_LEVEL_4;
      }
    } else if (from.isDecimal()) {
      DecimalType fromData = DecimalType.fromType(from);
      DecimalType toData = DecimalType.fromType(to);
      if (fromData.size == toData.size) {
        return EQUAL;
      } else if (toData.size < fromData.size) {
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
    if (from.is(TextType.STRING.type)) {
      return compareNormalStringToText(to);
    } else if (from.is(TextType.WIDESTRING.type)) {
      return compareWideStringToText(to);
    } else if (from.is(TextType.UNICODESTRING.type)) {
      return compareUnicodeStringToText(to);
    } else if (from.is(TextType.SHORTSTRING.type)) {
      return compareShortStringToText(to);
    } else if (from.is(TextType.ANSISTRING.type)) {
      return CONVERT_LEVEL_1;
    }

    throw new AssertionError("Unhandled string type!");
  }

  private static EqualityType compareNormalStringToText(Type to) {
    if (to.is(TextType.UNICODESTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(TextType.WIDESTRING.type)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(TextType.ANSISTRING.type)) {
      return CONVERT_LEVEL_3;
    } else {
      return CONVERT_LEVEL_4;
    }
  }

  private static EqualityType compareWideStringToText(Type to) {
    if (to.is(TextType.UNICODESTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(TextType.ANSISTRING.type)) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  private static EqualityType compareUnicodeStringToText(Type to) {
    if (to.is(TextType.WIDESTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(TextType.ANSISTRING.type)) {
      return CONVERT_LEVEL_2;
    } else {
      return CONVERT_LEVEL_3;
    }
  }

  private static EqualityType compareShortStringToText(Type to) {
    if (to.is(TextType.ANSISTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(TextType.UNICODESTRING.type)) {
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
    if (to.is(TextType.SHORTSTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(TextType.ANSISTRING.type)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(TextType.STRING.type) || to.is(TextType.UNICODESTRING.type)) {
      return CONVERT_LEVEL_3;
    } else if (to.is(TextType.WIDESTRING.type)) {
      return CONVERT_LEVEL_4;
    }
    return INCOMPATIBLE_TYPES;
  }

  @VisibleForTesting
  static EqualityType compareWideCharToText(Type to) {
    if (to.is(TextType.STRING.type) || to.is(TextType.UNICODESTRING.type)) {
      return CONVERT_LEVEL_1;
    } else if (to.is(TextType.WIDESTRING.type)) {
      return CONVERT_LEVEL_2;
    } else if (to.is(TextType.ANSISTRING.type)) {
      return CONVERT_LEVEL_3;
    } else if (to.is(TextType.SHORTSTRING.type)) {
      return CONVERT_LEVEL_4;
    }
    return INCOMPATIBLE_TYPES;
  }

  private static EqualityType compareBoolean(Type from, Type to) {
    if (from.isBoolean()) {
      BooleanType fromData = BooleanType.fromType(from);
      BooleanType toData = BooleanType.fromType(to);
      if (fromData.size > toData.size) {
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
      } else if (to.isFixedArray()) {
        return compareFixedArray(fromArray, toArray);
      }
    } else if (from.isPointer()) {
      return comparePointerToArray((PointerType) from, toArray);
    } else if (from.isNarrowChar()) {
      return compareCharToArray(toArray);
    } else if (from.isVariant()) {
      return compareVariantToArray(toArray);
    }

    return INCOMPATIBLE_TYPES;
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
    if (from.isObject() && from.isSubTypeOf(to)) {
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
    Type pointerTo = ((PointerType) to).dereferencedType();
    if (from.isText()) {
      if (from.is(TextType.SHORTSTRING.type)) {
        if (pointerTo.isWideChar()) {
          return CONVERT_LEVEL_2;
        } else {
          return CONVERT_LEVEL_3;
        }
      } else if (from.isChar()) {
        if (pointerTo.isWideChar()) {
          return CONVERT_LEVEL_1;
        } else {
          return CONVERT_LEVEL_2;
        }
      }
    } else if (from.isInteger()) {
      return CONVERT_LEVEL_5;
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

  static boolean equals(Type from, Type to) {
    return compare(from, to).ordinal() >= EQUAL.ordinal();
  }
}
