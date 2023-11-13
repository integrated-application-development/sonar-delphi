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
package au.com.integradev.delphi.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;

public final class InterfaceUtils {
  private InterfaceUtils() {
    // Utility class
  }

  public static boolean implementsMethodOnInterface(MethodNameDeclaration method) {
    var typeDeclaration = method.getTypeDeclaration();
    return typeDeclaration != null && hasMatchingInterfaceMethod(typeDeclaration.getType(), method);
  }

  public static Set<MethodNameDeclaration> findImplementedInterfaceMethodDeclarations(
      MethodNameDeclaration method) {
    var typeDeclaration = method.getTypeDeclaration();
    if (typeDeclaration == null) {
      return new HashSet<>();
    }

    Set<MethodNameDeclaration> implementedMethods = new HashSet<>();

    var interfaces =
        typeDeclaration.getType().ancestorList().stream()
            .filter(Type::isInterface)
            .map(ScopedType.class::cast)
            .collect(Collectors.toUnmodifiableList());

    for (var interfaceType : interfaces) {
      interfaceType.typeScope().getMethodDeclarations().stream()
          .filter(interfaceMethod -> hasSameMethodSignature(method, interfaceMethod))
          .forEach(implementedMethods::add);
    }

    return implementedMethods;
  }

  private static boolean hasMatchingInterfaceMethod(Type type, MethodNameDeclaration method) {
    if (type.isInterface()
        && ((ScopedType) type)
            .typeScope().getMethodDeclarations().stream()
                .anyMatch(interfaceMethod -> hasSameMethodSignature(method, interfaceMethod))) {
      return true;
    }

    return type.ancestorList().stream()
        .anyMatch(parentType -> hasMatchingInterfaceMethod(parentType, method));
  }

  private static boolean hasSameMethodSignature(
      MethodNameDeclaration thisMethod, MethodNameDeclaration overriddenMethod) {
    return thisMethod.getName().equalsIgnoreCase(overriddenMethod.getName())
        && thisMethod.hasSameParameterTypes(overriddenMethod);
  }
}
