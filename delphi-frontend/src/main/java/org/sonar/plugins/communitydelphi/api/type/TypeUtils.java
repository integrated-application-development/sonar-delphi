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
package org.sonar.plugins.communitydelphi.api.type;

import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.StringType;
import org.sonar.plugins.communitydelphi.api.type.Type.SubrangeType;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeType;

public final class TypeUtils {
  private TypeUtils() {
    // utility class
  }

  public static Type findBaseType(Type type) {
    while (type.isTypeType()) {
      type = ((TypeType) type).originalType();
    }

    if (type.isSubrange()) {
      type = ((SubrangeType) type).hostType();
    }

    return type;
  }

  public static Type dereference(Type type) {
    Type baseType = findBaseType(type);
    if (baseType instanceof PointerType) {
      return ((PointerType) baseType).dereferencedType();
    }
    return type;
  }

  /**
   * Check if the supplied type is a string with single-byte characters
   *
   * @param type The type we're checking
   * @return true if this is a string type with single-byte characters
   */
  public static boolean isNarrowString(Type type) {
    return type.isString() && ((StringType) type).characterType().size() == 1;
  }

  /**
   * Check if the supplied type is a string with multi-byte characters
   *
   * @param type The type we're checking
   * @return true if this is a string type with multi-byte characters
   */
  public static boolean isWideString(Type type) {
    return type.isString() && ((StringType) type).characterType().size() > 1;
  }
}
