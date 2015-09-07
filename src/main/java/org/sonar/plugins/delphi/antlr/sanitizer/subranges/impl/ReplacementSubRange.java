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

/**
 * Used to replace a specific range with a specific string
 */
public class ReplacementSubRange extends IntegerSubRange {

  private String replacementString = null;

  /**
   * ctor
   * 
   * @param rangeStart start index
   * @param rangeEnd end index
   * @param str string, which we will replace with
   */
  public ReplacementSubRange(int rangeStart, int rangeEnd, String str) {
    super(rangeStart, rangeEnd);
    replacementString = str;
  }

  @Override
  public String toString() {
    return replacementString;
  }

}
