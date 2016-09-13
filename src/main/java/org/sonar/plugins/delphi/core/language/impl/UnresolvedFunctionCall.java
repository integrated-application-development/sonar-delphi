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
package org.sonar.plugins.delphi.core.language.impl;

import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;

import java.util.Set;

/**
 * Class used by AbstractAnalyser for unresolved function calls from function
 * body (when we call a function, but didn't read function include file yet)
 */
public class UnresolvedFunctionCall {

  private FunctionInterface caller;
  private FunctionInterface called;
  private UnitInterface callerUnit;

  /**
   * Default ctor
   * 
   * @param caller Function that has called another function
   * @param called Called function
   * @param callerUnit Caller function unit
   */
  public UnresolvedFunctionCall(FunctionInterface caller, FunctionInterface called, UnitInterface callerUnit) {
    this.caller = caller;
    this.called = called;
    this.callerUnit = callerUnit;
  }

  /**
   * Get function that calls another function (caller)
   * 
   * @return Caller function
   */
  public FunctionInterface getCaller() {
    return caller;
  }

  /**
   * Get called function
   * 
   * @return Called function
   */
  public FunctionInterface getCalled() {
    return called;
  }

  /**
   * Get caller function unit
   * 
   * @return Caller function unit
   */
  public UnitInterface getCallerUnit() {
    return callerUnit;
  }

  /**
   * Try to resolve called function call (find called function in provided
   * units)
   * 
   * @param allUnits List of all units
   * @return True if function was resolved, false otherwise
   */
  public boolean resolve(Set<UnitInterface> allUnits) {
    Set<UnitInterface> includedUnits = callerUnit.getIncludedUnits(allUnits);
    for (UnitInterface unit : includedUnits) {
      FunctionInterface[] unitFunctions = unit.getAllFunctions();
      for (FunctionInterface function : unitFunctions) {
        if (function.getName().toLowerCase().endsWith(called.getName().toLowerCase())) {
          caller.addCalledFunction(function);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Try to resolve called function call (check if provided function is not
   * the function called from other function)
   * 
   * @param functionToCheck Function we will be checking if not a called
   *            function
   * @param allUnits List of all units
   * @return True if function was resolved, false otherwise
   */
  public boolean resolve(FunctionInterface functionToCheck, Set<UnitInterface> allUnits) {
    Set<UnitInterface> includedUnits = callerUnit.getIncludedUnits(allUnits);
    if (!includedUnits.contains(functionToCheck.getUnit())) {
      return false;
    }
    if (functionToCheck.getName().toLowerCase().endsWith(called.getName().toLowerCase())) {
      caller.addCalledFunction(functionToCheck);
      return true;
    }
    return false;
  }

}
