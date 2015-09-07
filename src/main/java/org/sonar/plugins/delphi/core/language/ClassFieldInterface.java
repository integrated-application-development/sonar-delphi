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
 * Interface used for class fields (variables).
 */
public interface ClassFieldInterface extends ArgumentInterface {

  /**
   * Sets field parent (class it resides in)
   * 
   * @param cl Parent class
   */
  void setParent(ClassInterface cl);

  /**
   * Get parent (class it resides in)
   * 
   * @return Parent class
   */
  ClassInterface getParent();

  /**
   * @return Fields visibility (, protected or private)
   */
  int getVisibility();

  /**
   * Sets class field visibility
   * 
   * @param value , protected of private
   */
  void setVisibility(int value);
}
