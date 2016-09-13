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

import java.util.HashMap;
import java.util.Map;

/**
 * Enum type for compiler directive Put every directive you don't use into
 * UNUSED name string - it will automaticaly supress warnings
 */
public enum CompilerDirectiveType {
  UNKNOWN(0, "unknown_directive"),
  DEFINE(1, "define"),
  UNDEFINE(2, "undef"),
  IF(3, "if"),
  ELSE(4, "else,elseif"),
  ENDIF(5, "endif"),
  IFDEF(6, "ifdef,ifndef"),
  IFEND(7, "ifend"),
  INCLUDE(8, "include,i"),
  UNUSED(100, "warn,r,h+,h-,i+,i-,m+,m-");

  private int number;
  private String name;
  private static Map<String, CompilerDirectiveType> mappedValues = null;

  /**
   * create a hash map for faster values lookup
   */
  static {
    mappedValues = new HashMap<String, CompilerDirectiveType>();
    CompilerDirectiveType[] values = CompilerDirectiveType.values();
    for (CompilerDirectiveType type : values) {
      String names[] = type.getName().split(",");
      for (String name : names) {
        mappedValues.put(name, type);
      }
    }
  }

  /**
   * @return directive name
   */
  public String getName() {
    return name;
  }

  /**
   * @return directive number
   */
  public int getNumber() {
    return number;
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

  CompilerDirectiveType(int number, String name) {
    this.number = number;
    this.name = name;
  }
}
