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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.UnresolvedFunctionCall;

/**
 * Holds cached results in static variables
 */
public class CodeAnalysisCacheResults {

  protected static final Set<UnitInterface> allUnits = new HashSet<>();
  protected static final Map<String, Map<String, ClassInterface>> allClasses = new HashMap<>();
  protected static final Map<String, FunctionInterface> allFunctions = new HashMap<>();
  protected static final Map<String, UnresolvedFunctionCall> unresolvedCalls = new HashMap<>();

  /**
   * resets results chache
   */
  public static void resetCache() {
    allClasses.clear();
    allFunctions.clear();
    allUnits.clear();
    unresolvedCalls.clear();
  }

  /**
   * @return map of unresolved function calls
   */
  public Map<String, UnresolvedFunctionCall> getUnresolvedCalls() {
    return unresolvedCalls;
  }

  /**
   * Adds a unresolved function call
   * 
   * @param name unresolved function name
   * @param call the unresolved call
   */
  public void addUnresolvedCall(String name, UnresolvedFunctionCall call) {
    unresolvedCalls.put(name, call);
  }

  /**
   * @param classInterface class for search by exampe
   * @return cached class if found, null otherwise
   */
  public ClassInterface getCachedClass(ClassInterface classInterface) {
    Map<String, ClassInterface> unitClasses = allClasses.get(classInterface.getFileName());
    if (unitClasses != null) {
      return unitClasses.get(classInterface.getName());
    }
    return null;
  }

  /**
   * @param unitName unit where the class is declared
   * @param classInterface class for search by exampe
   * @return cached class if found, null otherwise
   */
  public ClassInterface getCachedClass(String unitName, ClassInterface classInterface) {
    final String fileToSearch = File.pathSeparator + unitName + ".pas";
    Map<String, ClassInterface> unitClasses = null;
    for (String fileName : allClasses.keySet()) {
      if (StringUtils.containsIgnoreCase(fileName, fileToSearch)) {
        unitClasses = allClasses.get(fileName);
        break;
      }
    }
    if (unitClasses != null) {
      return unitClasses.get(classInterface.getName());
    }
    return null;
  }

  /**
   * @param funcName function name
   * @return cached function if found, null otherwise
   */
  public FunctionInterface getCachedFunction(String funcName) {
    return allFunctions.get(funcName);
  }

  /**
   * @param unit unit
   * @return true if unit was cached
   */
  public boolean hasCachedUnit(UnitInterface unit) {
    return allUnits.contains(unit);
  }

  /**
   * @return set of cached units
   */
  public Set<UnitInterface> getCachedUnits() {
    return allUnits;
  }

  /**
   * @return list of cached units
   */
  public Set<UnitInterface> getCachedUnitsAsList() {
    Set<UnitInterface> result = new HashSet<>();
    result.addAll(allUnits);
    return result;
  }

  /**
   * add new unit to cache
   * 
   * @param unit unit to add
   */
  public void cacheUnit(UnitInterface unit) {
    allUnits.add(unit);
  }

  /**
   * add new class to cache
   * 
   * @param clazz class
   */
  public void cacheClass(ClassInterface clazz) {
    Map<String, ClassInterface> unitClasses = allClasses.get(clazz.getFileName());
    if (unitClasses == null) {
      unitClasses = new HashMap<>();
      allClasses.put(clazz.getFileName(), unitClasses);
    }
    unitClasses.put(clazz.getName(), clazz);
  }

  /**
   * add new function to cache
   * 
   * @param funcName function name
   * @param function function
   */
  public void cacheFunction(String funcName, FunctionInterface function) {
    allFunctions.put(funcName, function);
  }

}
