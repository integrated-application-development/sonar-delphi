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
package org.sonar.plugins.delphi.metrics.basicmetrics;

abstract class LineContextHandler {

  abstract boolean matchToEnd(Line line, StringBuilder pendingLine);

  abstract boolean matchWithEndOfLine(Line line, StringBuilder pendingLine);

  abstract boolean matchToBegin(Line line, StringBuilder pendingLine);

  protected boolean matchEndOfString(CharSequence pendingLine, String end) {
    int pendingLineIndex = pendingLine.length() - end.length();
    if (pendingLineIndex < 0) {
      return false;
    }
    for (int endIndex = 0; endIndex < end.length(); endIndex++) {
      char endChar = end.charAt(endIndex);
      char pendingLineChar = pendingLine.charAt(pendingLineIndex + endIndex);
      if (endChar != pendingLineChar) {
        return false;
      }
    }
    return true;
  }

  protected boolean matchEndOfString(StringBuilder pendingLine, char endChar) {
    return pendingLine.length() >= 1 && pendingLine.charAt(pendingLine.length() - 1) == endChar;
  }

  protected char getLastCharacter(StringBuilder pendingLine) {
    if (pendingLine.length() < 1) {
      throw new IllegalStateException("The pending line is empty.");
    }
    return pendingLine.charAt(pendingLine.length() - 1);
  }
}
