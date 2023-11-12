/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.type;

import au.com.integradev.delphi.type.factory.UnknownTypeImpl;
import au.com.integradev.delphi.type.factory.UntypedTypeImpl;
import au.com.integradev.delphi.type.factory.VoidTypeImpl;
import java.math.BigInteger;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.type.Type.AnsiStringType;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.FileType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.StrongAliasType;
import org.sonar.plugins.communitydelphi.api.type.Type.SubrangeType;

public interface TypeFactory {

  static Type unknownType() {
    return UnknownTypeImpl.instance();
  }

  static Type untypedType() {
    return UntypedTypeImpl.instance();
  }

  static Type voidType() {
    return VoidTypeImpl.instance();
  }

  Type getIntrinsic(IntrinsicType intrinsic);

  AnsiStringType ansiString(int codePage);

  ArrayConstructorType arrayConstructor(List<Type> types);

  CollectionType set(Type type);

  CollectionType emptySet();

  SubrangeType subRange(String image, Type type);

  PointerType pointerTo(@Nullable String image, Type type);

  PointerType untypedPointer();

  PointerType nilPointer();

  FileType fileOf(Type type);

  FileType untypedFile();

  ClassReferenceType classOf(@Nullable String image, Type type);

  StrongAliasType strongAlias(String image, Type aliased);

  IntegerType integerFromLiteralValue(BigInteger value);
}
