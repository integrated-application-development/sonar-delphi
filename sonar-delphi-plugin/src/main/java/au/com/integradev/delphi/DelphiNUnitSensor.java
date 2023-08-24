/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi;

import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.nunit.DelphiNUnitParser;
import au.com.integradev.delphi.nunit.ResultsAggregator;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/** Detects and parses Delphi NUnit test reports. */
public class DelphiNUnitSensor implements Sensor {
  private static final Logger LOG = Loggers.get(DelphiNUnitSensor.class);

  private final Configuration configuration;

  public DelphiNUnitSensor(Configuration settings) {
    this.configuration = settings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Delphi.KEY).name("Delphi NUnit Sensor");
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /** The actual sensor code. */
  @Override
  public void execute(@Nonnull SensorContext context) {
    LOG.info("NUnit sensor execute...");
    String[] paths = configuration.getStringArray(DelphiProperties.NUNIT_REPORT_PATHS_PROPERTY);

    if (paths == null || paths.length == 0) {
      LOG.info(
          "No NUnit report directories specified (see '{}' property)",
          DelphiProperties.NUNIT_REPORT_PATHS_PROPERTY);
      return;
    }

    String mainPath = context.fileSystem().baseDir().getAbsolutePath();
    Arrays.stream(paths)
        .map(path -> DelphiUtils.resolveAbsolutePath(mainPath, path))
        .forEach(
            reportDir -> {
              if (reportDir.exists()) {
                collect(context, reportDir);
              } else {
                LOG.warn("Report path not found {}", reportDir.getAbsolutePath());
              }
            });
  }

  protected void collect(SensorContext context, File reportsDir) {
    save(context, DelphiNUnitParser.collect(reportsDir));
  }

  private void save(SensorContext context, ResultsAggregator results) {
    int testsRun = results.getTestsRun();
    context
        .<Integer>newMeasure()
        .forMetric(CoreMetrics.TESTS)
        .on(context.project())
        .withValue(testsRun)
        .save();
    context
        .<Integer>newMeasure()
        .forMetric(CoreMetrics.SKIPPED_TESTS)
        .on(context.project())
        .withValue(results.getSkipped())
        .save();
    context
        .<Integer>newMeasure()
        .forMetric(CoreMetrics.TEST_FAILURES)
        .on(context.project())
        .withValue(results.getFailures())
        .save();
    context
        .<Long>newMeasure()
        .forMetric(CoreMetrics.TEST_EXECUTION_TIME)
        .on(context.project())
        .withValue((long) (results.getDurationSeconds() * 1000))
        .save();
  }
}
