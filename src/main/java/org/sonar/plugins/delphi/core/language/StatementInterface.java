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
package org.sonar.plugins.delphi.core.language;

/**
 * Statement interface used to declare language specific statements.
 * 
 */
public interface StatementInterface {

  /**
   * Get statement line
   * 
   * @return Statement line
   */
  int getLine();

  /**
   * Get statement column
   * 
   * @return Statement column
   */
  int getColumn();

  /**
   * Get statement text, example: "if", "else", "x := 5;"
   * 
   * @return The statement text
   */
  String getText();

  /**
   * Get class field associated with this statement
   * 
   * @param fromClass Class, in which to look for fields
   * @return ClassField if statement includes variable, null otherwise
   */
  ClassFieldInterface[] getFields(ClassInterface fromClass);

  /**
   * Sets statement text
   * 
   * @param value Value to set
   */
  void setText(String value);

  /**
   * Set statement line number
   * 
   * @param value Line number
   */
  void setLine(int value);

  /**
   * Set statement column number
   * 
   * @param value Column number
   */
  void setColumn(int value);

  /**
   * Sets if statement is complex statement, example: "x := x + 6;",
   * "y := x * 2.5;" etc
   * 
   * @param isComplex Param to set
   */
  void setComplexity(boolean isComplex);

  /**
   * Shows if the statement is a complex statement or not
   * 
   * @return True if complex statement, false otherwise
   */
  boolean isComplex();
}
