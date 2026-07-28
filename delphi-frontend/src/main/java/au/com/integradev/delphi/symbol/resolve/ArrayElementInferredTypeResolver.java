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
import java.math.BigInteger;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

/** Resolves the element type the compiler infers for an array literal. */
final class ArrayElementInferredTypeResolver {
  private final TypeFactory typeFactory;

  ArrayElementInferredTypeResolver(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }

  Type elementType(List<Type> elements) {
    return elements.stream().map(this::normalize).reduce(this::combine).orElse(unknownType());
  }

  private Type normalize(Type type) {
    if (type.isReal()) {
      return typeFactory.getIntrinsic(IntrinsicType.EXTENDED);
    }
    if (isNilPointer(type)) {
      return typeFactory.getIntrinsic(IntrinsicType.POINTER);
    }
    return type;
  }

  private Type combine(Type first, Type second) {
    if (first.is(second)) {
      return first;
    }

    if (first.isInteger() && second.isInteger()) {
      return combineInteger((IntegerType) first, (IntegerType) second);
    }

    if (isTextual(first) && isTextual(second)) {
      return typeFactory.getIntrinsic(IntrinsicType.UNICODESTRING);
    }

    if (isBooleanBased(first) && isBooleanBased(second)) {
      return typeFactory.getIntrinsic(IntrinsicType.BOOLEAN);
    }

    Type firstBase = TypeUtils.findBaseType(first);
    if (firstBase.isEnum() && firstBase.is(TypeUtils.findBaseType(second))) {
      return firstBase;
    }

    if ((first.isClass() && second.isClass()) || (first.isInterface() && second.isInterface())) {
      if (second.isDescendantOf(first)) {
        return first;
      }
      if (first.isDescendantOf(second)) {
        return second;
      }
      return rootOf(first);
    }

    if (first.isClassReference() && second.isClassReference()) {
      return combineClassReference(first, second);
    }

    if (first.isPointer() && second.isPointer()) {
      return typeFactory.getIntrinsic(IntrinsicType.POINTER);
    }

    return unknownType();
  }

  private Type combineInteger(IntegerType first, IntegerType second) {
    IntegerType left = promote(first);
    IntegerType right = promote(second);
    if (left.is(right)) {
      return left;
    }

    BigInteger min = left.min().min(right.min());
    BigInteger max = left.max().max(right.max());
    for (IntrinsicType candidate :
        List.of(
            IntrinsicType.INTEGER,
            IntrinsicType.CARDINAL,
            IntrinsicType.INT64,
            IntrinsicType.UINT64)) {
      IntegerType type = (IntegerType) typeFactory.getIntrinsic(candidate);
      if (type.min().compareTo(min) <= 0 && type.max().compareTo(max) >= 0) {
        return type;
      }
    }

    return typeFactory.getIntrinsic(IntrinsicType.UINT64);
  }

  private IntegerType promote(IntegerType type) {
    if (type.size() < 4) {
      return (IntegerType) typeFactory.getIntrinsic(IntrinsicType.INTEGER);
    }
    return type;
  }

  private static Type combineClassReference(Type first, Type second) {
    Type firstClass = ((ClassReferenceType) first).classType();
    Type secondClass = ((ClassReferenceType) second).classType();
    if (secondClass.isDescendantOf(firstClass)) {
      return first;
    }
    if (firstClass.isDescendantOf(secondClass)) {
      return second;
    }
    return unknownType();
  }

  private static Type rootOf(Type type) {
    Type root = type;
    for (Type parent = root.parent(); !parent.isUnknown(); parent = parent.parent()) {
      root = parent;
    }
    return root;
  }

  private static boolean isNilPointer(Type type) {
    return type.isPointer() && ((PointerType) type).isNilPointer();
  }

  private static boolean isTextual(Type type) {
    Type base = TypeUtils.findBaseType(type);
    return base.isString() || base.isChar();
  }

  private static boolean isBooleanBased(Type type) {
    return TypeUtils.findBaseType(type).isBoolean();
  }
}
