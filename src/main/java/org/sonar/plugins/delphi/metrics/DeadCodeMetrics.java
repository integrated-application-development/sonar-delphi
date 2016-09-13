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

import org.apache.commons.io.FilenameUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  private final ActiveRule unitRule;
  private final ActiveRule functionRule;
  private final ResourcePerspectives perspectives;

  public static final RuleKey RULE_KEY_UNUSED_UNIT = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, "UnusedUnitRule");
  public static final RuleKey RULE_KEY_UNUSED_FUNCTION = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, "UnusedFunctionRule");

  /**
   * {@inheritDoc}
   */
  public DeadCodeMetrics(ActiveRules activeRules, ResourcePerspectives perspectives) {
    super();
    this.perspectives = perspectives;
    isCalculated = false;
    allUnits = new ArrayList<UnitInterface>();
    unitRule = activeRules.find(RULE_KEY_UNUSED_UNIT);
    functionRule = activeRules.find(RULE_KEY_UNUSED_FUNCTION);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void analyse(InputFile resource, SensorContext sensorContext, List<ClassInterface> classes,
    List<FunctionInterface> functions,
    Set<UnitInterface> units) {
    if (!isCalculated) {
      if (units == null || units.isEmpty()) {
        return;
      }
      unusedUnits = findUnusedUnits(units);

      // TODO findUnusedFunctions always returns an empty list
      unusedFunctions = findUnusedFunctions(units);
      isCalculated = true;
    }

  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void save(InputFile resource, SensorContext sensorContext) {
    if (resource.type() == Type.TEST) {
      return;
    }

    String fileName = FilenameUtils.removeExtension(resource.file().getName());
    UnitInterface unit = findUnit(fileName);
    if (unit == null) {
      DelphiUtils.LOG.debug("No unit for " + fileName + "(" + resource.absolutePath() + ")");
      return;
    }

    if (unusedUnits.contains(fileName.toLowerCase())) {
      Issuable issuable = perspectives.as(Issuable.class, resource);
      if (issuable != null) {
        Issue issue = issuable.newIssueBuilder()
          .ruleKey(unitRule.ruleKey())
          .line(unit.getLine())
          .message(unit.getName() + DEAD_UNIT_VIOLATION_MESSAGE)
          .build();
        issuable.addIssue(issue);
      }
    }

    for (FunctionInterface function : getUnitFunctions(unit)) {
      if (function.isMessage() || function.isVirtual() || function.getVisibility() == DelphiLexer.PUBLISHED) {
        continue;
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
          // function used as interface implementation, surely not unused function
          continue;
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
          // function used in property field, surely not unused function
          continue;
        }
      }

      if (unusedFunctions.contains(function)) {
        Issuable issuable = perspectives.as(Issuable.class, resource);
        if (issuable != null) {
          Issue issue = issuable.newIssueBuilder()
            .ruleKey(functionRule.ruleKey())
            .line(function.getLine())
            .message(function.getRealName() + DEAD_FUNCTION_VIOLATION_MESSAGE)
            .build();

          // TODO Unused functions it's not working. There are many
          // false positives.
          // issuable.addIssue(issue);
        }
      }
    }
  }

  /**
   * @param unit Unit
   * @return List of all unit functions (global and class functions)
   */
  private List<FunctionInterface> getUnitFunctions(UnitInterface unit) {
    Set<FunctionInterface> result = new HashSet<FunctionInterface>();
    for (FunctionInterface globalFunction : unit.getFunctions()) {
      result.add(globalFunction);
    }

    for (ClassInterface clazz : unit.getClasses()) {
      for (FunctionInterface function : clazz.getFunctions()) {
        result.add(function);
      }
    }

    return new ArrayList<FunctionInterface>(result);
  }

  /**
   * Find unused functions in a unit
   * 
   * @param units Unit array
   * @return List of unused functions
   */
  protected Set<FunctionInterface> findUnusedFunctions(Set<UnitInterface> units) {
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
   * @param units Units in project
   * @return a list of unused units
   */
  protected List<String> findUnusedUnits(Set<UnitInterface> units) {
    Set<String> usedUnits = new HashSet<String>();
    List<String> result = new ArrayList<String>();
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
    return DelphiUtils.acceptFile(resource.absolutePath());
  }

  /**
   * Searches for a unit by given unit name
   * 
   * @param unitName Unit name
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
