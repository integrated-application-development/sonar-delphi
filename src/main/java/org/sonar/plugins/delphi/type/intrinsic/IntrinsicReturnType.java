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
package org.sonar.plugins.delphi.type.intrinsic;

import java.util.List;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

public abstract class IntrinsicReturnType extends DelphiType {
  @Override
  public String getImage() {
    return "<" + getClass().getSimpleName() + ">";
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  public abstract Type getReturnType(List<Type> arguments);

  public static Type high(TypeFactory typeFactory) {
    return new HighLowReturnType(typeFactory);
  }

  public static Type low(TypeFactory typeFactory) {
    return new HighLowReturnType(typeFactory);
  }

  public static Type round(TypeFactory typeFactory) {
    return new RoundTruncReturnType("Round", typeFactory);
  }

  public static Type trunc(TypeFactory typeFactory) {
    return new RoundTruncReturnType("Trunc", typeFactory);
  }

  public static Type classReferenceValue() {
    return new ClassReferenceValueType();
  }

  private static class HighLowReturnType extends IntrinsicReturnType {
    private final Type integerType;

    private HighLowReturnType(TypeFactory typeFactory) {
      this.integerType = typeFactory.getIntrinsic(IntrinsicType.INTEGER);
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);

      if (type.isClassReference()) {
        type = ((ClassReferenceType) type).classType();
      }

      if (type.isArray() || type.isString()) {
        type = integerType;
      }

      return type;
    }
  }

  private static class RoundTruncReturnType extends IntrinsicReturnType {
    private final String name;
    private final Type int64Type;

    private RoundTruncReturnType(String name, TypeFactory typeFactory) {
      this.name = name;
      this.int64Type = typeFactory.getIntrinsic(IntrinsicType.INT64);
    }

    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);
      if (type.isRecord()) {
        return ((ScopedType) type)
            .typeScope().getMethodDeclarations().stream()
                .filter(method -> method.getMethodKind() == MethodKind.OPERATOR)
                .filter(method -> method.getImage().equalsIgnoreCase(name))
                .map(MethodNameDeclaration::getReturnType)
                .findFirst()
                .orElseGet(DelphiType::unknownType);
      }
      return int64Type;
    }
  }

  private static class ClassReferenceValueType extends IntrinsicReturnType {
    @Override
    public Type getReturnType(List<Type> arguments) {
      Type type = arguments.get(0);
      if (type.isClassReference()) {
        return ((ClassReferenceType) type).classType();
      }
      return unknownType();
    }
  }
}
