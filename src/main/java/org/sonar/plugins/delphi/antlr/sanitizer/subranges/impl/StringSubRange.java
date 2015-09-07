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
package org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl;

import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRange;

/**
 * Sub range class that contains a string
 */
public class StringSubRange extends IntegerSubRange implements SubRange {

  private String fullString;
  private String subString;
  private boolean dirty;

  /**
   * Ctor.
   * 
   * @param rangeStart Start of substring
   * @param rangeEnd End of substring
   * @param str The string
   */
  public StringSubRange(int rangeStart, int rangeEnd, String str) {
    super(rangeStart, rangeEnd);
    fullString = str;
    dirty = true;
    refreshString();
  }

  @Override
  public String toString() {
    if (subString == null) {
      return super.toString();
    } else {
      refreshString();
      return super.toString() + " " + subString;
    }
  }

  @Override
  public void setBegin(int value) {
    super.setBegin(Math.max(0, value));
    dirty = true;
  }

  @Override
  public void setEnd(int value) {
    super.setEnd(Math.min(fullString.length(), value));
    dirty = true;
  }

  private void refreshString() {
    if (dirty && fullString != null) {
      subString = fullString.substring(getBegin(), getEnd());
      dirty = false;
    }
  }

}
