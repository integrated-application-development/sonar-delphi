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

package org.sonar.plugins.delphi.pmd;

import java.util.Comparator;

class RuleData {

  private String name;
  private int line;

  public RuleData(String _name, int _line) {
    name = _name;
    line = _line;
  }

  public String getName() {
    return name;
  }

  public int getLine() {
    return line;
  }

  @Override
  public String toString() {
    return "RuleData [name=" + name + ", line=" + line + "]";
  }

  public static Comparator<RuleData> getComparator() {
    return new Comparator<RuleData>() {

      @Override
      public int compare(RuleData o1, RuleData o2) {
        return o1.getLine() - o2.getLine();
      }
    };
  }
}
