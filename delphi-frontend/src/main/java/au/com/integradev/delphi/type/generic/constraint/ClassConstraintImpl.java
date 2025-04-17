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

import org.sonar.plugins.communitydelphi.api.type.Constraint.ClassConstraint;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;

public class ClassConstraintImpl extends ConstraintImpl implements ClassConstraint {
  private static final ClassConstraintImpl INSTANCE = new ClassConstraintImpl();

  private ClassConstraintImpl() {
    // Hide constructor
  }

  @Override
  protected ConstraintCheckResult check(Type type) {
    if (type.isClass() || (type.isPointer() && ((PointerType) type).isNilPointer())) {
      return ConstraintCheckResult.SATISFIED;
    } else {
      return ConstraintCheckResult.VIOLATED;
    }
  }

  @Override
  protected ConstraintCheckResult check(ClassConstraint constraint) {
    return ConstraintCheckResult.SATISFIED;
  }

  @Override
  protected ConstraintCheckResult check(ConstructorConstraint constraint) {
    return ConstraintCheckResult.COMPATIBLE;
  }

  @Override
  protected ConstraintCheckResult check(RecordConstraint constraint) {
    return ConstraintCheckResult.VIOLATED;
  }

  public static ClassConstraintImpl instance() {
    return INSTANCE;
  }
}
