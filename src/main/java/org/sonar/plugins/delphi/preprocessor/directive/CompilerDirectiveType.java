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
package org.sonar.plugins.delphi.preprocessor.directive;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.TreeMap;

public enum CompilerDirectiveType {
  DEFINE("define"),
  UNDEFINE("undef"),
  IFDEF("ifdef"),
  IFNDEF("ifndef"),
  IFOPT("ifopt"),
  IF("if"),
  ELSE("else"),
  ELSEIF("elseif"),
  ENDIF("endif"),
  IFEND("ifend"),
  INCLUDE("include", "i"),
  SCOPEDENUMS("scopedenums"),
  POINTERMATH("pointermath"),
  HINTS("hints"),
  WARNINGS("warnings"),
  WARN("warn"),
  UNSUPPORTED;

  private static final Map<String, CompilerDirectiveType> TYPES_BY_NAME;

  static {
    TYPES_BY_NAME = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    CompilerDirectiveType[] values = CompilerDirectiveType.values();
    for (CompilerDirectiveType type : values) {
      for (String name : type.names) {
        TYPES_BY_NAME.put(name, type);
      }
    }
  }

  private final ImmutableSet<String> names;

  CompilerDirectiveType(String... names) {
    this.names = ImmutableSet.copyOf(names);
  }

  /**
   * Returns directive type by name
   *
   * @param directiveName directive name
   * @return directive type with given name
   */
  public static CompilerDirectiveType getTypeByName(String directiveName) {
    return TYPES_BY_NAME.getOrDefault(directiveName, UNSUPPORTED);
  }
}
