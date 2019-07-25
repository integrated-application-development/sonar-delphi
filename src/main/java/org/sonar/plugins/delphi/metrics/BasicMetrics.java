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

import java.io.Reader;
import java.io.StringReader;
import java.util.Deque;
import java.util.Set;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.metrics.basicmetrics.DelphiSource;
import org.sonar.plugins.delphi.metrics.basicmetrics.Metric;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/** Class calculating basic file metrics: lines of code and comments, documentation. */
public class BasicMetrics extends DefaultMetrics {
  private static final Logger LOG = Loggers.get(BasicMetrics.class);
  private final SensorContext context;

  public BasicMetrics(SensorContext sensorContext) {
    super();
    context = sensorContext;
  }

  @Override
  public void analyse(
      InputFile resource,
      Deque<ClassInterface> classes,
      Deque<FunctionInterface> functions,
      Set<UnitInterface> units) {
    clearMetrics();

    try (Reader reader = new StringReader(resource.contents())) {
      DelphiSource source = new DelphiSource(reader);
      setIntMetric("NCLOC", source.getMeasure(Metric.LINES_OF_CODE));
      setIntMetric("COMMENT_LINES", source.getMeasure(Metric.COMMENT_LINES));
      setIntMetric("COMMENT_BLANK_LINES", source.getMeasure(Metric.COMMENT_BLANK_LINES));
      setIntMetric("PUBLIC_DOC_API", source.getMeasure(Metric.PUBLIC_DOC_API));
    } catch (Exception e) {
      LOG.error("BasicMetrics::analyse() -- Can not analyse the file " + resource.toString(), e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void save(InputFile resource) {
    context
        .<Integer>newMeasure()
        .forMetric(CoreMetrics.NCLOC)
        .on(resource)
        .withValue(getIntMetric("NCLOC"))
        .save();
    context
        .<Integer>newMeasure()
        .forMetric(CoreMetrics.COMMENT_LINES)
        .on(resource)
        .withValue(getIntMetric("COMMENT_LINES"))
        .save();
  }

  /** {@inheritDoc} */
  @Override
  public boolean executeOnResource(InputFile resource) {
    return DelphiUtils.acceptFile(resource.filename());
  }
}
