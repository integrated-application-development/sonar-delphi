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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.compiler.CompilerVersion;
import org.sonar.plugins.delphi.compiler.Toolchain;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.executor.ExecutorContext;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.msbuild.DelphiProjectHelper;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;
import org.sonar.plugins.delphi.symbol.SymbolTable;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
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
  public void execute(@NotNull SensorContext context) {
    if (shouldExecuteOnProject()) {
      executor.setup();
      executeOnFiles(context);
      executor.complete();
    }
  }

  private void executeOnFiles(SensorContext sensorContext) {
    Toolchain toolchain = delphiProjectHelper.getToolchain();
    CompilerVersion compilerVersion = delphiProjectHelper.getCompilerVersion();

    LOG.info("Platform: {}", toolchain.platform.name());
    LOG.info("Architecture: {}", toolchain.architecture.name());
    LOG.info("Compiler version: {}", compilerVersion.number().toString());
    LOG.info("Conditional defines: {}", delphiProjectHelper.getConditionalDefines());

    TypeFactory typeFactory = new TypeFactory(toolchain, compilerVersion);
    Iterable<InputFile> inputFiles = delphiProjectHelper.mainFiles();
    List<Path> sourceFiles = inputFilesToPaths(inputFiles);
    SearchPath searchPath = SearchPath.create(delphiProjectHelper.getSearchDirectories());

    SymbolTable symbolTable =
        SymbolTable.builder()
            .typeFactory(typeFactory)
            .sourceFiles(sourceFiles)
            .encoding(delphiProjectHelper.encoding())
            .searchPath(searchPath)
            .conditionalDefines(delphiProjectHelper.getConditionalDefines())
            .unitScopeNames(delphiProjectHelper.getUnitScopeNames())
            .unitAliases(delphiProjectHelper.getUnitAliases())
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
            typeFactory,
            searchPath,
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
