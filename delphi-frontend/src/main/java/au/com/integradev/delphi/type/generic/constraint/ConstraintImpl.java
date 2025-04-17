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

import org.sonar.plugins.communitydelphi.api.type.Constraint;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;

abstract class ConstraintImpl implements Constraint {
  protected enum ConstraintCheckResult {
    VIOLATED,
    COMPATIBLE,
    SATISFIED
  }

  @Override
  public final boolean satisfiedBy(Type type) {
    if (type instanceof TypeParameterType) {
      ConstraintCheckResult result = ConstraintCheckResult.COMPATIBLE;
      for (Constraint constraint : ((TypeParameterType) type).constraintItems()) {
        switch (check(constraint)) {
          case VIOLATED:
            return false;
          case COMPATIBLE:
            break;
          case SATISFIED:
            result = ConstraintCheckResult.SATISFIED;
            break;
        }
      }
      return result == ConstraintCheckResult.SATISFIED;
    } else {
      return check(type) == ConstraintCheckResult.SATISFIED;
    }
  }

  private ConstraintCheckResult check(Constraint constraint) {
    if (constraint instanceof TypeConstraint) {
      return check(((TypeConstraint) constraint).type());
    }

    if (constraint instanceof ClassConstraint) {
      return check((ClassConstraint) constraint);
    }

    if (constraint instanceof ConstructorConstraint) {
      return check((ConstructorConstraint) constraint);
    }

    if (constraint instanceof RecordConstraint) {
      return check((RecordConstraint) constraint);
    }

    return ConstraintCheckResult.VIOLATED;
  }

  protected abstract ConstraintCheckResult check(Type type);

  @SuppressWarnings("overloads")
  protected abstract ConstraintCheckResult check(ClassConstraint constraint);

  @SuppressWarnings("overloads")
  protected abstract ConstraintCheckResult check(ConstructorConstraint constraint);

  @SuppressWarnings("overloads")
  protected abstract ConstraintCheckResult check(RecordConstraint constraint);
}
