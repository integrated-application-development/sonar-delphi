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
package org.sonar.plugins.delphi.metrics;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Metric used to search for "dead code" (unused units, unused methods).
 */
public class DeadCodeMetrics extends DefaultMetrics {

  private static final String DEAD_UNIT_VIOLATION_MESSAGE = " - unused unit. No other unit nor " +
      "project has this unit in it's uses section. Probably you could remove this unit from " +
      "project.";
  private static final String DEAD_FUNCTION_VIOLATION_MESSAGE = " - unused function/procedure. " +
      "No other function and procedure in a project refers to it. " +
      "Probably you could remove it.";

  private boolean isCalculated;
  private List<String> unusedUnits;
  private Set<FunctionInterface> unusedFunctions;
  private List<UnitInterface> allUnits;
  private final ActiveRule unitRule;
  private final ActiveRule functionRule;
  private final SensorContext context;

  public static final RuleKey RULE_KEY_UNUSED_UNIT = RuleKey
      .of(DelphiPmdConstants.REPOSITORY_KEY, "UnusedUnitRule");
  public static final RuleKey RULE_KEY_UNUSED_FUNCTION = RuleKey
      .of(DelphiPmdConstants.REPOSITORY_KEY, "UnusedFunctionRule");

  /**
   * {@inheritDoc}
   */
  public DeadCodeMetrics(ActiveRules activeRules, SensorContext sensorContext) {
    super();
    context = sensorContext;
    isCalculated = false;
    allUnits = new ArrayList<>();
    unitRule = activeRules.find(RULE_KEY_UNUSED_UNIT);
    functionRule = activeRules.find(RULE_KEY_UNUSED_FUNCTION);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void analyse(InputFile resource, List<ClassInterface> classes,
      List<FunctionInterface> functions,
      Set<UnitInterface> units) {
    if (!isCalculated) {
      if (units == null || units.isEmpty()) {
        return;
      }
      unusedUnits = findUnusedUnits(units);

      // findUnusedFunctions always returns an empty list
      // huh? Why?
      unusedFunctions = findUnusedFunctions(units);
      isCalculated = true;
    }

  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void save(InputFile inputFile) {
    if (inputFile.type() == Type.TEST) {
      return;
    }

    UnitInterface unit = findUnit(inputFile.uri());
    if (unit == null) {
      String unitName = FilenameUtils.removeExtension(inputFile.filename());
      String logDesc = String.format("No unit for %s (%s)", unitName, inputFile.toString());
      DelphiUtils.LOG.debug(logDesc);
      return;
    }

    if (unusedUnits.contains(unit.getName().toLowerCase())) {
      NewIssue newIssue = context.newIssue();
      newIssue
          .forRule(unitRule.ruleKey())
          .at(newIssue.newLocation()
              .on(inputFile)
              .at(inputFile.selectLine(1))
              .message(unit.getName() + DEAD_UNIT_VIOLATION_MESSAGE))
          .save();
    }

    for (FunctionInterface function : getUnitFunctions(unit)) {
      if (!isDeadFunction(function)) {
        continue;
      }

      RuleKey rule = functionRule.ruleKey();

      if (rule != null) {
        int line = function.getLine();
        int column = function.getColumn();

        NewIssue newIssue = context.newIssue();
        newIssue
            .forRule(rule)
            .at(newIssue.newLocation()
                .on(inputFile)
                .at(inputFile.newRange(line, column,
                    line, column + function.getName().length()))
                .message(function.getRealName() + DEAD_FUNCTION_VIOLATION_MESSAGE))
            .save();
      }
    }
  }

  private boolean isDeadFunction(FunctionInterface function) {
    if (function.isMessage() ||
        function.isVirtual() ||
        function.getVisibility() == DelphiLexer.PUBLISHED) {
      return false;
    }

    return unusedFunctions.contains(function)
        && !isFunctionInterfaceImplementation(function)
        && !isFunctionPropertyReadWriteSpecifier(function);
  }

  private boolean isFunctionInterfaceImplementation(FunctionInterface function) {
    ClassInterface clazz = function.getParentClass();
    if (clazz != null) {
      for (ClassInterface parent : clazz.getParents()) {
        if (parent.hasFunction(function)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isFunctionPropertyReadWriteSpecifier(FunctionInterface function) {
    ClassInterface clazz = function.getParentClass();
    if (clazz != null) {
      for (ClassPropertyInterface property : clazz.getProperties()) {
        if (property.hasFunction(function)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param unit Unit
   * @return List of all unit functions (global and class functions)
   */
  private List<FunctionInterface> getUnitFunctions(UnitInterface unit) {
    Set<FunctionInterface> result = new HashSet<>();
    Collections.addAll(result, unit.getFunctions());

    for (ClassInterface clazz : unit.getClasses()) {
      Collections.addAll(result, clazz.getFunctions());
    }

    return new ArrayList<>(result);
  }

  /**
   * Find unused functions in a unit
   *
   * @param units Unit array
   * @return List of unused functions
   */
  private Set<FunctionInterface> findUnusedFunctions(Set<UnitInterface> units) {
    Set<FunctionInterface> allFunctions = new HashSet<>();
    Set<FunctionInterface> usedFunctions = new HashSet<>();
    for (UnitInterface unit : units) {
      List<FunctionInterface> unitFunctions = getUnitFunctions(unit);
      allFunctions.addAll(unitFunctions);
      for (FunctionInterface unitFunction : unitFunctions) {
        Collections.addAll(usedFunctions, unitFunction.getCalledFunctions());
      }

    }
    allFunctions.removeAll(usedFunctions);
    return allFunctions;
  }

  /**
   * Find unused units
   *
   * @param units Units in project
   * @return a list of unused units
   */
  private List<String> findUnusedUnits(Set<UnitInterface> units) {
    Set<String> usedUnits = new HashSet<>();
    List<String> result = new ArrayList<>();
    for (UnitInterface unit : units) {
      if (unit.getFileName().toLowerCase().endsWith(".pas")) {
        result.add(unit.getName().toLowerCase());
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
  public boolean executeOnResource(InputFile resource) {
    return DelphiUtils.acceptFile(resource.filename());
  }

  /**
   * Searches for a unit by given unit name
   *
   * @param uri The unique uri from this unit's InputFile
   * @return Unit if found, null otherwise
   */
  private UnitInterface findUnit(URI uri) {
    for (UnitInterface unit : allUnits) {
      if (unit.getUri().equals(uri)) {
        return unit;
      }
    }
    return null;
  }

}
