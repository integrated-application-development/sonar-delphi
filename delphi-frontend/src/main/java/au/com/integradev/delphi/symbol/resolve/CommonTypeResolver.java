/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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

import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.type.TypeUtils;
import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

/**
 * Resolves the common type of an {@code if} expression's operands, documented by Embarcadero as the
 * "least upper bound".
 *
 * <p>In practice, this is not a strict least upper bound:
 *
 * <ul>
 *   <li>ties break toward the first operand (operand order matters)
 *   <li>some results cannot hold both operands
 * </ul>
 *
 * @see <a href="https://docwiki.embarcadero.com/RADStudio/en/Conditional_Operators_(Delphi)">
 *     Conditional Operators (Delphi) </a>
 */
final class CommonTypeResolver {
  private static final List<IntegerTier> INTEGER_TIERS =
      List.of(
          new IntegerTier(IntrinsicType.SHORTINT, IntrinsicType.BYTE),
          new IntegerTier(IntrinsicType.SMALLINT, IntrinsicType.WORD),
          new IntegerTier(IntrinsicType.INTEGER, IntrinsicType.CARDINAL),
          new IntegerTier(IntrinsicType.INT64, IntrinsicType.UINT64));

  private static final class IntegerTier {
    final IntrinsicType signedType;
    final IntrinsicType unsignedType;

    IntegerTier(IntrinsicType signedType, IntrinsicType unsignedType) {
      this.signedType = signedType;
      this.unsignedType = unsignedType;
    }
  }

  private final TypeFactory typeFactory;
  private final ArrayElementInferredTypeResolver elementResolver;

  CommonTypeResolver(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
    this.elementResolver = new ArrayElementInferredTypeResolver(typeFactory);
  }

  /** The common type of two operands, or unknown where Delphi rejects the combination. */
  Type commonType(Type first, Type second) {
    if (first.is(second)) {
      return first;
    }

    if (isNilPointer(first) && !second.isPointer()) {
      return nilCombinedType(second);
    }
    if (isNilPointer(second) && !first.isPointer()) {
      return nilCombinedType(first);
    }

    if (first.isInteger() && second.isInteger()) {
      return commonIntegerType(first, second);
    }

    if (isNumeric(first) && isNumeric(second)) {
      return commonRealType(first, second);
    }

    if (isBooleanBased(first) && isBooleanBased(second)) {
      return first;
    }

    if (first.isVariant() && second.isVariant()) {
      return typeFactory.getIntrinsic(IntrinsicType.VARIANT);
    }

    if (isTextual(first) && isTextual(second)) {
      return commonTextualType(first, second);
    }

    if (isPWideCharWithTextual(first, second)) {
      return typeFactory.getIntrinsic(IntrinsicType.UNICODESTRING);
    }

    if (first.isPointer() && second.isPointer()) {
      return commonPointerType(first, second);
    }

    if (first.isClass() && second.isClass()) {
      return nearestCommonAncestor(first, second);
    }

    if (first.isInterface() && second.isInterface()) {
      return nearestCommonAncestor(first, second);
    }

    if (first.isClassReference() && second.isClassReference()) {
      return commonClassReferenceType(first, second);
    }

    if (first.isProcedural() && second.isProcedural()) {
      return commonProceduralType(first, second);
    }

    if (first.isSet() && second.isSet()) {
      return commonSetType(first, second);
    }

    if (first.isSet() && second.isArrayConstructor()) {
      return setAbsorbingConstructor(first, (ArrayConstructorType) second);
    }
    if (second.isSet() && first.isArrayConstructor()) {
      return setAbsorbingConstructor(second, (ArrayConstructorType) first);
    }

    if (first.isArrayConstructor() && second.isArrayConstructor()) {
      return commonArrayConstructorType(
          (ArrayConstructorType) first, (ArrayConstructorType) second);
    }

    return sharedBaseType(first, second);
  }

  private static Type sharedBaseType(Type first, Type second) {
    Type baseType = TypeUtils.findBaseType(first);
    if (baseType.is(TypeUtils.findBaseType(second))) {
      return baseType;
    }
    return unknownType();
  }

  private static Type unalias(Type type) {
    return TypeUtils.findAliasedType(type);
  }

  private Type commonArrayConstructorType(ArrayConstructorType first, ArrayConstructorType second) {
    return allElementsOrdinal(first) && allElementsOrdinal(second) ? first : unknownType();
  }

  private static boolean isNilPointer(Type type) {
    return type.isPointer() && ((PointerType) type).isNilPointer();
  }

  private Type nilCombinedType(Type other) {
    if (other.isArrayConstructor()) {
      return anonymousDynamicArray((ArrayConstructorType) other);
    }
    return acceptsNil(other) ? other : unknownType();
  }

  private Type anonymousDynamicArray(ArrayConstructorType constructor) {
    Type element = elementResolver.elementType(constructor.elementTypes());
    if (element.isUnknown()) {
      return unknownType();
    }
    return ((TypeFactoryImpl) typeFactory).array(null, element, Set.of(ArrayOption.DYNAMIC));
  }

