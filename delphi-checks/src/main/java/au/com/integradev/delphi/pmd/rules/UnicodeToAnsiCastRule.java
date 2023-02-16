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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StringType;
import org.sonar.plugins.communitydelphi.api.type.TypeUtils;

public class UnicodeToAnsiCastRule extends AbstractCastRule {
  @Override
  protected boolean isViolation(Type originalType, Type castType) {
    return isWideTextType(originalType) && isNarrowTextType(castType);
  }

  private static boolean isWideTextType(Type type) {
    return characterSize(type) > 1;
  }

  private static boolean isNarrowTextType(Type type) {
    return characterSize(type) == 1;
  }

  private static int characterSize(Type type) {
    type = TypeUtils.findBaseType(type);
    if (type.isString()) {
      type = ((StringType) type).characterType();
    }
    if (!type.isChar()) {
      type = DelphiType.unknownType();
    }
    return type.size();
  }
}
