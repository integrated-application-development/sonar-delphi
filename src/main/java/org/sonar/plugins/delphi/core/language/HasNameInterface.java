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
 * For each class that consists of name and realName (real name is not converted
 * to lowercase)
 */
public interface HasNameInterface {

  /**
   * Gets function name (lowercase)
   * 
   * @return Function name
   */
  String getName();

  /**
   * Sets function name
   * 
   * @param name New function name
   */
  void setName(String name);

  /**
   * Gets function real name (not converted to lowercase)
   * 
   * @return Function real name
   */
  String getRealName();

  /**
   * Sets function real name
   * 
   * @param name New function real name
   */
  void setRealName(String name);
}
