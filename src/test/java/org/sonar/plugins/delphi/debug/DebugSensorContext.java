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
package org.sonar.plugins.delphi.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.Event;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.design.Dependency;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilter;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.ProjectLink;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;

/**
 * Debug class used in DelphiSensorTest. It proviedes some overriden functions, in order to get additional information from sensor, not
 * provided by DelphiSensor.
 */
public class DebugSensorContext implements SensorContext {

  private Map<String, Double> data = new HashMap<String, Double>();
  private Map<String, String> sdata = new HashMap<String, String>();
  private List<Violation> violations = new ArrayList<Violation>();

  /**
   * {@inheritDoc}
   */
  
  public void saveViolation(Violation violation) {
    violations.add(violation);
  }

  /**
   * Gets the violation by its index
   * 
   * @return violation
   */
  public Violation getViolation(int index) {
    return violations.get(index);
  }

  /**
   * Gets violation count
   * 
   * @return Violation count
   */
  public int getViolationsCount() {
    return violations.size();
  }

  /**
   * Unused, not implemented
   */
  
  public Measure getMeasure(Metric metric) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Measure getMeasure(String key) {
    if ( !data.containsKey(key)) {
      if ( !sdata.containsKey(key)) {
        throw new IllegalStateException("No key (" + key + ") for sensor context.");
      }
      Measure m = new Measure();
      m.setData(sdata.get(key));
      return m;
    }
    Measure m = new Measure();
    m.setValue(data.get(key));
    m.setData(key);
    return m;
  }

  /**
   * Unused, not implemented
   */
  
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

  /**
   * {@inheritDoc}
   */
  
  public Measure saveMeasure(Resource resource, Metric metric, Double value) {
    data.put(resource.getName() + ".pas:" + metric.getKey(), value);
    return null;
  }

  /**
   * {@inheritDoc}
   */
  
  public Measure saveMeasure(Resource resource, Measure measure) {
    if (resource == null || measure == null) {
      return null;
    }
    if (measure.getValue() != null) {
      data.put(resource.getName() + ".pas:" + measure.getMetric().getKey(), measure.getValue());
    } else {
      sdata.put(resource.getName() + ".pas:" + measure.getMetric().getKey(), measure.getData());
    }
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Measure saveMeasure(Measure measure) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Measure saveMeasure(Metric metric, Double value) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Measure getMeasure(Resource resource, Metric metric) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public String saveResource(Resource resource) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public <M> M getMeasures(Resource resource, MeasuresFilter<M> filter) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public void saveViolations(Collection<Violation> violations) {
  }

  /**
   * Unused, not implemented
   */
  
  public Dependency saveDependency(Dependency dependency) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Set<Dependency> getDependencies() {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Collection<Dependency> getIncomingDependencies(Resource to) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Collection<Dependency> getOutgoingDependencies(Resource from) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public void saveSource(Resource resource, String source) {

  }

  /**
   * Unused, not implemented
   */
  
  public void saveLink(ProjectLink link) {
  }

  /**
   * Unused, not implemented
   */
  
  public void deleteLink(String key) {
  }

  /**
   * Unused, not implemented
   */
  
  public List<Event> getEvents(Resource resource) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Event createEvent(Resource resource, String name, String description, String category, Date date) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public void deleteEvent(Event event) {
  }

  /**
   * Unused, not implemented
   */
  
  public boolean index(Resource resource) {
    return false;
  }

  /**
   * Unused, not implemented
   */
  
  public boolean index(Resource resource, Resource parentReference) {
    return false;
  }

  /**
   * Unused, not implemented
   */
  
  public boolean isExcluded(Resource reference) {
    return false;
  }

  /**
   * Unused, not implemented
   */
  
  public boolean isIndexed(Resource reference, boolean acceptExcluded) {
    return false;
  }

  /**
   * Unused, not implemented
   */
  
  public Resource getResource(Resource reference) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Resource getParent(Resource reference) {
    return null;
  }

  /**
   * Unused, not implemented
   */
  
  public Collection<Resource> getChildren(Resource reference) {
    return null;
  }

  /**
   * Save forced violation
   */
  
  public void saveViolation(Violation violation, boolean force) {
    saveViolation(violation);
  }

}
