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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.delphi.core.DelphiRecognizer;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.squidbridge.measures.Metric;
import org.sonar.squid.text.delphi.DelphiSource;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

/**
 * Class calculating basic file metrics: lines of code and comments,
 * documentation.
 */
public class BasicMetrics extends DefaultMetrics implements MetricsInterface {

  private final SensorContext context;

  public BasicMetrics(SensorContext sensorContext) {
    super();
    context = sensorContext;
  }

  @Override
  public void analyse(InputFile resource, List<ClassInterface> classes,
    List<FunctionInterface> functions,
    Set<UnitInterface> units) {
    clearMetrics();
    Reader reader = null;
    try {
      reader = new StringReader(FileUtils.readFileToString(new File(resource.absolutePath())));
      DelphiSource source = new DelphiSource(reader, new DelphiRecognizer());
      setIntMetric("LINES", source.getMeasure(Metric.LINES));
      setIntMetric("NCLOC", source.getMeasure(Metric.LINES_OF_CODE));
      setIntMetric("COMMENT_LINES", source.getMeasure(Metric.COMMENT_LINES));
      setIntMetric("COMMENT_BLANK_LINES", source.getMeasure(Metric.COMMENT_BLANK_LINES));
      setIntMetric("PUBLIC_DOC_API", source.getMeasure(Metric.PUBLIC_DOC_API));
      setIntMetric("FILES", 1);
    } catch (Exception e) {
      DelphiUtils.LOG.error("BasicMetrics::analyse() -- Can not analyse the file " + resource.absolutePath(), e);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void save(InputFile resource) {
    context.<Integer>newMeasure().forMetric(CoreMetrics.LINES).on(resource).withValue(getIntMetric("LINES")).save();
    // Number of physical lines of code -
    // number of blank lines -
    // number of comment lines -
    // number of header file comments -
    // commented-out lines of code
    context.<Integer>newMeasure().forMetric(CoreMetrics.NCLOC).on(resource).withValue(getIntMetric("NCLOC")).save();
    // Number of javadoc, multi-comment and single-comment lines.
    // Empty comment lines like, header file comments (mainly used to define the license)
    // and commented-out lines of code are not included.
    context.<Integer>newMeasure().forMetric(CoreMetrics.COMMENT_LINES).on(resource).withValue(getIntMetric("COMMENT_LINES")).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.FILES).on(resource).withValue(getIntMetric("FILES")).save();
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean executeOnResource(InputFile resource) {
    return DelphiUtils.acceptFile(resource.absolutePath());
  }

}
