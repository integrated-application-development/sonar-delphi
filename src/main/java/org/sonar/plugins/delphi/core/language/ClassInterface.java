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
 * Interface used for custom class creation.
 */

public interface ClassInterface extends HasNameInterface {

  /**
   * Get the number of class accessors (setters and getters)
   * 
   * @return Number of accessors
   */
  int getAccessorCount();

  /**
   * Gets class visibility (public, protected or private)
   * 
   * @return Public, protected or private as int
   */
  int getVisibility();

  /**
   * Sets class visibility
   * 
   * @param value New visibility
   */
  void setVisibility(int value);

  /**
   * Gets count of methods and fields
   * 
   * @return Count of methods and fields
   */
  int getPublicApiCount();

  /**
   * Sets class file name
   * 
   * @param fileName File in which class resides
   */
  void setFileName(String fileName);

  /**
   * Gets class file name
   * 
   * @return File name
   */
  String getFileName();

  /**
   * Gets the class short name, without filename prefix
   * 
   * @return Class short name
   */
  String getShortName();

  /**
   * Add field to a class
   * 
   * @param field New field to add
   */
  void addField(ClassFieldInterface field);

  /**
   * Get class fields
   * 
   * @return Class fields list
   */
  ClassFieldInterface[] getFields();

  /**
   * Add property field
   * 
   * @param property Property field to add
   */
  void addProperty(ClassPropertyInterface property);

  /**
   * Get class property fields
   * 
   * @return Class property fields
   */
  ClassPropertyInterface[] getProperties();

  /**
   * Gets class complexity
   * 
   * @return Class complexity
   */
  int getComplexity();

  /**
   * Get class functions (without declarations)
   * 
   * @return Class function list
   */
  FunctionInterface[] getFunctions();

  /**
   * Get class function declarations (only)
   * 
   * @return Class function declaration list
   */
  FunctionInterface[] getDeclarations();

  /**
   * Adds a function to a class
   * 
   * @param func Functions to add
   */
  void addFunction(FunctionInterface func);

  /**
   * Add a parent (ancestor class) of current class
   * 
   * @param parent Ancestor
   */
  void addParent(ClassInterface parent);

  /**
   * Add a child (direct or indirect descendant)
   * 
   * @param child Child class
   */
  void addChild(ClassInterface child);

  /**
   * Gets a list of all parents (ancestors of class)
   * 
   * @return List of all parents
   */
  ClassInterface[] getParents();

  /**
   * Gets a list of children (direct descendants of class)
   * 
   * @return List of all children
   */
  ClassInterface[] getChildren();

  /**
   * Gets a list of all children (direct AND indirect descendants of class)
   * 
   * @return List of all descendants
   */
  ClassInterface[] getDescendants();

  /**
   * Do class contains a function (or function declaration)?
   * 
   * @param func Function to check
   * @return True if function is a member of class, false otherwise
   */
  boolean hasFunction(FunctionInterface func);

}
