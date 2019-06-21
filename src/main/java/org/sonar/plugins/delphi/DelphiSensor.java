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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.antlr.runtime.Token;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.sanitizer.DelphiSourceSanitizer;
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
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

/**
 * Main DelphiLanguage sensor class, it executes on DelphiLanguage project and gathers all data
 * through metrics.
 */
public class DelphiSensor implements Sensor {

  private int scannedFiles;
  private List<InputFile> resourceList = new ArrayList<>();
  private Map<InputFile, List<ClassInterface>> fileClasses = new HashMap<>();
  private Map<InputFile, List<FunctionInterface>> fileFunctions = new HashMap<>();
  private Set<UnitInterface> units = new HashSet<>();
  private final List<File> excluded;

  private final DelphiProjectHelper delphiProjectHelper;
  private final BasicMetrics basicMetrics;
  private final ComplexityMetrics complexityMetrics;
  private final DeadCodeMetrics deadCodeMetrics;

  public DelphiSensor(DelphiProjectHelper delphiProjectHelper, ActiveRules activeRules,
      SensorContext sensorContext) {
    DelphiUtils.LOG.info("Delphi sensor DelphiSensor...");
    this.delphiProjectHelper = delphiProjectHelper;

    basicMetrics = new BasicMetrics(sensorContext);
    complexityMetrics = new ComplexityMetrics(activeRules, sensorContext);
    deadCodeMetrics = new DeadCodeMetrics(activeRules, sensorContext);
    this.excluded = delphiProjectHelper.getExcludedSources();
  }

  /**
   * Populate {@link SensorDescriptor} of this sensor.
   */
  @Override
  public void describe(SensorDescriptor descriptor) {
    DelphiUtils.LOG.info("Delphi sensor describe...");
    descriptor.name("Combined LCOV and LOC sensor");
    descriptor.onlyOnLanguage(DelphiLanguage.KEY);
  }

