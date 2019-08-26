/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.antlr.directives;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;

public enum CompilerDirectiveType {
  UNKNOWN("unknown_directive"),
  DEFINE("define"),
  UNDEFINE("undef"),
  IF("if"),
  ELSE("else", "elseif"),
  ENDIF("endif"),
  IFDEF("ifdef", "ifndef"),
  IFEND("ifend"),
  INCLUDE("include", "i"),
  UNSUPPORTED("warn", "r", "h+", "h-", "i+", "i-", "m+", "m-");

  private final ImmutableSet<String> names;
  private static final Map<String, CompilerDirectiveType> mappedValues;

  // create a hash map for faster values lookup
  static {
    mappedValues = new HashMap<>();
    CompilerDirectiveType[] values = CompilerDirectiveType.values();
    for (CompilerDirectiveType type : values) {
      for (String name : type.names) {
        mappedValues.put(name, type);
      }
    }
  }

  CompilerDirectiveType(String... names) {
    this.names = ImmutableSet.copyOf(names);
  }

  /**
   * @param directiveName directive name
   * @return directive type with given name
   */
  public static CompilerDirectiveType getTypeByName(String directiveName) {
    if (!mappedValues.containsKey(directiveName)) {
      return UNKNOWN;
    }
    return mappedValues.get(directiveName);
  }
}
