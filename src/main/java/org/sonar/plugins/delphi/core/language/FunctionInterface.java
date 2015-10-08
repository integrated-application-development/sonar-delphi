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

import java.util.List;

/**
 * Interface used to create custom, language-dependend functions. Used by
 * AbstractAnalyser.
 */
public interface FunctionInterface extends HasNameInterface {

  /**
   * Is function an accessor (getter or setter)?
   * 
   * @return True if it is, false otherwise
   */
  boolean isAccessor();

  /**
   * Has function a body?
   * 
   * @return true if so, false otherwise
   */
  boolean hasBody();

  /**
   * Sets a function to be only a declaration (with no body)
   * 
   * @param value Value to set
   */
  void setDeclaration(boolean value);

  /**
   * Gets function visibility (public, protected, private)
   * 
   * @return Function visibility
   */
  int getVisibility();

  /**
   * Sets function visibility
   * 
   * @param value New function visibility
   */
  void setVisibility(int value);

  /**
   * Is function a declaration (with no body)?
   * 
   * @return True if it is, false otherwise
   */
  boolean isDeclaration();

  /**
   * Increases function Cyclomatic Complexity by 1
   */
  void increaseComplexity();

  /**
   * Get the number of current function overloads
   * 
   * @return Count of current function overloads
   */
  int getOverloadsCount();

  /**
   * Increase number of function overloads
   */
  void increaseFunctionOverload();

  /**
   * Gets function Cyclomatic Complexity
   * 
   * @return Cyclomatic Complexity
   */
  int getComplexity();

  /**
   * Sets function Cyclomatic Complexity
   * 
   * @param complexity New Cyclomatic Complexity value
   */
  void setComplexity(int complexity);

  /**
   * Gets function declaration line in file
   * 
   * @return Function line
   */
  int getLine();

  /**
   * Gets function implementation line in file
   * 
   * @return Function Body line
   */
  int getBodyLine();

  /**
   * Sets function line
   * 
   * @param line New line
   */
  void setLine(int line);

  /**
   * Sets function implementation line
   * 
   * @param line New body line
   */
  void setBodyLine(int line);

  /**
   * Gets function column in file
   * 
   * @return Function column in file
   */
  int getColumn();

  /**
   * Sets function column in file
   * 
   * @param column New function column
   */
  void setColumn(int column);

  /**
   * Gets function short name (without class name prefix)
   * 
   * @return String, short function name
   */
  String getShortName();

  /**
   * Adds statement to function
   * 
   * @param st Statement to add
   */
  void addStatement(StatementInterface st);

  /**
   * Get list of statements in function
   * 
   * @return List of statements
   */
  List<StatementInterface> getStatements();

  /**
   * Get list of called functions from function body
   * 
   * @return List of functions
   */
  FunctionInterface[] getCalledFunctions();

  /**
   * Adds a function to called function list
   * 
   * @param function Function to add
   */
  void addCalledFunction(FunctionInterface function);

  /**
   * Checks if current function is calls other function
   * 
   * @param calledFunc Function to check
   * @return True if current function calls calledFunc, false otherwise
   */
  boolean isCalling(FunctionInterface calledFunc);

  /**
   * Check if current function is a global function
   * 
   * @return True if function is global, false otherwise
   */
  boolean isGlobal();

  /**
   * Set parent class, if function belongs to a class
   * 
   * @param parent Parent class
   */
  void setParentClass(ClassInterface parent);

  /**
   * Get parent class of a function
   * 
   * @return Parent class, null if function is not a class method
   */
  ClassInterface getParentClass();

  /**
   * Adds function argument
   * 
   * @param arg Argument to add
   */
  void addArgument(ArgumentInterface arg);

  /**
   * Gets function arguments
   * 
   * @return Function arguments
   */
  ArgumentInterface[] getArguments();

  /**
   * Sets function long name, with class and argument list
   * 
   * @param functionLongName New long name
   */
  void setLongName(String functionLongName);

  /**
   * Gets function long name, with class and argument list
   * 
   * @return Function long name
   */
  String getLongName();

  /**
   * Add overloaded function to this function list
   * 
   * @param overFunc Overloaded function
   */
  void addOverloadFunction(FunctionInterface overFunc);

  /**
   * Get the overloaded functions for this function
   * 
   * @return All functions that overload this function
   */
  FunctionInterface[] getOverloadedFunctions();

  /**
   * Gets the function unit
   * 
   * @return function unit
   */
  UnitInterface getUnit();

  /**
   * Sets the function unit
   * 
   * @param functionUnit new function unit
   */
  void setUnit(UnitInterface functionUnit);

  /**
   * Set if function is virtual or not
   * 
   * @param value True if function is virtual
   */
  void setVirtual(boolean value);

  /**
   * Set if function is message function or not
   * 
   * @param value True if function is a message function
   */
  void setMessage(boolean value);

  /**
   * Checks if function is virtual
   * 
   * @return True if function is virtual, false otherwise
   */
  boolean isVirtual();

  /**
   * Checks if function is a message function
   * 
   * @return True if function is a message function, false otherwise
   */
  boolean isMessage();

}
