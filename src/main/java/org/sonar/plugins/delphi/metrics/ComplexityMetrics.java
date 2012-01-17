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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Class counting function cyclomatic complexity.
 */
public class ComplexityMetrics extends DefaultMetrics implements MetricsInterface {

  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12, 20, 30 };
  private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = { 1, 5, 10, 20, 30, 60, 90 };
  private static final Number[] CLASS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12, 15, 20, 30, 50 };
  private static final Number[] RFC_DISTRIB_BOTTOM_LIMITS = { 1, 5, 10, 20, 30, 40, 50, 70, 90, 100, 150 };

  // class_complexity_distribution = Number of classes for given complexities
  private RangeDistributionBuilder classDist = new RangeDistributionBuilder(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION,
      CLASS_DISTRIB_BOTTOM_LIMITS);
  // FUNCTION_COMPLEXITY_DISTRIBUTION = Number of methods for given complexities
  private RangeDistributionBuilder functionDist = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
      FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
  // FILE COMPLEXITY DISTRIBUTION = Number of files for given complexities
  private RangeDistributionBuilder fileDist = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION,
      FILES_DISTRIB_BOTTOM_LIMITS);
  // RFC CLASS DISTRIBUTION
  private RangeDistributionBuilder rfcDist = new RangeDistributionBuilder(CoreMetrics.RFC_DISTRIBUTION, RFC_DISTRIB_BOTTOM_LIMITS);

  private double fileComplexity = 0; // The Cyclomatic Complexity Number
  private double functionComplexity = 0; // Average cyclomatic complexity number by method
  private double classCount = 0; // Number of classes including nested classes, interfaces, enums and annotations
  private double classComplexity = 0; // Average complexity by class
  private double methodsCount = 0; // Number of Methods without including accessors. A constructor is considered to be a method.
  private double accessorsCount = 0; // Number of getter and setter methods used to get(reading) or set(writing) a class' property .
  private double statementsCount = 0; // Number of statements as defined in the DelphiLanguage Language Specification but without block
                                      // definitions.
  private double publicApi = 0; // Number of public classes, public methods (without accessors) and public properties (without public final
                                // static ones)
  private double dit = 0; // The depth of inheritance tree (DIT) metric provides for each class a measure of the inheritance levels from the
                          // object hierarchy top.
  private double noc = 0; // Number of children

  /**
   * -- WARNING ACHTUNG UWAGA -- This method counts only functions, that are in some unit that is in "used" section and were parsed by
   * AbstractAnalyser. That's why system function and procedures (such as "writeln") are not counted, unless their unit is also parsed by
   * ANTLR analyser.
   */
  private double rfc = 0; // The response set of a class is a set of methods that can potentially be executed in response to a message
                          // received by an object of that class. RFC is simply the number of methods in the set.

  /**
   * {@inheritDoc}
   */
  public ComplexityMetrics(Project delphiProject) {
    super(delphiProject);
  }

  /**
   * Analyses DelphiLanguage source file
   * 
   * @param resource
   *          DelphiLanguage source file (.pas) to analyse
   * @param sensorContext
   *          Sensor context, given by Sonar
   * @param classes
   *          Classes that were found in that file
   * @param functions
   *          Functions that were found in that file
   */
  @Override
  public void analyse(DelphiFile resource, SensorContext sensorContext, List<ClassInterface> classes, List<FunctionInterface> functions,
      List<UnitInterface> units) {
    reset();
    Set<String> processedFunc = new HashSet<String>();
    // for every class in file
    if (classes != null) {
      for (ClassInterface cl : classes) {
        if (cl == null) {
          continue;
        }
        // basic stats
        ++classCount;
        fileComplexity += cl.getComplexity();
        classComplexity += cl.getComplexity();
        accessorsCount += cl.getAccessorCount();
        publicApi += cl.getPublicApiCount();
        noc += cl.getDescendants().length;
        rfc += cl.getRfc();
        int clDit = cl.getDit();
        if (clDit > dit) {
          dit = clDit;
        }

        // for every function in class
        for (FunctionInterface func : cl.getFunctions()) {
          processFunction(func);
          processedFunc.add(func.getName()); // add function to processed
        }
        classDist.add(Double.valueOf(cl.getComplexity())); // class complexity distribut
        rfcDist.add(Double.valueOf(cl.getRfc())); // rfc complexity distribut
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
        functionDist.add(Double.valueOf(func.getComplexity())); // function complexity distribution
        if (func.getVisibility() == DelphiParser.PUBLIC) {
          ++publicApi;
        }
        processedFunc.add(func.getName());
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

  private void processFunction(FunctionInterface func) {
    if ( !func.isAccessor()) { // is this a function, not a accessor
      methodsCount++;
      functionComplexity += func.getComplexity();
      functionDist.add(Double.valueOf(func.getComplexity())); // function complexity distribution
      for (FunctionInterface over : func.getOverloadedFunctions()) {
        processFunction(over);
      }
    }
    statementsCount += func.getStatements().size(); // number of statements
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(Resource resource, SensorContext sensorContext) {
    if (resource == null || sensorContext == null) {
      return;
    }
    try {
      sensorContext.saveMeasure(resource, CoreMetrics.STATEMENTS, getMetric("STATEMENTS")); // Number of statements as defined in the
                                                                                            // DelphiLanguage
                                                                                            // Language Specification but without block
                                                                                            // definitions.
      sensorContext.saveMeasure(resource, CoreMetrics.COMPLEXITY, getMetric("COMPLEXITY")); // The Cyclomatic Complexity Number
      sensorContext.saveMeasure(resource, CoreMetrics.CLASS_COMPLEXITY, getMetric("CLASS_COMPLEXITY")); // Average complexity by class
      sensorContext.saveMeasure(resource, CoreMetrics.FUNCTION_COMPLEXITY, getMetric("FUNCTION_COMPLEXITY")); // Average cyclomatic
                                                                                                              // complexity number by method
      sensorContext.saveMeasure(resource, CoreMetrics.CLASSES, getMetric("CLASSES")); // Number of classes including nested classes,
                                                                                      // interfaces, enums and annotations
      sensorContext.saveMeasure(resource, CoreMetrics.FUNCTIONS, getMetric("FUNCTIONS")); // Number of Methods without including accessors.
                                                                                          // A constructor is considered to be a method.
      sensorContext.saveMeasure(resource, CoreMetrics.ACCESSORS, getMetric("ACCESSORS")); // Number of getter and setter methods used to
                                                                                          // get(reading) or set(writing) a class' property
                                                                                          // .
      sensorContext.saveMeasure(resource, CoreMetrics.PUBLIC_API, getMetric("PUBLIC_API")); // Number of public classes, public methods
                                                                                            // (without accessors) and public properties
                                                                                            // (without public static final ones)
      sensorContext.saveMeasure(resource, CoreMetrics.DEPTH_IN_TREE, getMetric("DEPTH_IN_TREE")); // The depth of inheritance tree (DIT)
                                                                                                  // metric provides for each class a
                                                                                                  // measure of the inheritance levels from
                                                                                                  // the object hierarchy top.
      sensorContext.saveMeasure(resource, CoreMetrics.NUMBER_OF_CHILDREN, getMetric("NUMBER_OF_CHILDREN")); // A class's number of children
                                                                                                            // (NOC) metric simply measures
                                                                                                            // the number of direct and
                                                                                                            // indirect descendants of the
                                                                                                            // class.
      sensorContext.saveMeasure(resource, CoreMetrics.RFC, getMetric("RFC")); // The response set of a class is a set of methods that can
                                                                              // potentially be executed in response to a message received
                                                                              // by an object of that class. RFC is simply the number of
                                                                              // methods in the set.
      sensorContext.saveMeasure(resource, functionDist.build().setPersistenceMode(PersistenceMode.MEMORY));
      sensorContext.saveMeasure(resource, classDist.build().setPersistenceMode(PersistenceMode.MEMORY));
      sensorContext.saveMeasure(resource, fileDist.build().setPersistenceMode(PersistenceMode.MEMORY));
      sensorContext.saveMeasure(resource, rfcDist.build().setPersistenceMode(PersistenceMode.MEMORY));
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
    setMetric("DEPTH_IN_TREE", dit);
    setMetric("NUMBER_OF_CHILDREN", noc);
    setMetric("RFC", rfc); // look for note above
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
    noc = 0;
    rfc = 0;
    dit = 0;
    classDist.clear();
    functionDist.clear();
    fileDist.clear();
    clearMetrics();
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

}