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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Class counting function cyclomatic complexity.
 */
public class ComplexityMetrics extends DefaultMetrics implements MetricsInterface {

  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
  private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {1, 5, 10, 20, 30, 60, 90};

  public static final RuleKey RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, "MethodCyclomaticComplexityRule");

  private ActiveRule methodCyclomaticComplexityRule;

  // FUNCTION_COMPLEXITY_DISTRIBUTION = Number of methods for given
  // complexities
  private RangeDistributionBuilder functionDist = new RangeDistributionBuilder(
    CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
    FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
  // FILE COMPLEXITY DISTRIBUTION = Number of files for given complexities
  private RangeDistributionBuilder fileDist = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION,
    FILES_DISTRIB_BOTTOM_LIMITS);

  /**
   * The Cyclomatic Complexity Number.
   */
  private double fileComplexity = 0;
  /**
   * Average cyclomatic complexity number by method.
   */
  private double functionComplexity = 0;
  /**
   * Number of classes including nested classes, interfaces, enums and annotations.
   */
  private double classCount = 0;
  /**
   * Average complexity by class.
   */
  private double classComplexity = 0;
  /**
   * Number of Methods without including  accessors. A constructor is considered  to be a method.
   */
  private double methodsCount = 0;
  /**
   * Number of getter and setter methods used to get(reading) or set(writing) a class' property.
   */
  private double accessorsCount = 0;
  /**
   *  Number of statements as defined in the DelphiLanguage Language Specification but without block  definitions.
   */
  private double statementsCount = 0;
  /**
   * Number of public classes, public methods  (without accessors) and public properties  (without public final static ones).
   */
  private double publicApi = 0;

  private ResourcePerspectives perspectives;
  private Integer threshold;

  /**
   * {@inheritDoc}
   */
  public ComplexityMetrics(ActiveRules activeRules, ResourcePerspectives perspectives) {
    super();
    this.perspectives = perspectives;
    methodCyclomaticComplexityRule = activeRules.find(RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY);
    threshold = Integer.valueOf(methodCyclomaticComplexityRule.param("Threshold"));

  }

  /**
   * Analyses DelphiLanguage source file
   * 
   * @param resource DelphiLanguage source file (.pas) to analyse
   * @param sensorContext Sensor context, given by Sonar
   * @param classes Classes that were found in that file
   * @param functions Functions that were found in that file
   */

  @Override
  public void analyse(InputFile resource, SensorContext sensorContext, List<ClassInterface> classes,
    List<FunctionInterface> functions,
    List<UnitInterface> units) {
    reset();
    Set<String> processedFunc = new HashSet<String>();
    if (classes != null) {
      for (ClassInterface cl : classes) {
        if (cl == null) {
          continue;
        }

        ++classCount;
        fileComplexity += cl.getComplexity();
        classComplexity += cl.getComplexity();
        accessorsCount += cl.getAccessorCount();
        publicApi += cl.getPublicApiCount();

        for (FunctionInterface func : cl.getFunctions()) {
          processFunction(resource, func);
          processedFunc.add(func.getName());
        }
      }
    }

    // procesing stand-alone (global) functions, not merged with any class
    if (functions != null) {
      for (FunctionInterface func : functions) {
        if (func == null) {
          continue;
        }
        if (processedFunc.contains(func.getName())) {
          continue;
        }
        methodsCount += 1 + func.getOverloadsCount();
        fileComplexity += func.getComplexity();
        functionComplexity += func.getComplexity();
        statementsCount += func.getStatements().size();
        functionDist.add(Double.valueOf(func.getComplexity()));
        if (func.getVisibility() == DelphiParser.PUBLIC) {
          ++publicApi;
        }
        processedFunc.add(func.getName());

        addIssue(resource, func);
      }
    }

    fileDist.add(fileComplexity);

    if (methodsCount != 0.0) {
      functionComplexity /= methodsCount;
    }
    if (classCount != 0.0) {
      classComplexity /= classCount;
    }

    saveAllMetrics();
  }

  private void processFunction(InputFile resource, FunctionInterface func) {
    if (!func.isAccessor()) {
      methodsCount++;
      functionComplexity += func.getComplexity();
      functionDist.add(Double.valueOf(func.getComplexity()));

      addIssue(resource, func);

      for (FunctionInterface over : func.getOverloadedFunctions()) {
        processFunction(resource, over);
      }
    }
    statementsCount += func.getStatements().size();
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void save(InputFile resource, SensorContext sensorContext) {
    if (resource == null || sensorContext == null) {
      return;
    }
    try {
      // Number of statements as defined in the DelphiLanguage Language Specification but without block definitions.
      sensorContext.saveMeasure(resource, CoreMetrics.STATEMENTS, getMetric("STATEMENTS"));
      // The Cyclomatic Complexity Number
      sensorContext.saveMeasure(resource, CoreMetrics.COMPLEXITY, getMetric("COMPLEXITY"));
      // Average complexity by class
      sensorContext.saveMeasure(resource, CoreMetrics.CLASS_COMPLEXITY, getMetric("CLASS_COMPLEXITY"));

      // Average cyclomatic complexity number by method
      sensorContext.saveMeasure(resource, CoreMetrics.FUNCTION_COMPLEXITY, getMetric("FUNCTION_COMPLEXITY"));

      // Number of classes including nested classes, interfaces, enums and annotations
      sensorContext.saveMeasure(resource, CoreMetrics.CLASSES, getMetric("CLASSES"));
      // Number of Methods without including accessors. A constructor is considered to be a method.
      sensorContext.saveMeasure(resource, CoreMetrics.FUNCTIONS, getMetric("FUNCTIONS"));
      // Number of getter and setter methods used to get(reading) or set(writing) a class' property .
      sensorContext.saveMeasure(resource, CoreMetrics.ACCESSORS, getMetric("ACCESSORS"));
      // Number of public classes, public methods (without accessors) and public properties (without public static final ones)
      sensorContext.saveMeasure(resource, CoreMetrics.PUBLIC_API, getMetric("PUBLIC_API"));
      sensorContext.saveMeasure(resource, functionDist.build().setPersistenceMode(PersistenceMode.MEMORY));
      sensorContext.saveMeasure(resource, fileDist.build().setPersistenceMode(PersistenceMode.MEMORY));
    } catch (IllegalStateException ise) {
      DelphiUtils.LOG.error(ise.getMessage());
    }
  }

  private void saveAllMetrics() {
    setMetric("STATEMENTS", statementsCount);
    setMetric("COMPLEXITY", fileComplexity);
    setMetric("CLASS_COMPLEXITY", classComplexity);
    setMetric("FUNCTION_COMPLEXITY", functionComplexity);
    setMetric("CLASSES", classCount);
    setMetric("FUNCTIONS", methodsCount);
    setMetric("ACCESSORS", accessorsCount);
    setMetric("PUBLIC_API", publicApi);
  }

  private void reset() {
    fileComplexity = 0;
    functionComplexity = 0;
    classCount = 0;
    classComplexity = 0;
    methodsCount = 0;
    accessorsCount = 0;
    statementsCount = 0;
    publicApi = 0;
    functionDist.clear();
    fileDist.clear();
    clearMetrics();
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean executeOnResource(InputFile resource) {
    return DelphiUtils.acceptFile(resource.absolutePath());
  }

  private void addIssue(InputFile resource, FunctionInterface func) {
    if (func.getComplexity() > threshold.intValue()) {
      Issuable issuable = perspectives.as(Issuable.class, resource);
      if (issuable != null) {
        Issue issue = issuable.newIssueBuilder()
          .ruleKey(methodCyclomaticComplexityRule.ruleKey())
          .line(func.getBodyLine())
          .message(String.format("The Cyclomatic Complexity of this method \"%s\" is %d which is greater than %d authorized.",
            func.getRealName(), func.getComplexity(), threshold))
          .build();
        issuable.addIssue(issue);
      }
    }
  }
}
