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
package au.com.integradev.delphi;

import static au.com.integradev.delphi.utils.DelphiUtils.inputFilesToPaths;
import static au.com.integradev.delphi.utils.DelphiUtils.stopProgressReport;

import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.executor.DelphiMasterExecutor;
import au.com.integradev.delphi.executor.ExecutorContext;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.file.DelphiFile.DelphiFileConstructionException;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonarsource.analyzer.commons.ProgressReport;

public class DelphiSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiSensor.class);

  private final DelphiProjectHelper delphiProjectHelper;
  private final DelphiMasterExecutor executor;

  /**
   * Dependency-injection constructor
   *
   * @param delphiProjectHelper Helper class for navigating delphi projects
   * @param executor Executes analysis on each file
   */
  public DelphiSensor(DelphiProjectHelper delphiProjectHelper, DelphiMasterExecutor executor) {
    this.executor = executor;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Delphi.KEY).name("DelphiSensor");
  }

  /** The actual sensor code. */
  @Override
  public void execute(@Nonnull SensorContext context) {
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
    LOG.info("Compiler version: {}", compilerVersion.number());
    LOG.info("Conditional defines: {}", delphiProjectHelper.getConditionalDefines());

    var preprocessorFactory = new DelphiPreprocessorFactory(toolchain.platform);
    var typeFactory = new TypeFactoryImpl(toolchain, compilerVersion);
    Iterable<InputFile> inputFiles = delphiProjectHelper.inputFiles();
    List<Path> sourceFiles = inputFilesToPaths(inputFiles);
    List<Path> referencedFiles = delphiProjectHelper.getReferencedFiles();
    List<Path> searchPathDirectories = new ArrayList<>();
    searchPathDirectories.addAll(delphiProjectHelper.getSearchDirectories());
    searchPathDirectories.addAll(delphiProjectHelper.getDebugSourceDirectories());
    SearchPath searchPath = SearchPath.create(searchPathDirectories);

    SymbolTable symbolTable =
        SymbolTable.builder()
            .preprocessorFactory(preprocessorFactory)
            .typeFactory(typeFactory)
            .sourceFiles(sourceFiles)
            .referencedFiles(referencedFiles)
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
            preprocessorFactory,
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
          LOG.error("Error while analyzing {}", absolutePath, e);
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
}
