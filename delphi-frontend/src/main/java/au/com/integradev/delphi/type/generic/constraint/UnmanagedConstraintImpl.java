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
package au.com.integradev.delphi.type.generic.constraint;

import java.util.HashSet;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Constraint.UnmanagedConstraint;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;

public class UnmanagedConstraintImpl extends ConstraintImpl implements UnmanagedConstraint {
  private static final UnmanagedConstraintImpl INSTANCE = new UnmanagedConstraintImpl();

  private UnmanagedConstraintImpl() {
    // Hide constructor
  }

  @Override
  protected ConstraintCheckResult check(Type type) {
    return isUnmanaged(type, new HashSet<>())
        ? ConstraintCheckResult.SATISFIED
        : ConstraintCheckResult.VIOLATED;
  }

  private static boolean isUnmanaged(Type type, Set<Type> visited) {
    if (type.isBoolean()
        || type.isReal()
        || type.isInteger()
        || type.isEnum()
        || type.isSubrange()
        || type.isChar()
        || type.isPointer()) {
      return true;
    }
    if (type.isRecord()) {
      if (!visited.add(type)) {
        return true;
      }
      for (VariableNameDeclaration field :
          ((ScopedType) type).typeScope().getVariableDeclarations()) {
        if (field.isField() && !field.isClassVar() && !isUnmanaged(field.getType(), visited)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  @SuppressWarnings("overloads")
  protected ConstraintCheckResult check(ClassConstraint constraint) {
    return ConstraintCheckResult.VIOLATED;
  }

  @Override
  @SuppressWarnings("overloads")
  protected ConstraintCheckResult check(ConstructorConstraint constraint) {
    return ConstraintCheckResult.VIOLATED;
  }

  @Override
  @SuppressWarnings("overloads")
  protected ConstraintCheckResult check(RecordConstraint constraint) {
    return ConstraintCheckResult.VIOLATED;
  }

  @Override
  @SuppressWarnings("overloads")
  protected ConstraintCheckResult check(InterfaceConstraint constraint) {
    return ConstraintCheckResult.VIOLATED;
  }

  @Override
  @SuppressWarnings("overloads")
  protected ConstraintCheckResult check(UnmanagedConstraint constraint) {
    return ConstraintCheckResult.SATISFIED;
  }

  public static UnmanagedConstraintImpl instance() {
    return INSTANCE;
  }
}