  private static boolean acceptsNil(Type type) {
    return type.isClass()
        || type.isInterface()
        || type.isClassReference()
        || type.isProcedural()
        || type.isDynamicArray();
  }

  private static boolean isPWideCharWithTextual(Type first, Type second) {
    return (unalias(first).is(IntrinsicType.PWIDECHAR) && isTextual(second))
        || (unalias(second).is(IntrinsicType.PWIDECHAR) && isTextual(first));
  }

  private Type commonPointerType(Type first, Type second) {
    PointerType firstPointer = (PointerType) first;
    PointerType secondPointer = (PointerType) second;

    if (firstPointer.isNilPointer()) {
      return typeFactory.getIntrinsic(IntrinsicType.POINTER);
    }

    if (secondPointer.isNilPointer()
        || firstPointer.isUntypedPointer()
        || secondPointer.isUntypedPointer()) {
      return first;
    }

    return typeFactory.getIntrinsic(IntrinsicType.POINTER);
  }

  private static Type nearestCommonAncestor(Type first, Type second) {
    for (Type candidate = first; candidate.isStruct(); candidate = candidate.parent()) {
      if (second.is(candidate) || second.isDescendantOf(candidate)) {
        return candidate;
      }
    }
    return unknownType();
  }

  private Type commonClassReferenceType(Type first, Type second) {
    ClassReferenceType firstReference = (ClassReferenceType) unalias(first);
    ClassReferenceType secondReference = (ClassReferenceType) unalias(second);

    if (firstReference.classType().is(secondReference.classType())) {
      // For a class reference tie, the compiler prefers the second (!!!) operand.
      // This is a departure from every other case, where the first operand is preferred.
      return second;
    }

    Type ancestor = nearestCommonAncestor(firstReference.classType(), secondReference.classType());
    if (ancestor.isUnknown()) {
      return unknownType();
    }

    if (ancestor.is(firstReference.classType())) {
      return firstReference;
    }

    if (ancestor.is(secondReference.classType())) {
      return secondReference;
    }

    return typeFactory.classOf(null, ancestor);
  }

  private Type commonSetType(Type first, Type second) {
    Type firstElement = ((CollectionType) first).elementType();
    Type secondElement = ((CollectionType) second).elementType();

    if (containsRange(firstElement, secondElement)) {
      return first;
    }
    if (containsRange(secondElement, firstElement)) {
      return second;
    }

    Type firstElementBase = TypeUtils.findBaseType(firstElement);
    Type secondElementBase = TypeUtils.findBaseType(secondElement);

    if (firstElementBase.isInteger() && secondElementBase.isInteger()) {
      return typeFactory.set(typeFactory.getIntrinsic(IntrinsicType.BYTE));
    }

    if (firstElementBase.isChar() && secondElementBase.isChar()) {
      return typeFactory.set(typeFactory.getIntrinsic(IntrinsicType.ANSICHAR));
    }

    return unknownType();
  }

  private static boolean containsRange(Type container, Type contained) {
    if (container instanceof IntegerType && contained instanceof IntegerType) {
      IntegerType containerRange = (IntegerType) container;
      IntegerType containedRange = (IntegerType) contained;
      return containerRange.min().compareTo(containedRange.min()) <= 0
          && containerRange.max().compareTo(containedRange.max()) >= 0;
    }
    return TypeUtils.findBaseType(contained).is(TypeUtils.findBaseType(container));
  }

  private static boolean allElementsOrdinal(ArrayConstructorType constructor) {
    return constructor.elementTypes().stream().allMatch(CommonTypeResolver::isOrdinal);
  }

  private static boolean isOrdinal(Type type) {
    return type.isInteger()
        || type.isEnum()
        || type.isChar()
        || type.isBoolean()
        || type.isSubrange();
  }

  private Type setAbsorbingConstructor(Type set, ArrayConstructorType constructor) {
    Type element = ((CollectionType) set).elementType();

    for (Type constructorElement : constructor.elementTypes()) {
      if (commonType(element, constructorElement).isUnknown()) {
        return unknownType();
      }
    }

    return set;
  }

  private Type commonProceduralType(Type first, Type second) {
    ProceduralType firstProcedural = (ProceduralType) first;
    ProceduralType secondProcedural = (ProceduralType) second;

    if (!signaturesMatch(firstProcedural, secondProcedural)) {
      return unknownType();
    }

    if (firstProcedural.kind() == ProceduralKind.REFERENCE
        && secondProcedural.kind() == ProceduralKind.ANONYMOUS) {
      return first;
    }

    if (firstProcedural.kind() == ProceduralKind.ANONYMOUS
        && secondProcedural.kind() == ProceduralKind.REFERENCE) {
      return second;
    }

    return unknownType();
  }

  private static boolean signaturesMatch(ProceduralType first, ProceduralType second) {
    if (first.parametersCount() != second.parametersCount()
        || !first.returnType().is(second.returnType())) {
      return false;
    }

    for (int i = 0; i < first.parametersCount(); i++) {
      if (!first.parameters().get(i).getType().is(second.parameters().get(i).getType())) {
        return false;
      }
    }

    return true;
  }

