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
package org.sonar.plugins.delphi.debug;

import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.api.config.Settings;
import org.sonar.api.design.Dependency;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilter;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.Version;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Debug class used in DelphiSensorTest. It proviedes some overriden functions,
 * in order to get additional information from sensor, not provided by
 * DelphiSensor.
 */
@SuppressWarnings("rawtypes")
public class DebugSensorContext implements SensorContext {

  private Map<String, Double> data = new HashMap<String, Double>();
  private Map<String, String> sdata = new HashMap<String, String>();
  /**
   * Gets the violation by its index
   * 
   * @return violation
   */

  /**
   * Gets violation count
   * 
   * @return Violation count
   */

  @Override
  public <G extends Serializable> Measure<G> getMeasure(Metric<G> metric) {
    return null;
  }

  public <G extends Serializable> Measure<G> getMeasure(String key) {
    if (!data.containsKey(key)) {
      if (!sdata.containsKey(key)) {
        throw new IllegalStateException("No key (" + key + ") for sensor context.");
      }
      Measure<G> m = new Measure<G>();
      m.setData(sdata.get(key));
      return m;
    }
    Measure<G> m = new Measure<G>();
    m.setValue(data.get(key));
    m.setData(key);
    return m;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public <M> M getMeasures(MeasuresFilter<M> filter) {
    return null;
  }

  /**
   * Get measure keys
   * 
   * @return Keys
   */
  public Set<String> getMeasuresKeys() {
    return data.keySet();
  }

  @Override
  public Measure<?> saveMeasure(Resource resource, Metric metric, Double value) {
    data.put(resource.getName() + ":" + metric.getKey(), value);
    return null;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public Measure saveMeasure(Resource resource, Measure measure) {
    if (resource == null || measure == null) {
      return null;
    }
    if (measure.getValue() != null) {
      data.put(resource.getKey() + ":" + measure.getMetric().getKey(), measure.getValue());
    } else {
      sdata.put(resource.getKey() + ":" + measure.getMetric().getKey(), measure.getData());
    }
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public Measure saveMeasure(Measure measure) {
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public Measure saveMeasure(Metric metric, Double value) {
    return null;
  }

  @Override
  public <G extends Serializable> Measure<G> getMeasure(Resource resource, Metric<G> metric) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public String saveResource(Resource resource) {
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public <M> M getMeasures(Resource resource, MeasuresFilter<M> filter) {
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public Dependency saveDependency(Dependency dependency) {
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public void saveSource(Resource resource, String source) {

  }

  /**
   * Unused, not implemented
   */

  @Override
  public boolean index(Resource resource) {
    return false;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public boolean index(Resource resource, Resource parentReference) {
    return false;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public boolean isExcluded(Resource reference) {
    return false;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public boolean isIndexed(Resource reference, boolean acceptExcluded) {
    return false;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public <R extends Resource> R getResource(R reference) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  @Override
  public Resource getResource(InputPath inputPath) {
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public Resource getParent(Resource reference) {
    return null;
  }

  /**
   * Unused, not implemented
   */

  @Override
  public Collection<Resource> getChildren(Resource reference) {
    return null;
  }

  @Override
  public Measure saveMeasure(InputFile inputFile, Metric metric, Double value) {
    data.put(inputFile.file().getName() + ":" + metric.getKey(), value);
    return null;
  }

  @Override
  public Measure saveMeasure(InputFile inputFile, Measure measure) {
    if (inputFile == null || measure == null) {
      return null;
    }
    if (measure.getValue() != null) {
      data.put(inputFile.file().getName() + ":" + measure.getMetric().getKey(), measure.getValue());
    } else {
      sdata.put(inputFile.file().getName() + ":" + measure.getMetric().getKey(), measure.getData());
    }
    return null;
  }

  @Override
  public Settings settings() {
    return null;
  }

  @Override
  public FileSystem fileSystem() {
    return null;
  }

  @Override
  public ActiveRules activeRules() {
    return null;
  }

  /**
   * @since 5.5
   */
  @Override
  public InputModule module() {
    return null;
  }

  /**
   * @since 5.5
   */
  @Override
  public Version getSonarQubeVersion() {
    return null;
  }

  /**
   * Get analysis mode.
   */

  public AnalysisMode analysisMode() {
    return null;
  }



  @Override
  public <G extends Serializable> NewMeasure<G> newMeasure() {
    return null;
  }

  @Override
  public NewIssue newIssue() {
    return null;
  }

  @Override
  public NewHighlighting newHighlighting() {
    return null;
  }

  /**
   * Builder to define symbol table of a file. Don't forget to call {@link NewSymbolTable#save()} once all symbols are provided.
   *
   * @since 5.6
   */
  @Override
  public NewSymbolTable newSymbolTable() {
    return null;
  }





  @Override
  public NewCoverage newCoverage() {
    return null;
  }

  /**
   * Builder to define CPD tokens in a file.
   * Don't forget to call {@link NewCpdTokens#save()}.
   *
   * @since 5.5
   */
  @Override
  public NewCpdTokens newCpdTokens() {
    return null;
  }

}
