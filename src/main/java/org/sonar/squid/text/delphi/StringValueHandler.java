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

import org.sonar.squidbridge.measures.Metric;

public class StringValueHandler extends LineContextHandler {

  private static final int ALNUM_BEGIN_INDEX = 48;
  private String str = null;
  private Metric metric = null;
  private boolean isMatch = false;

  StringValueHandler(String value, Metric metric) {
    str = value.toLowerCase();
    this.metric = metric;
  }

  boolean isWhitespaceOrIsNotAlnum(char c) {
    boolean whiteSpace = (c == '\n') || (c == ' ') || (c == '\r') || (c == '\0');
    boolean notAlnum = c < ALNUM_BEGIN_INDEX;
    return whiteSpace || notAlnum;
  }

  @Override
  boolean matchToEnd(Line line, StringBuilder pendingLine) {
    if (str == null) {
      throw new IllegalStateException("Method StringValueHandler::matchToEnd has no string value.");
    }

    if (isMatch && isWhitespaceOrIsNotAlnum(pendingLine.charAt(pendingLine.length() - 1))) {
      line.setMeasure(metric, line.getInt(metric) + 1);
    }

    // always returns true, even if not found to finish checking further
    return true;
  }

  @Override
  boolean matchWithEndOfLine(Line line, StringBuilder pendingLine) {
    return matchToEnd(line, pendingLine);
  }

  @Override
  boolean matchToBegin(Line line, StringBuilder pendingLine) {
    if (str == null) {
      throw new IllegalStateException("Method StringValueHandler::matchToBegin has no string value.");
    }
    isMatch = matchEndOfString(pendingLine.toString().toLowerCase(), str);
    return isMatch;
  }

}
