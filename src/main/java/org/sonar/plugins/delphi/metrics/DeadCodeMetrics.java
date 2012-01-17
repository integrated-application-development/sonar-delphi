/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.metrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Metric used to search for "dead code" (unused units, unused methods).
 */
public class DeadCodeMetrics extends DefaultMetrics implements MetricsInterface {

  private static final String DEAD_UNIT_VIOLATION_MESSAGE = " - unused unit. No other unit nor project has this unit in it's uses section. Probably you could remove this unit from project.";
  private static final String DEAD_FUNCTION_VIOLATION_MESSAGE = " - unused function/procedure. No other function and procedure in a project refers to it. Probably you could remove it.";

  private boolean isCalculated;
  private List<String> unusedUnits;
  private Set<FunctionInterface> unusedFunctions;
  private List<UnitInterface> allUnits;
  private Rule unitRule = null;
  private Rule functionRule = null;

  /**
   * {@inheritDoc}
   */
  public DeadCodeMetrics(Project delphiProject) {
    super(delphiProject);
    isCalculated = false;
    allUnits = new ArrayList<UnitInterface>();
    RuleFinder ruleFinder = DelphiProjectHelper.getInstance().getRuleFinder();
    if (ruleFinder == null) {
      return; // no rule finder
    }
    RuleQuery ruleQuery = RuleQuery.create().withRepositoryKey(DelphiPmdConstants.REPOSITORY_KEY).withKey("Unused Unit Rule");
    unitRule = ruleFinder.find(ruleQuery);
    ruleQuery = RuleQuery.create().withRepositoryKey(DelphiPmdConstants.REPOSITORY_KEY).withKey("Unused Function Rule");
    functionRule = ruleFinder.find(ruleQuery);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(DelphiFile resource, SensorContext sensorContext, List<ClassInterface> classes, List<FunctionInterface> functions,
      List<UnitInterface> units) {
    if ( !isCalculated) { // calculate only once
      if (units == null || units.size() == 0) {
        return;
      }
      unusedUnits = findUnusedUnits(units);
      unusedFunctions = findUnusedFunctions(units);
      isCalculated = true;
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(Resource resource, SensorContext sensorContext) {
    DelphiFile delphiFile = (DelphiFile) resource;
    if (delphiFile.isUnitTest()) {
      return; // do not count unit tests
    }

    UnitInterface unit = findUnit(delphiFile.getName());
    if (unit == null) {
      DelphiUtils.getDebugLog().println("No unit for " + delphiFile.getName() + "(" + delphiFile.getPath() + ")");
      return;
    }

    if (unusedUnits.contains(delphiFile.getName().toLowerCase())) { // unused unit, add violation
      int line = unit.getLine();
      Violation violation = Violation.create(unitRule, resource).setLineId(line).setMessage(unit.getName() + DEAD_UNIT_VIOLATION_MESSAGE);
      sensorContext.saveViolation(violation, true);
    }

    for (FunctionInterface function : getUnitFunctions(unit)) {

      if (function.isMessage() || function.isVirtual() || function.getVisibility() == DelphiLexer.PUBLISHED) {
        continue; // function is either a virtual function or at published visibility or message function
      }

      if (function.getParentClass() != null) {
        // check if function is not a interface implementation
        boolean isImplementation = false;
        for (ClassInterface parent : function.getParentClass().getParents()) {
          if (parent.hasFunction(function)) {
            isImplementation = true;
            break;
          }
        }
        if (isImplementation) {
          continue; // function used as interface implementation, surely not unused function
        }

        // check if function is used in property field
        boolean usedInProperty = false;
        for (ClassPropertyInterface property : function.getParentClass().getProperties()) {
          if (property.hasFunction(function)) {
            usedInProperty = true;
            break;
          }
        }
        if (usedInProperty) {
          continue; // function used in property field, surely not unused function
        }
      }

      if (unusedFunctions.contains(function)) { // unused function, add violation
        int line = function.getLine();
        Violation violation = Violation.create(functionRule, resource).setLineId(line)
            .setMessage(function.getRealName() + DEAD_FUNCTION_VIOLATION_MESSAGE);
        sensorContext.saveViolation(violation, true);
        unusedFunctions.remove(function); // to avoid duplicated violations
      }
    }
  }

  /**
   * @param unit
   *          Unit
   * @return List of all unit functions (global and class functions)
   */
  private List<FunctionInterface> getUnitFunctions(UnitInterface unit) {
    List<FunctionInterface> result = new ArrayList<FunctionInterface>();
    for (FunctionInterface globalFunction : unit.getFunctions()) { // add global functions
      result.add(globalFunction);
    }

    for (ClassInterface clazz : unit.getClasses()) { // add class function
      for (FunctionInterface function : clazz.getFunctions()) {
        result.add(function);
      }
    }

    return result;
  }

  /**
   * Find unused functions in a unit
   * 
   * @param units
   *          Unit array
   * @return List of unused functions
   */
  protected Set<FunctionInterface> findUnusedFunctions(List<UnitInterface> units) {
    Set<FunctionInterface> allFunctions = new HashSet<FunctionInterface>();
    Set<FunctionInterface> usedFunctions = new HashSet<FunctionInterface>();
    for (UnitInterface unit : units) {
      List<FunctionInterface> unitFunctions = getUnitFunctions(unit);
      allFunctions.addAll(unitFunctions);
      for (FunctionInterface unitFunction : unitFunctions) {
        for (FunctionInterface usedFunction : unitFunction.getCalledFunctions()) {
          usedFunctions.add(usedFunction);
        }
      }

    }
    allFunctions.removeAll(usedFunctions);
    return allFunctions;
  }

  /**
   * Find unused units
   */
  protected List<String> findUnusedUnits(List<UnitInterface> units) {
    Set<String> usedUnits = new HashSet<String>();
    List<String> result = new ArrayList<String>();
    for (UnitInterface unit : units) {
      if (unit.getFileName().toLowerCase().endsWith(".pas")) { // TODO make it DelphiLanguage independable
        result.add(unit.getName().toLowerCase()); // if not a .dpr, add it to unusedUnits
        allUnits.add(unit);
      }

      for (String usedUnit : unit.getIncludes()) {
        usedUnits.add(usedUnit.toLowerCase());
      }
    }

    result.removeAll(usedUnits);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean executeOnResource(DelphiFile resource) {
    String[] endings = DelphiLanguage.instance.getFileSuffixes();
    for (String ending : endings) {
      if (resource.getPath().endsWith("." + ending)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Searches for a unit by given unit name
   * 
   * @param unitName
   *          Unit name
   * @return Unit if found, null otherwise
   */
  private UnitInterface findUnit(String unitName) {
    for (UnitInterface unit : allUnits) {
      if (unit.getName().equalsIgnoreCase(unitName)) {
        return unit;
      }
    }
    return null;
  }

}