  private Type commonIntegerType(Type first, Type second) {
    IntegerType firstInteger = (IntegerType) first;
    IntegerType secondInteger = (IntegerType) second;
    BigInteger min = firstInteger.min().min(secondInteger.min());
    BigInteger max = firstInteger.max().max(secondInteger.max());

    for (IntegerTier tier : INTEGER_TIERS) {
      IntegerType signedType = (IntegerType) typeFactory.getIntrinsic(tier.signedType);
      IntegerType unsignedType = (IntegerType) typeFactory.getIntrinsic(tier.unsignedType);

      boolean signedHolds = holdsRange(signedType, min, max);
      boolean unsignedHolds = holdsRange(unsignedType, min, max);

      if (signedHolds && unsignedHolds) {
        return resultForSignedUnsignedTie(signedType);
      } else if (signedHolds) {
        return signedType;
      } else if (unsignedHolds) {
        return unsignedType;
      }
    }

    // Nothing holds both an Int64 and a UInt64; the compiler treats the result as unsigned.
    return typeFactory.getIntrinsic(IntrinsicType.UINT64);
  }

  private static boolean holdsRange(IntegerType type, BigInteger min, BigInteger max) {
    return type.min().compareTo(min) <= 0 && type.max().compareTo(max) >= 0;
  }

  /**
   * @param signedType the narrowest signed type holding the combined range
   */
  private Type resultForSignedUnsignedTie(IntegerType signedType) {
    switch (signedType.size()) {
      case 2:
        return ((TypeFactoryImpl) typeFactory).anonymousUInt15();
      case 4:
        return ((TypeFactoryImpl) typeFactory).anonymousUInt31();
      default:
        return signedType;
    }
  }

  private Type commonRealType(Type first, Type second) {
    if (isFloatingPoint(first) || isFloatingPoint(second) || extendedHoldsFixedPointTypes()) {
      return typeFactory.getIntrinsic(IntrinsicType.EXTENDED);
    }

    if (isCurrency(first) || isCurrency(second)) {
      return typeFactory.getIntrinsic(IntrinsicType.CURRENCY);
    }

    return typeFactory.getIntrinsic(IntrinsicType.COMP);
  }

  /** An Extended larger than 8 bytes can hold Comp or Currency without losing precision. */
  private boolean extendedHoldsFixedPointTypes() {
    return typeFactory.getIntrinsic(IntrinsicType.EXTENDED).size() > 8;
  }

  private static boolean isCurrency(Type type) {
    return unalias(type).is(IntrinsicType.CURRENCY);
  }

  private Type commonTextualType(Type first, Type second) {
    Type firstBase = TypeUtils.findBaseType(first);
    Type secondBase = TypeUtils.findBaseType(second);

    if (firstBase.is(secondBase)) {
      boolean stringsKeepTheirDeclaredType = firstBase.isString();
      return stringsKeepTheirDeclaredType ? first : firstBase;
    }

    if (isShortString(first) || isShortString(second)) {
      return commonShortStringType(first, second);
    } else if (firstBase.is(IntrinsicType.UNICODESTRING)) {
      return first;
    } else if (secondBase.is(IntrinsicType.UNICODESTRING)) {
      return second;
    } else if (firstBase.is(IntrinsicType.WIDESTRING)) {
      return first;
    } else if (secondBase.is(IntrinsicType.WIDESTRING)) {
      return second;
    } else if (firstBase.is(IntrinsicType.WIDECHAR) || secondBase.is(IntrinsicType.WIDECHAR)) {
      return typeFactory.getIntrinsic(IntrinsicType.UNICODESTRING);
    } else {
      return firstBase.isAnsiString() ? first : second;
    }
  }

  private Type commonShortStringType(Type first, Type second) {
    Type shortString = isShortString(first) ? first : second;
    Type other = isShortString(first) ? second : first;
    Type otherBase = TypeUtils.findBaseType(other);

    if (otherBase.is(IntrinsicType.ANSICHAR)) {
      return shortString;
    }
    if (otherBase.is(IntrinsicType.UNICODESTRING)) {
      return other;
    }
    return unknownType();
  }

  private static boolean isNumeric(Type type) {
    return type.isInteger() || type.isReal();
  }

  private static boolean isFloatingPoint(Type type) {
    type = unalias(type);
    return type.isReal() && !type.is(IntrinsicType.COMP) && !type.is(IntrinsicType.CURRENCY);
  }

  private static boolean isBooleanBased(Type type) {
    return TypeUtils.findBaseType(type).isBoolean();
  }

  private static boolean isTextual(Type type) {
    type = TypeUtils.findBaseType(type);
    return type.isString() || type.isChar();
  }

  private static boolean isShortString(Type type) {
    return TypeUtils.findBaseType(type).is(IntrinsicType.SHORTSTRING);
  }
}
