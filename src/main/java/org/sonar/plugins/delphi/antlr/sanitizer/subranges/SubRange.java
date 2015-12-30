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
package org.sonar.plugins.delphi.antlr.sanitizer.subranges;

/**
 * Sub range interface
 */
public interface SubRange {

  /**
   * Checks if value is in range
   * 
   * @param value Value to check
   * @return True if value &gt;= begin &nbsp;&nbsp; value &lt;= end, false otherwise
   */
  boolean inRange(int value);

  /**
   * Checks if range is in scope of current range
   * 
   * @param range Range to check
   * @return True if range includes itself in current range, false otherwise
   */
  boolean inRange(SubRange range);

  /**
   * Get beginning of the range
   * 
   * @return Beggining of the range
   */
  int getBegin();

  /**
   * Get the end of the range
   * 
   * @return End of the range
   */
  int getEnd();

  /**
   * sets end
   * 
   * @param value new end value
   */
  void setEnd(int value);

  /**
   * sets begin
   * 
   * @param value new begin value
   */
  void setBegin(int value);

}
