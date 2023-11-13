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
package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;

@Rule(key = "NonLinearCast")
public class NonLinearCastCheck extends AbstractCastCheck {
  @Override
  protected boolean isViolation(Type originalType, Type castType) {
    if (originalType.isStruct() && castType.isStruct()) {
      return !isValidStructToStructCast((StructType) originalType, (StructType) castType);
    } else if (originalType.isStruct() && castType.isPointer()) {
      return !isValidStructToPointerCast((StructType) originalType, (PointerType) castType);
    } else {
      return false;
    }
  }

  private static boolean isValidStructToStructCast(StructType from, StructType to) {
    if (!from.isClass() || !to.isClass()) {
      return true;
    } else {
      return from.is(to) || from.isDescendantOf(to) || to.isDescendantOf(from);
    }
  }

  private static boolean isValidStructToPointerCast(StructType from, PointerType to) {
    if (!from.isClass() || to.isUntypedPointer() || !to.dereferencedType().isClass()) {
      return true;
    } else {
      return isValidStructToStructCast(from, (StructType) to.dereferencedType());
    }
  }

  @Override
  protected String getIssueMessage() {
    return "Remove this unsafe object cast.";
  }
}
