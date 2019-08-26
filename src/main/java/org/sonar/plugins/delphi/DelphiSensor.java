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
package org.sonar.plugins.delphi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

/** PMD sensor */
public class DelphiSensor implements Sensor {
  private static final Logger LOG = Loggers.get(DelphiSensor.class);

  private final DelphiProjectHelper delphiProjectHelper;
  private final DelphiMasterExecutor executor;
  private final List<String> errors;

  /**
   * Dependency-injection constructor
   *
   * @param delphiProjectHelper Helper class for navigating delphi projects
   * @param executor Executes analysis on each file
   */
  public DelphiSensor(DelphiProjectHelper delphiProjectHelper, DelphiMasterExecutor executor) {
    this.executor = executor;
    this.delphiProjectHelper = delphiProjectHelper;
    this.errors = new ArrayList<>();
  }

  /** Populate {@link SensorDescriptor} of this sensor. */
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(DelphiLanguage.KEY).name("DelphiSensor");
  }

  /** The actual sensor code. */
  @Override
  public void execute(@NonNull SensorContext context) {
    if (shouldExecuteOnProject()) {
      executor.setup();
      for (DelphiProject delphiProject : delphiProjectHelper.getProjects()) {
        executeOnProject(delphiProject, context);
      }
      executor.complete();
      addCoverage(context);
    }
  }

  private void executeOnProject(DelphiProject delphiProject, SensorContext context) {
    LOG.info("Parsing project: {}", delphiProject.getName());

    ProgressReporter progressReporter =
        new ProgressReporter(
            delphiProject.getSourceFiles().size(), 10, new ProgressReporterLogger(LOG));

    var fileStreamConfig = DelphiFileStream.createConfig(delphiProject, delphiProjectHelper);

    for (File sourceFile : delphiProject.getSourceFiles()) {
      try {
        InputFile inputFile = delphiProjectHelper.getFile(sourceFile.getAbsolutePath());
        DelphiFile delphiFile = DelphiFile.from(inputFile, fileStreamConfig);
        executor.execute(context, delphiFile);
      } catch (DelphiFileConstructionException e) {
        String error = String.format("Error while parsing %s", sourceFile.getAbsolutePath());
        LOG.error(error, e);
        errors.add(error);
      }

      progressReporter.progress();
    }
  }

  private void addCoverage(SensorContext context) {
    Optional<String> coverageTool = context.config().get(DelphiPlugin.CODECOVERAGE_TOOL_KEY);
    if (coverageTool.isPresent() && coverageTool.get().equals("dcc")) {
      Optional<String> coverageReport = context.config().get(DelphiPlugin.CODECOVERAGE_REPORT_KEY);

      if (coverageReport.isPresent()) {
        Path coverageReportDir = Paths.get(coverageReport.get());
        try (Stream<Path> coverageReportStream = Files.walk(coverageReportDir)) {
          coverageReportStream
              .filter(Files::isRegularFile)
              .filter(DelphiCodeCoverageToolParser::isCodeCoverageReport)
              .forEach(
                  path -> {
                    DelphiCodeCoverageParser coverageParser =
                        new DelphiCodeCoverageToolParser(path.toFile(), delphiProjectHelper);
                    coverageParser.parse(context);
                  });
        } catch (IOException e) {
          LOG.error("Error while parsing Coverage Reports:", e);
        }
      }
    }
  }

  private boolean shouldExecuteOnProject() {
    return delphiProjectHelper.shouldExecuteOnProject();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public List<String> getErrors() {
    return errors;
  }
}
