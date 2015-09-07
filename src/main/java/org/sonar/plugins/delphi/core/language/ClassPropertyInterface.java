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
 * Interface used for class properties (DelphiLanguage style) Class property is
 * a class field, but uses class functions to read and write from another class
 * field. Functions used to read and write are not counted as unused functions.<br>
 * Example:<br>
 * <code>
 * type myClass = class(myAncestor)<br>
 * private<br>
 *  integer age;<br>
 *  function getAge: integer;<br>
 *  procedure setAge(value: integer);<br>
 * published<br>
 *    property personAge : Boolean read getAge write setAge; * <br>
 * end;<br>
 * </code>
 */
public interface ClassPropertyInterface extends ClassFieldInterface {

  /**
   * Gets the read function
   * 
   * @return Read function
   */
  FunctionInterface getReadFunction();

  /**
   * Gets the write function
   * 
   * @return Write function
   */
  FunctionInterface getWriteFunction();

  /**
   * Sets the read function
   * 
   * @param newFunction new read function
   */
  void setReadFunction(FunctionInterface newFunction);

  /**
   * Sets the write function
   * 
   * @param newFunction new write function
   */
  void setWriteFunction(FunctionInterface newFunction);

  /**
   * Checks if property has specified function
   * 
   * @param function Function to check
   * @return True if so, false otherwise
   */
  boolean hasFunction(FunctionInterface function);
}
