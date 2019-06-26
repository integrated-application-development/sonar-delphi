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
import java.util.Objects;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.antlr.generated.DelphiParser;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Class counting function cyclomatic complexity.
 */
public class ComplexityMetrics extends DefaultMetrics {

  public static final RuleKey RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY = RuleKey
      .of(DelphiPmdConstants.REPOSITORY_KEY, "MethodCyclomaticComplexityRule");

  private ActiveRule methodCyclomaticComplexityRule;

  /**
   * The Cyclomatic Complexity Number.
   */
  private int fileComplexity;
  /**
   * Number of classes including nested classes, interfaces, enums and annotations.
   */
  private int classCount;
  /**
   * Number of Methods without including  accessors. A constructor is considered  to be a method.
   */
  private int methodsCount;
  /**
   * Number of statements as defined in the DelphiLanguage Language Specification but without block
   * definitions.
   */
  private int statementsCount;
  /**
   * Number of public classes, public methods  (without accessors) and public properties  (without
   * public final static ones).
   */
  private int publicApi;

  private Integer threshold;
  private final SensorContext context;

  /**
   * {@inheritDoc}
   */
  public ComplexityMetrics(ActiveRules activeRules, SensorContext sensorContext) {
    super();
    context = sensorContext;
    methodCyclomaticComplexityRule = activeRules.find(RULE_KEY_METHOD_CYCLOMATIC_COMPLEXITY);
    threshold = Integer.valueOf(Objects
        .requireNonNull(Objects.requireNonNull(methodCyclomaticComplexityRule).param("Threshold")));
  }

  /**
   * Analyses DelphiLanguage source file
   *
   * @param resource DelphiLanguage source file (.pas) to analyse
   * @param classes Classes that were found in that file
   * @param functions Functions that were found in that file
   */

  @Override
  public void analyse(InputFile resource, List<ClassInterface> classes,
      List<FunctionInterface> functions,
      Set<UnitInterface> units) {
    reset();
    Set<String> processedFunc = new HashSet<>();
    if (classes != null) {
      for (ClassInterface cl : classes) {
        if (cl == null) {
          continue;
        }

        ++classCount;
        fileComplexity += cl.getComplexity();
        publicApi += cl.getPublicApiCount();

        for (FunctionInterface func : cl.getFunctions()) {
          processFunction(resource, func);
          processedFunc.add(func.getLongName());
        }
      }
    }

    // processing stand-alone (global) functions, not merged with any class
    if (functions != null) {
      for (FunctionInterface func : functions) {
        if (func == null || processedFunc.contains(func.getLongName())) {
          continue;
        }
        ++methodsCount;
        fileComplexity += func.getComplexity();
        statementsCount += func.getStatements().size();
        if (func.getVisibility() == DelphiParser.PUBLIC) {
          ++publicApi;
        }
        processedFunc.add(func.getLongName());

        addIssue(resource, func);
      }
    }

    saveAllMetrics();
  }

  private void processFunction(InputFile resource, FunctionInterface func) {
    if (!func.isAccessor()) {
      methodsCount++;

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
  public void save(InputFile resource) {
    if (resource == null) {
      return;
    }
    try {

      /* Number of statements as defined in the DelphiLanguage Language Specification
      but without block definitions.*/
      context.<Integer>newMeasure().forMetric(CoreMetrics.STATEMENTS).on(resource)
          .withValue(getIntMetric("STATEMENTS")).save();
      // The Cyclomatic Complexity Number
      context.<Integer>newMeasure().forMetric(CoreMetrics.COMPLEXITY).on(resource)
          .withValue(getIntMetric("COMPLEXITY")).save();
      // Number of classes including nested classes, interfaces, enums and annotations
      context.<Integer>newMeasure().forMetric(CoreMetrics.CLASSES).on(resource)
          .withValue(getIntMetric("CLASSES")).save();
      // Number of Methods without including accessors. A constructor is considered to be a method.
      context.<Integer>newMeasure().forMetric(CoreMetrics.FUNCTIONS).on(resource)
          .withValue(getIntMetric("FUNCTIONS")).save();
      /* Number of public classes, public methods (without accessors) and
      public properties (without public static final ones)*/
      context.<Integer>newMeasure().forMetric(CoreMetrics.PUBLIC_API).on(resource)
          .withValue(getIntMetric("PUBLIC_API")).save();

    } catch (IllegalStateException e) {
      DelphiPlugin.LOG.error("{}: Error while saving Complexity metrics: ", resource.filename(), e);
    }
  }

  private void saveAllMetrics() {
    setIntMetric("STATEMENTS", statementsCount);
    setIntMetric("COMPLEXITY", fileComplexity);
    setIntMetric("CLASSES", classCount);
    setIntMetric("FUNCTIONS", methodsCount);
    setIntMetric("PUBLIC_API", publicApi);
  }

  private void reset() {
    fileComplexity = 0;
    classCount = 0;
    methodsCount = 0;
    statementsCount = 0;
    publicApi = 0;
    clearMetrics();
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean executeOnResource(InputFile resource) {
    return DelphiUtils.acceptFile(resource.filename());
  }

  private void addIssue(InputFile inputFile, FunctionInterface func) {
    if (func.getComplexity() > threshold) {
      NewIssue newIssue = context.newIssue();
      int line = func.getLine();
      int beginCol = func.getBeginColumn();
      int endCol = func.getEndColumn();
      newIssue
          .forRule(methodCyclomaticComplexityRule.ruleKey())
          .at(newIssue.newLocation()
              .on(inputFile)
              .at(inputFile.newRange(line, beginCol, line, endCol))
              .message(String.format(
                  "The Cyclomatic Complexity of this method \"%s\" is %d which is " +
                      "greater than %d authorized.",
                  func.getRealName(), func.getComplexity(), threshold)));
      newIssue.save();
    }
  }
}
