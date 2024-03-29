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
import au.com.integradev.delphi.coverage.DelphiCoverageParser;
import au.com.integradev.delphi.coverage.DelphiCoverageParserFactory;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

public class DelphiCoverageSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiCoverageSensor.class);

  private final DelphiProjectHelper delphiProjectHelper;
  private final DelphiCoverageParserFactory coverageParserFactory;

  /**
   * Dependency-injection constructor
   *
   * @param delphiProjectHelper Helper class for navigating delphi projects
   * @param coverageParserFactory Provides a coverage parsers
   */
  public DelphiCoverageSensor(
      DelphiProjectHelper delphiProjectHelper, DelphiCoverageParserFactory coverageParserFactory) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.coverageParserFactory = coverageParserFactory;
  }

  /** Populate {@link SensorDescriptor} of this sensor. */
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Delphi.KEY).name("DelphiCoverageSensor");
  }

  /** The actual sensor code. */
  @Override
  public void execute(@Nonnull SensorContext context) {
    if (shouldExecuteOnProject()) {
      addCoverage(context);
    }
  }

  private void addCoverage(SensorContext context) {
    LOG.info("Adding coverage reports.");
    DelphiCoverageParser parser = coverageParserFactory.create();
    addCoverage(context, parser);
  }

  private static void addCoverage(SensorContext context, DelphiCoverageParser parser) {
    String[] paths = context.config().getStringArray(DelphiProperties.COVERAGE_REPORT_KEY);
    if (paths == null || paths.length == 0) {
      LOG.info(
          "No coverage reports specified (see '{}' property)",
          DelphiProperties.COVERAGE_REPORT_KEY);
    } else {
      Arrays.stream(paths).forEach(path -> addCoverage(context, path, parser));
    }
  }

  private static void addCoverage(SensorContext context, String path, DelphiCoverageParser parser) {
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
