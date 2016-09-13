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
package org.sonar.squid.text.delphi;

public class LiteralValueHandler extends LineContextHandler {

  private final char delimiter;

  public LiteralValueHandler(char delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  boolean matchToEnd(Line line, StringBuilder pendingLine) {
    return matchEndOfString(pendingLine, delimiter) && evenNumberOfBackSlashBeforeDelimiter(pendingLine);
  }

  private boolean evenNumberOfBackSlashBeforeDelimiter(StringBuilder pendingLine) {
    int numberOfBackSlashChar = 0;
    for (int index = pendingLine.length() - 2; index >= 0; index--) {
      if (pendingLine.charAt(index) == '\\') {
        numberOfBackSlashChar++;
      } else {
        break;
      }
    }
    return numberOfBackSlashChar % 2 == 0;
  }

  @Override
  boolean matchToBegin(Line line, StringBuilder pendingLine) {
      return matchEndOfString(pendingLine, delimiter);
  }

  @Override
  boolean matchWithEndOfLine(Line line, StringBuilder pendingLine) {
    // see http://jira.codehaus.org/browse/SONAR-1555
    return true;
  }
}
