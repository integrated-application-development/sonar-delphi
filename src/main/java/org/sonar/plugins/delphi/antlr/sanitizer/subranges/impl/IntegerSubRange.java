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
 * Class used for checking, if provided value is in specified range. Used to
 * parse quotes, comments and definitions.
 */
public class IntegerSubRange implements SubRange {

  private int begin;
  private int end;

  /**
   * Constructor
   * 
   * @param rangeStart Range start
   * @param rangeEnd Range end
   */
  public IntegerSubRange(int rangeStart, int rangeEnd) {
    begin = rangeStart;
    end = rangeEnd;
    if (begin > end) {
      throw new IllegalArgumentException("begin (" + begin + ") > end (" + end + ")");
    }
  }

  /**
   * Checks if value is in range
   * 
   * @param value Value to check
   * @return True if value &gt;= begin &nbsp;&nbsp; value &lt;= end, false otherwise
   */

  @Override
  public boolean inRange(int value) {
    return value >= begin && value <= end;
  }

  /**
   * Checks if range is in scope of current range
   * 
   * @param range Range to check
   * @return True if range includes itself in current range, false otherwise
   */

  @Override
  public boolean inRange(SubRange range) {
    return range.getBegin() >= begin && range.getEnd() <= end;
  }

  /**
   * Get beginning of the range
   * 
   * @return Beggining of the range
   */

  @Override
  public int getBegin() {
    return begin;
  }

  /**
   * Get the end of the range
   * 
   * @return End of the range
   */

  @Override
  public int getEnd() {
    return end;
  }

  @Override
  public String toString() {
    return "[" + begin + ", " + end + "]";
  }

  @Override
  public void setEnd(int value) {
    if (value < begin) {
      throw new IllegalArgumentException("Cannot set range end value less than begin value.");
    }
    end = value;
  }

  @Override
  public void setBegin(int value) {
    if (value > end) {
      throw new IllegalArgumentException("Cannot set range begin value grater than end value");
    }
    begin = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (obj instanceof SubRange) {
      SubRange range = (SubRange) obj;
      return range.getBegin() == this.begin && range.getEnd() == this.end;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
