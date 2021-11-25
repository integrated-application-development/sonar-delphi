package org.sonar.plugins.delphi.nunit;

import java.io.File;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/** Detects and parses Delphi NUnit test reports. */
public class DelphiNUnitSensor implements Sensor {
  private static final Logger LOG = Loggers.get(DelphiNUnitSensor.class);

  private final Configuration configuration;

  public DelphiNUnitSensor(Configuration settings) {
    this.configuration = settings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(DelphiLanguage.KEY).name("Delphi NUnit Sensor");
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /** The actual sensor code. */
  @Override
  public void execute(@NotNull SensorContext context) {
    LOG.info("NUnit sensor execute...");
    String[] paths = configuration.getStringArray(DelphiPlugin.NUNIT_REPORT_PATHS_PROPERTY);

    if (paths == null || paths.length == 0) {
      LOG.info(
          "No NUnit report directories specified (see '{}' property)",
          DelphiPlugin.NUNIT_REPORT_PATHS_PROPERTY);
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
