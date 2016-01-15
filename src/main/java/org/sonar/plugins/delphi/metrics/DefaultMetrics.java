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

import java.util.HashMap;
import java.util.Map;

/**
 * Metric class providing default behavior
 */
public abstract class DefaultMetrics implements MetricsInterface {

  protected Map<String, Double> metrics = new HashMap<String, Double>();

  /**
   * {@inheritDoc}
   */

  @Override
  public String[] getMetricKeys() {
    return metrics.keySet().toArray(new String[metrics.keySet().size()]);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public double getMetric(String metric) {
    if (!metrics.containsKey(metric)) {
      throw new IllegalStateException("No metric (" + metric + ") for " + this);
    }
    return metrics.get(metric);
  }

  /**
   * {@inheritDoc}
   */
  public void setMetric(String metric, double value) {
    metrics.put(metric, value);
  }

  protected void clearMetrics() {
    metrics.clear();
  }

  @Override
  public boolean hasMetric(String metric) {
    return metrics.containsKey(metric);
  }
}
