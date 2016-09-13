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

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;

import java.util.List;
import java.util.Set;

/**
 * Interface for metrics
 */
public interface MetricsInterface {

  /**
   * Analyse given DelphiLanguage source file
   * 
   * @param resource DelphiLanguage source file
   * @param sensorContext Given by Sonar
   * @param classes Classes in source file
   * @param functions Functions in source file
   * @param units Units in project
   */
  void analyse(InputFile resource, SensorContext sensorContext, List<ClassInterface> classes,
    List<FunctionInterface> functions,
    Set<UnitInterface> units);

  /**
   * Saves analysis result from sensorContext and associates it with resource
   * 
   * @param inputFile Resource to associate analysis results with
   * @param sensorContext Sensor context
   */
  void save(InputFile inputFile, SensorContext sensorContext);

  /**
   * Gets custom metric
   * 
   * @param metric Metric name
   * @throws IllegalStateException if the metric was not set before while
   *             analysing
   * @return double Metric value
   */
  double getMetric(String metric);

  /**
   * Gets the metric keys, for you to know what metrics are avaible
   * 
   * @return Metrics keys
   */
  String[] getMetricKeys();

  /**
   * Should metric execute on provided resource?
   * 
   * @param resource Resource to check
   * @return True if metric should execute, false otherwise
   */
  boolean executeOnResource(InputFile resource);

  /**
   * Verify if a metric exists
   * 
   * @param metric Metric name
   * @return true if the metric exists
   */
  boolean hasMetric(String metric);
}
