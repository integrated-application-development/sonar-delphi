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
package au.com.integradev.delphi.type;

import java.util.Collections;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public abstract class TypeImpl implements Type {
  @Override
  public Type parent() {
    return TypeFactory.unknownType();
  }

  @Override
  public Set<Type> ancestorList() {
    return Collections.emptySet();
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    return false;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    return this;
  }

  @Override
  public boolean is(String image) {
    return getImage().equalsIgnoreCase(image);
  }

  @Override
  public final boolean is(Type type) {
    return is(type.getImage());
  }

  @Override
  public final boolean is(IntrinsicType intrinsic) {
    return is(intrinsic.fullyQualifiedName());
  }

  @Override
  public boolean isDescendantOf(String image) {
    return false;
  }

  @Override
  public boolean isDescendantOf(Type type) {
    return isDescendantOf(type.getImage());
  }

  @Override
  public boolean isUntyped() {
    return false;
  }

  @Override
  public boolean isUnknown() {
    return false;
  }

  @Override
  public boolean isUnresolved() {
    return false;
  }

  @Override
  public boolean isVoid() {
    return false;
  }

  @Override
  public boolean isInteger() {
    return false;
  }

  @Override
  public boolean isReal() {
    return false;
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  @Override
  public boolean isString() {
    return false;
  }

  @Override
  public boolean isAnsiString() {
    return false;
  }

  @Override
  public boolean isChar() {
    return false;
  }

  @Override
  public boolean isStruct() {
    return false;
  }

  @Override
  public boolean isClass() {
    return false;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isRecord() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  @Override
  public boolean isSubrange() {
    return false;
  }

  @Override
  public boolean isFile() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isFixedArray() {
    return false;
  }

  @Override
  public boolean isDynamicArray() {
    return false;
  }

  @Override
  public boolean isOpenArray() {
    return false;
  }

  @Override
  public boolean isPointer() {
    return false;
  }

  @Override
  public boolean isSet() {
    return false;
  }

  @Override
  public boolean isProcedural() {
    return false;
  }

  @Override
  public boolean isRoutine() {
    return false;
  }

  @Override
  public boolean isClassReference() {
    return false;
  }

  @Override
  public boolean isVariant() {
    return false;
  }

  @Override
  public boolean isOleVariant() {
    return false;
  }

  @Override
  public boolean isAlias() {
    return false;
  }

  @Override
  public boolean isWeakAlias() {
    return false;
  }

  @Override
  public boolean isStrongAlias() {
    return false;
  }

  @Override
  public boolean isArrayConstructor() {
    return false;
  }

  @Override
  public boolean isArrayOfConst() {
    return false;
  }

  @Override
  public boolean isHelper() {
    return false;
  }

  @Override
  public boolean isTypeParameter() {
    return false;
  }

  @Override
  public String toString() {
    return getImage();
  }
}
