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
package org.sonar.plugins.delphi.antlr.analyzer;

import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding results of source code parsing
 */
public class CodeAnalysisResults extends CodeAnalysisState {

  protected List<FunctionInterface> functions = new ArrayList<FunctionInterface>();
  protected List<ClassInterface> classes = new ArrayList<ClassInterface>();

  /**
   * @return class list in current file
   */
  public List<ClassInterface> getClasses() {
    return classes;
  }

  /**
   * @return function list in current file
   */
  public List<FunctionInterface> getFunctions() {
    return functions;
  }

  /**
   * add function to current file
   * 
   * @param activeFunction function to add
   */
  public void addFunction(FunctionInterface activeFunction) {
    functions.add(activeFunction);
  }

  /**
   * add class to current file
   * 
   * @param clazz class to add
   */
  public void addClass(ClassInterface clazz) {
    classes.add(clazz);
  }

  /**
   * check, if there is a certain function in current file
   * 
   * @param activeFunction function to find
   * @return true if function is present, false otherwise
   */
  public boolean hasFunction(FunctionInterface activeFunction) {
    return functions.contains(activeFunction);
  }
}
