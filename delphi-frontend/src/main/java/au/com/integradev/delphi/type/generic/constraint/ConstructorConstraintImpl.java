/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.type.generic.constraint;

import org.sonar.plugins.communitydelphi.api.ast.Visibility.VisibilityType;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Constraint.ConstructorConstraint;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;

public class ConstructorConstraintImpl extends ConstraintImpl implements ConstructorConstraint {
  private static final ConstructorConstraintImpl INSTANCE = new ConstructorConstraintImpl();

  private ConstructorConstraintImpl() {
    // Hide constructor
  }

  @Override
  protected ConstraintCheckResult check(Type type) {
    if (type.isDynamicArray()
        || type.isFixedArray()
        || (type.isPointer() && ((PointerType) type).isNilPointer())
        || declaresPublicDefaultConstructor(type)) {
      return ConstraintCheckResult.SATISFIED;
    } else {
      return ConstraintCheckResult.VIOLATED;
    }
  }

  @Override
  protected ConstraintCheckResult check(ClassConstraint constraint) {
    return ConstraintCheckResult.COMPATIBLE;
  }

  @Override
  protected ConstraintCheckResult check(ConstructorConstraint constraint) {
    return ConstraintCheckResult.SATISFIED;
  }

  @Override
  protected ConstraintCheckResult check(RecordConstraint constraint) {
    return ConstraintCheckResult.VIOLATED;
  }

  private static boolean declaresPublicDefaultConstructor(Type type) {
    while (type.isClass()) {
      VisibilityType constructorVisibility =
          ((ScopedType) type)
              .typeScope().getRoutineDeclarations().stream()
                  .filter(ConstructorConstraintImpl::isDefaultConstructor)
                  .map(RoutineNameDeclaration::getVisibility)
                  .findFirst()
                  .orElse(null);

      if (constructorVisibility != null) {
        return constructorVisibility == VisibilityType.PUBLIC;
      }

      type = type.parent();
    }
    return false;
  }

  private static boolean isDefaultConstructor(RoutineNameDeclaration routine) {
    return routine.getRoutineKind() == RoutineKind.CONSTRUCTOR
        && routine.getParametersCount() == 0
        && routine.getName().equalsIgnoreCase("Create");
  }

  public static ConstructorConstraintImpl instance() {
    return INSTANCE;
  }
}