  @Override
  /*
   * The actual sensor code.
   */
  public void execute(SensorContext context) {
    DelphiUtils.LOG.info("Delphi sensor execute...");
    List<DelphiProject> projects = delphiProjectHelper.getProjects();
    for (DelphiProject delphiProject : projects) {
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
              .forEach(path -> {
                DelphiCodeCoverageParser coverageParser = new DelphiCodeCoverageToolParser(
                    path.toFile(),
                    delphiProjectHelper);
                coverageParser.parse(context);
              });
        } catch (IOException e) {
          DelphiUtils.LOG.error("Error while parsing Coverage Reports:", e);
        }
      }
    }
  }

  private boolean canTokenize(String fileName) {
    Set<String> includedFiles = DelphiSourceSanitizer.getIncludedFiles();
    return !includedFiles.contains(fileName) && !delphiProjectHelper.isExcluded(fileName, excluded);
  }

  private void doTokenize(SensorContext context, InputFile inputFile) {
    try {
      String fileName = delphiProjectHelper.uriToAbsolutePath(inputFile.uri());
      DelphiLexer lexer = new DelphiLexer(new DelphiSourceSanitizer(fileName));
      Token prevToken = null;
      Token token = lexer.nextToken();
      NewCpdTokens cpdTokens = context.newCpdTokens().onFile(inputFile);
      while (token.getType() != Token.EOF) {
        if (prevToken != null) {
          TextRange endLineRange = inputFile.selectLine(token.getLine());
          int endPosition = Math
              .min(endLineRange.end().lineOffset(), token.getCharPositionInLine());
          TextRange startLineRange = inputFile.selectLine(prevToken.getLine());
          int startPosition = Math
              .min(startLineRange.end().lineOffset(), prevToken.getCharPositionInLine());
          int startLine = prevToken.getLine();
          int endLine = token.getLine();
          if (endLine <= startLine && startPosition < endPosition) {
            cpdTokens.addToken(startLine, startPosition, endLine, endPosition, prevToken.getText());
          }
        }
        prevToken = token;
        token = lexer.nextToken();
      }
      cpdTokens.save();
    } catch (FileNotFoundException ex) {
      DelphiUtils.LOG.error("{} {} {}", "Cpd could not find : ", inputFile.toString(), ex);
    } catch (IOException ex) {
      DelphiUtils.LOG.error("{} {} {}", "Cpd IO Exception on ", inputFile.toString(), ex);
    }
  }

  /**
   * Calculate metrics for project files
   *
   * @param sensorContext Sensor context (provided by Sonar)
   */
  private void processFiles(SensorContext sensorContext) {
    DelphiUtils.LOG.info("Processing metrics...");
    ProgressReporter progressReporter = new ProgressReporter(resourceList.size(), 10,
        new ProgressReporterLogger(
            DelphiUtils.LOG));

    for (InputFile resource : resourceList) {
      DelphiUtils.LOG.debug("{} {}", ">> PROCESSING ", resource);

      try {
        processMetric(basicMetrics, resource);
        processMetric(complexityMetrics, resource);
        processMetric(deadCodeMetrics, resource);
      } catch (IllegalArgumentException e) {
        // Some files may produce invalid pointers due to code that is valid for the compiler but
        // not for the scanner, this will handle that so the execution does not fail
        DelphiUtils.LOG.error("{} produced IllegalArgumentException: \"{}\""
            + " Metric report for this file may be in error.", resource, e.getMessage());
      }

      if (basicMetrics.hasMetric("PUBLIC_DOC_API") && complexityMetrics.hasMetric("PUBLIC_API")) {
        int undocumentedApi = DelphiUtils.checkIntRange(
            complexityMetrics.getIntMetric("PUBLIC_API") - basicMetrics
                .getIntMetric("PUBLIC_DOC_API"), 0, Integer.MAX_VALUE);

        // Number of public API without a documentation block
        sensorContext.<Integer>newMeasure().forMetric(CoreMetrics.PUBLIC_UNDOCUMENTED_API)
            .on(resource).withValue(undocumentedApi).save();
      }

      String fileName = resource.filename();
      if (canTokenize(fileName)) {
        doTokenize(sensorContext, resource);
      }
      progressReporter.progress();
    }

    DelphiUtils.LOG.info("Done");
  }

  public void processMetric(MetricsInterface metric, InputFile resource) {
    if (metric.executeOnResource(resource)) {
      metric.analyse(resource, fileClasses.get(resource), fileFunctions.get(resource), units);
      metric.save(resource);
    }
  }

  // for debugging, prints file paths with message to debug file
  private void printFileList(String msg, List<File> list) {
    for (File f : list) {
      DelphiUtils.LOG.info("{}{}", msg, f.getAbsolutePath());
    }
  }

  /**
   * Parse files with ANTLR
   *
   * @param delphiProject DelphiLanguage project to parse
   */
  protected void parseFiles(DelphiProject delphiProject) {
    List<File> includedDirs = delphiProject.getIncludeDirectories();
    List<File> excludedDirs = delphiProjectHelper.getExcludedSources();
    List<File> sourceFiles = delphiProject.getSourceFiles();
    List<String> definitions = delphiProject.getDefinitions();

    DelphiSourceSanitizer.setIncludeDirectories(includedDirs);
    DelphiSourceSanitizer.setDefinitions(definitions);

    printFileList("Included: ", includedDirs);
    printFileList("Excluded: ", excludedDirs);

    DelphiUtils.LOG.info("{} {}", "Parsing project ", delphiProject.getName());

    ProgressReporter progressReporter = new ProgressReporter(sourceFiles.size(), 10,
        new ProgressReporterLogger(
            DelphiUtils.LOG));
    DelphiUtils.LOG.info("{} {}", "Files to parse: ", sourceFiles.size());

    ASTAnalyzer analyser = new DelphiASTAnalyzer(delphiProjectHelper);
    for (File delphiFile : sourceFiles) {
      final CodeAnalysisResults results = parseSourceFile(delphiFile, excludedDirs, analyser);
      if (results != null) {
        units.addAll(results.getCachedUnitsAsList());
      }
      progressReporter.progress();
    }

    DelphiUtils.LOG.info("Done");
  }

  private CodeAnalysisResults parseSourceFile(File sourceFile, List<File> excludedDirs,
      ASTAnalyzer analyzer) {
    if (delphiProjectHelper.isExcluded(sourceFile, excludedDirs)) {
      return null;
    }

    DelphiUtils.LOG.debug("{} {}", ">> PARSING ", sourceFile.getAbsolutePath());

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
    final DelphiAST ast = new DelphiAST(sourceFile, delphiProjectHelper.encoding());

    if (ast.isError()) {
      DelphiUtils.LOG.error("{} {}", "Error while parsing ", sourceFile.getAbsolutePath());
      return null;
    }

    try {
      final CodeAnalysisResults results = analyser.analyze(ast);
      ++scannedFiles;
      return results;
    } catch (Exception e) {
      if (DelphiUtils.LOG.isDebugEnabled()) {
        DelphiUtils.LOG
            .debug("{} {} {} {}", "Error analyzing file: ", e.getMessage(),
                sourceFile.getAbsolutePath(), e);
      } else {
        DelphiUtils.LOG
            .error("{} {} {}", "Error analyzing file: ", e.getMessage(),
                sourceFile.getAbsolutePath());
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

  Map<InputFile, List<ClassInterface>> getFileClasses() {
    return fileClasses;
  }

  Map<InputFile, List<FunctionInterface>> getFileFunctions() {
    return fileFunctions;
  }
}
