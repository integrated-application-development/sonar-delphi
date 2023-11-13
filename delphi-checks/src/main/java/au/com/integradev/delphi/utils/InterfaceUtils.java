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
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;

public final class InterfaceUtils {
  private InterfaceUtils() {
    // Utility class
  }

  public static boolean implementsMethodOnInterface(RoutineNameDeclaration routine) {
    var typeDeclaration = routine.getTypeDeclaration();
    return typeDeclaration != null
        && hasMatchingInterfaceMethod(typeDeclaration.getType(), routine);
  }

  public static Set<RoutineNameDeclaration> findImplementedInterfaceMethodDeclarations(
      RoutineNameDeclaration routine) {
    var typeDeclaration = routine.getTypeDeclaration();
    if (typeDeclaration == null) {
      return new HashSet<>();
    }

    Set<RoutineNameDeclaration> implementedMethods = new HashSet<>();

    var interfaces =
        typeDeclaration.getType().ancestorList().stream()
            .filter(Type::isInterface)
            .map(ScopedType.class::cast)
            .collect(Collectors.toUnmodifiableList());

    for (var interfaceType : interfaces) {
      interfaceType.typeScope().getRoutineDeclarations().stream()
          .filter(interfaceMethod -> hasSameSignature(routine, interfaceMethod))
          .forEach(implementedMethods::add);
    }

    return implementedMethods;
  }

  private static boolean hasMatchingInterfaceMethod(Type type, RoutineNameDeclaration routine) {
    if (type.isInterface()
        && ((ScopedType) type)
            .typeScope().getRoutineDeclarations().stream()
                .anyMatch(interfaceMethod -> hasSameSignature(routine, interfaceMethod))) {
      return true;
    }

    return type.ancestorList().stream()
        .anyMatch(parentType -> hasMatchingInterfaceMethod(parentType, routine));
  }

  private static boolean hasSameSignature(
      RoutineNameDeclaration method, RoutineNameDeclaration overriddenMethod) {
    return method.getName().equalsIgnoreCase(overriddenMethod.getName())
        && method.hasSameParameterTypes(overriddenMethod);
  }
}
