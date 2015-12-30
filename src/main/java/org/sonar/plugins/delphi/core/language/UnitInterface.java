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

import java.util.Set;

/**
 * Delphi Unit (unit, library keywords) interface
 * 
 * @author SG0214809
 * 
 */
public interface UnitInterface {

  /**
   * Gets unit file name
   * 
   * @return File name
   */
  String getFileName();

  /**
   * Sets unit absolute path to file
   * 
   * @param path Path to file
   */
  void setPath(String path);

  /**
   * Gets unit absolute path
   * 
   * @return Absolute path to file
   */
  String getPath();

  /**
   * Adds class to unit
   * 
   * @param cl Added class
   */
  void addClass(ClassInterface cl);

  /**
   * Gets array of unit classes
   * 
   * @return Array of ClassInterface
   */
  ClassInterface[] getClasses();

  /**
   * Add global function to unit
   * 
   * @param func Global function
   */
  void addFunction(FunctionInterface func);

  /**
   * Get unit global functions
   * 
   * @return Function array
   */
  FunctionInterface[] getFunctions();

  /**
   * Gets all functions (classes and global)
   * 
   * @return Array of all functions in a unit
   */
  FunctionInterface[] getAllFunctions();

  /**
   * Adds unit include (by name)
   * 
   * @param includeName Include name
   */
  void addIncludes(String includeName);

  /**
   * Get unit includes (by name)
   * 
   * @return Array of include names
   */
  String[] getIncludes();

  /**
   * Get a array of units, that are included by this unit
   * 
   * @param allUnits Set of all units
   * @return Array of included units, or empty array if none
   */
  Set<UnitInterface> getIncludedUnits(Set<UnitInterface> allUnits);

  /**
   * Tries to find specified class in this unit
   * 
   * @param classShortName Short name (without filename prefix) of class
   * @return Class reference if found, null otherwise
   */
  ClassInterface findClass(String classShortName);

  /**
   * Tries to find specified function in this unit
   * 
   * @param functionShortName Function name (WITH class name prefix, WITHOUT argument list)
   * @return Function reference if found, null otherwise
   */
  FunctionInterface findFunction(String functionShortName);

  /**
   * Checks if unit is including another unit
   * 
   * @param unit Unit to check
   * @return True if unit is included, false otherwise
   */
  boolean isIncluding(UnitInterface unit);

  /**
   * Sets unit line number
   * 
   * @param lineNumber Line number
   */
  void setLine(int lineNumber);

  /**
   * @return unit line number
   */
  int getLine();

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

}
