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
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.metrics.BasicMetrics;
import org.sonar.plugins.delphi.metrics.ComplexityMetrics;
import org.sonar.plugins.delphi.metrics.DeadCodeMetrics;
import org.sonar.plugins.delphi.metrics.MetricsInterface;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

/**
 * Main DelphiLanguage sensor class, it executes on DelphiLanguage project and gathers all data
 * through metrics.
 */
public class DelphiSensor implements Sensor {
  private static final Logger LOG = Loggers.get(DelphiSensor.class);
  private final DelphiProjectHelper delphiProjectHelper;
  private final BasicMetrics basicMetrics;
  private final ComplexityMetrics complexityMetrics;
  private final DeadCodeMetrics deadCodeMetrics;

  private int scannedFiles;
  private final List<InputFile> resourceList = new ArrayList<>();
  private final Map<InputFile, Deque<ClassInterface>> fileClasses = new HashMap<>();
  private final Map<InputFile, Deque<FunctionInterface>> fileFunctions = new HashMap<>();
  private final Set<UnitInterface> units = new HashSet<>();
  private DelphiFileStreamConfig fileStreamConfig;

  public DelphiSensor(
      DelphiProjectHelper delphiProjectHelper,
      ActiveRules activeRules,
      SensorContext sensorContext) {
    this.delphiProjectHelper = delphiProjectHelper;

    basicMetrics = new BasicMetrics(sensorContext);
    complexityMetrics = new ComplexityMetrics(activeRules, sensorContext);
    deadCodeMetrics = new DeadCodeMetrics(sensorContext);
  }

  /** Populate {@link SensorDescriptor} of this sensor. */
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("DelphiSensor: Combined LCOV and LOC sensor");
    descriptor.onlyOnLanguage(DelphiLanguage.KEY);
  }

  /*
   * The actual sensor code.
   */
  @Override
  public void execute(@NonNull SensorContext context) {
    LOG.info("Delphi sensor execute...");
    List<DelphiProject> projects = delphiProjectHelper.getProjects();
    for (DelphiProject delphiProject : projects) {
      fileStreamConfig = DelphiFileStream.createConfig(delphiProject, delphiProjectHelper);
      addCoverage(context);
      CodeAnalysisCacheResults.resetCache();
      parseFiles(delphiProject);
      processFiles(context);
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

  /**
   * Calculate metrics for project files
   *
   * @param sensorContext Sensor context (provided by Sonar)
   */
  private void processFiles(SensorContext sensorContext) {
    LOG.info("Processing metrics...");
    ProgressReporter progressReporter =
        new ProgressReporter(resourceList.size(), 10, new ProgressReporterLogger(LOG));

    for (InputFile resource : resourceList) {
      LOG.debug("{} {}", ">> PROCESSING ", resource);

      try {
        processMetric(basicMetrics, resource);
        processMetric(complexityMetrics, resource);
        processMetric(deadCodeMetrics, resource);
      } catch (IllegalArgumentException e) {
        LOG.error(
            "{} produced IllegalArgumentException: \"{}\""
                + " Metric report for this file may be in error.",
            resource,
            e.getMessage());
        LOG.debug("Stacktrace: ", e);
      }

      if (complexityMetrics.hasMetric("PUBLIC_API")) {
        int publicApi = complexityMetrics.getIntMetric("PUBLIC_API");
        int documentedApi = 0;

        if (basicMetrics.hasMetric("PUBLIC_DOC_API")) {
          documentedApi = basicMetrics.getIntMetric("PUBLIC_DOC_API");
        }

        int undocumentedApi = Math.max(publicApi - documentedApi, 0);

        // Number of public API without a documentation block
        sensorContext
            .<Integer>newMeasure()
            .forMetric(CoreMetrics.PUBLIC_UNDOCUMENTED_API)
            .on(resource)
            .withValue(undocumentedApi)
            .save();
      }

      progressReporter.progress();
    }

    LOG.info("Done");
  }

  private void processMetric(MetricsInterface metric, InputFile resource) {
    if (metric.executeOnResource(resource)) {
      metric.analyse(resource, fileClasses.get(resource), fileFunctions.get(resource), units);
      metric.save(resource);
    }
  }

  // for debugging, prints file paths with message to debug file
  private void printFileList(String msg, List<File> list) {
    for (File f : list) {
      LOG.info("{}{}", msg, f.getAbsolutePath());
    }
  }

  /**
   * Parse files with ANTLR
   *
   * @param delphiProject DelphiLanguage project to parse
   */
  private void parseFiles(DelphiProject delphiProject) {
    List<File> includedDirs = delphiProject.getIncludeDirectories();
    List<File> excludedDirs = delphiProjectHelper.getExcludedDirectories();
    List<File> sourceFiles = delphiProject.getSourceFiles();

    printFileList("Included: ", includedDirs);
    printFileList("Excluded: ", excludedDirs);

    LOG.info("{} {}", "Parsing project ", delphiProject.getName());

    ProgressReporter progressReporter =
        new ProgressReporter(sourceFiles.size(), 10, new ProgressReporterLogger(LOG));
    LOG.info("{} {}", "Files to parse: ", sourceFiles.size());

    ASTAnalyzer analyser = new DelphiASTAnalyzer();
    for (File delphiFile : sourceFiles) {
      final CodeAnalysisResults results = parseSourceFile(delphiFile, analyser);
      if (results != null) {
        units.addAll(results.getCachedUnitsAsList());
      }
      progressReporter.progress();
    }

    LOG.info("Done");
  }

  private CodeAnalysisResults parseSourceFile(File sourceFile, ASTAnalyzer analyzer) {
    if (delphiProjectHelper.isExcluded(sourceFile)) {
      return null;
    }

    LOG.debug("{} {}", ">> PARSING ", sourceFile.getAbsolutePath());

    InputFile resource = delphiProjectHelper.getFile(sourceFile);

    resourceList.add(resource);

    final CodeAnalysisResults results = analyseSourceFile(sourceFile, analyzer);

    if (results == null) {
      return null;
    }

    if (results.getActiveUnit() != null) {
      fileClasses.put(resource, results.getClasses());
      fileFunctions.put(resource, results.getFunctions());
    }

    return results;
  }

  /**
   * Analysing a source file with ANTLR
   *
   * @param sourceFile File to analyse
   * @param analyser Source code analyser
   * @return AST Tree
   */
  private CodeAnalysisResults analyseSourceFile(File sourceFile, ASTAnalyzer analyser) {
    final DelphiAST ast = new DelphiAST(sourceFile, fileStreamConfig);

    if (ast.isError()) {
      LOG.error("{} {}", "Error while parsing ", sourceFile.getAbsolutePath());
      return null;
    }

    try {
      final CodeAnalysisResults results = analyser.analyze(ast);
      ++scannedFiles;
      return results;
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "{} {} {} {}",
            "Error analyzing file: ",
            e.getMessage(),
            sourceFile.getAbsolutePath(),
            e);
      } else {
        LOG.error(
            "{} {} {}", "Error analyzing file: ", e.getMessage(), sourceFile.getAbsolutePath());
      }
    }

    return null;
  }

  /**
   * Get the number of processed files
   *
   * @return The number of processed files
   */
  public int getProcessedFilesCount() {
    return scannedFiles;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  Set<UnitInterface> getUnits() {
    return units;
  }

  Map<InputFile, Deque<ClassInterface>> getFileClasses() {
    return fileClasses;
  }

  Map<InputFile, Deque<FunctionInterface>> getFileFunctions() {
    return fileFunctions;
  }
}
