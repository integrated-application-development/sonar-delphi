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
package au.com.integradev.delphi.utils.types;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.type.factory.HelperTypeImpl;
import au.com.integradev.delphi.type.factory.StructTypeImpl;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;

public final class TypeMocker {
  private TypeMocker() {
    // Utility class
  }

  public static StructType struct(String image, StructKind kind) {
    return struct(image, kind, unknownType());
  }

  public static StructType struct(String image, StructKind kind, Type parent) {
    StructType type;

    switch (kind) {
      case CLASS_HELPER:
      case RECORD_HELPER:
        type = mock(HelperTypeImpl.class);
        break;
      default:
        type = mock(StructTypeImpl.class);
    }

    when(type.typeScope()).thenReturn(unknownScope());
    when(type.isStruct()).thenReturn(true);
    when(type.isInterface()).thenReturn(kind == StructKind.INTERFACE);
    when(type.isRecord()).thenReturn(kind == StructKind.RECORD);
    when(type.isHelper())
        .thenReturn(kind == StructKind.RECORD_HELPER || kind == StructKind.CLASS_HELPER);
    when(type.getImage()).thenReturn(image);
    when(type.is(anyString()))
        .thenAnswer(invocation -> image.equalsIgnoreCase(invocation.getArgument(0)));
    when(type.is(any(Type.class)))
        .thenAnswer(
            arguments -> image.equalsIgnoreCase(((Type) arguments.getArgument(0)).getImage()));
    when(type.kind()).thenReturn(kind);
    when(type.isDescendantOf(anyString()))
        .thenAnswer(invocation -> isImageOrParentType(invocation.getArgument(0), image, parent));
    when(type.isDescendantOf(any(Type.class)))
        .thenAnswer(
            invocation ->
                isImageOrParentType(((Type) invocation.getArgument(0)).getImage(), image, parent));
    when(type.parent()).thenReturn(parent);

    return type;
  }

  public static Parameter parameter(Type type) {
    Parameter parameter = mock(Parameter.class);
    when(parameter.getImage()).thenReturn("_");
    when(parameter.getType()).thenReturn(type);
    return parameter;
  }

  private static boolean isImageOrParentType(String typeToCheck, String image, Type parent) {
    return image.equalsIgnoreCase(typeToCheck) || parent.is(typeToCheck);
  }
}
