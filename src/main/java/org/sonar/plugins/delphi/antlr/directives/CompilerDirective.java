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
package org.sonar.plugins.delphi.antlr.directives;

/**
 * Compiler directive interface
 */
public interface CompilerDirective {

  /**
   * example: <code>#include &lt;iostream&gt;</code> will return "include"
   * 
   * @return definition name
   */
  String getName();

  /**
   * example: <code>#include &lt;iostream&gt;</code> will return "iostream"
   * 
   * @return definition item
   */
  String getItem();

  /**
   * @return first definition char position in whole file
   */
  int getFirstCharPosition();

  /**
   * @return last definition char position
   */
  int getLastCharPosition();

  /**
   * @return preprocessor definition type
   */
  CompilerDirectiveType getType();

  /**
   * Get length of the directive, that is: the span from first and last char
   * position
   * 
   * @return directive length
   */
  int getLength();

}
