package org.sonar.plugins.delphi.coverage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.project.DelphiProjectHelper;

public class DelphiCoverageSensor implements Sensor {
  private static final Logger LOG = Loggers.get(DelphiCoverageSensor.class);

  private final DelphiProjectHelper delphiProjectHelper;
  private final DelphiCoverageParserFactory coverageParserFactory;

  /**
   * Dependency-injection constructor
   *
   * @param delphiProjectHelper Helper class for navigating delphi projects
   * @param coverageParserFactory provides a coverage parser provided its key
   */
  public DelphiCoverageSensor(
      DelphiProjectHelper delphiProjectHelper, DelphiCoverageParserFactory coverageParserFactory) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.coverageParserFactory = coverageParserFactory;
  }

  /** Populate {@link SensorDescriptor} of this sensor. */
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(DelphiLanguage.KEY).name("DelphiCoverageSensor");
  }

  /** The actual sensor code. */
  @Override
  public void execute(@NotNull SensorContext context) {
    if (shouldExecuteOnProject()) {
      addCoverage(context);
    }
  }

  private void addCoverage(SensorContext context) {
    LOG.info("Adding coverage reports.");
    context
        .config()
        .get(DelphiPlugin.COVERAGE_TOOL_KEY)
        .flatMap(
            key -> {
              LOG.info("Coverage tool: '{}'.", key);
              return coverageParserFactory.getParser(key, delphiProjectHelper);
            })
        .ifPresentOrElse(
            coverageParser -> addCoverage(context, coverageParser),
            () ->
                LOG.info(
                    "No coverage tool specified (see '{}' property)",
                    DelphiPlugin.COVERAGE_TOOL_KEY));
  }

  private void addCoverage(SensorContext context, DelphiCoverageParser parser) {
    String[] paths = context.config().getStringArray(DelphiPlugin.COVERAGE_REPORT_KEY);
    if (paths == null || paths.length == 0) {
      LOG.info(
          "No coverage reports specified (see '{}' property)", DelphiPlugin.COVERAGE_REPORT_KEY);
    } else {
      Arrays.stream(paths).forEach(path -> addCoverage(context, path, parser));
    }
  }

  private void addCoverage(SensorContext context, String path, DelphiCoverageParser parser) {
    try (Stream<Path> coverageReportStream = Files.walk(Path.of(path))) {
      coverageReportStream
          .filter(Files::isRegularFile)
          .map(Path::toFile)
          .forEach(file -> parser.parse(context, file));
    } catch (IOException | InvalidPathException e) {
      LOG.error("Error while parsing coverage reports:", e);
    }
  }

  private boolean shouldExecuteOnProject() {
    return delphiProjectHelper.shouldExecuteOnProject();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
