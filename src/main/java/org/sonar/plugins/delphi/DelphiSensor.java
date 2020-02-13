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

import static org.sonar.plugins.delphi.utils.DelphiUtils.inputFilesToPaths;
import static org.sonar.plugins.delphi.utils.DelphiUtils.stopProgressReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.executor.ExecutorContext;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.project.DelphiProjectHelper;
import org.sonar.plugins.delphi.symbol.SymbolTable;
import org.sonarsource.analyzer.commons.ProgressReport;

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
      executeOnFiles(context);
      executor.complete();
      addCoverage(context);
    }
  }

  private void executeOnFiles(SensorContext sensorContext) {
    LOG.info("Conditional defines: {}", delphiProjectHelper.getConditionalDefines());

    Iterable<InputFile> inputFiles = delphiProjectHelper.mainFiles();
    List<Path> sourceFiles = inputFilesToPaths(inputFiles);

    SymbolTable symbolTable =
        SymbolTable.builder()
            .sourceFiles(sourceFiles)
            .encoding(delphiProjectHelper.encoding())
            .searchDirectories(delphiProjectHelper.getSearchDirectories())
            .conditionalDefines(delphiProjectHelper.getConditionalDefines())
            .unitScopeNames(delphiProjectHelper.getUnitScopeNames())
            .standardLibraryPath(delphiProjectHelper.standardLibraryPath())
            .build();

    ProgressReport progressReport =
        new ProgressReport(
            "Report about progress of DelphiSensor analysis", TimeUnit.SECONDS.toMillis(10));

    progressReport.start(sourceFiles.stream().map(Path::toString).collect(Collectors.toList()));

    ExecutorContext executorContext = new ExecutorContext(sensorContext, symbolTable);
    DelphiFileConfig config =
        DelphiFile.createConfig(
            delphiProjectHelper.encoding(),
            delphiProjectHelper.getSearchDirectories(),
            delphiProjectHelper.getConditionalDefines());

    boolean success = false;

    try {
      for (Path sourceFile : sourceFiles) {
        String absolutePath = sourceFile.toAbsolutePath().toString();
        try {
          InputFile inputFile = delphiProjectHelper.getFile(absolutePath);
          DelphiInputFile delphiFile = DelphiInputFile.from(inputFile, config);
          executor.execute(executorContext, delphiFile);
          progressReport.nextFile();
        } catch (DelphiFileConstructionException e) {
          String error = String.format("Error while analyzing %s", absolutePath);
          LOG.error(error, e);
          errors.add(error);
        }
      }
      success = true;
    } finally {
      stopProgressReport(progressReport, success);
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
