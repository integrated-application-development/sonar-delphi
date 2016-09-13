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

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.sanitizer.DelphiSourceSanitizer;
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

import java.io.File;
import java.util.*;

/**
 * Main DelphiLanguage sensor class, it executes on DelphiLanguage project and
 * gathers all data through metrics.
 */
public class DelphiSensor implements Sensor {

  private int scannedFiles = 0;
  private Set<Directory> packageList = new HashSet<>();
  private Map<Directory, Integer> filesCount = new HashMap<>();
  private List<InputFile> resourceList = new ArrayList<InputFile>();
  private Map<InputFile, List<ClassInterface>> fileClasses = new HashMap<InputFile, List<ClassInterface>>();
  private Map<InputFile, List<FunctionInterface>> fileFunctions = new HashMap<InputFile, List<FunctionInterface>>();
  private Set<UnitInterface> units = new HashSet<>();

  private final DelphiProjectHelper delphiProjectHelper;
  private final BasicMetrics basicMetrics;
  private final ComplexityMetrics complexityMetrics;
  private final DeadCodeMetrics deadCodeMetrics;

  public DelphiSensor(DelphiProjectHelper delphiProjectHelper, ActiveRules activeRules,
    ResourcePerspectives perspectives) {
    this.delphiProjectHelper = delphiProjectHelper;

    basicMetrics = new BasicMetrics();
    complexityMetrics = new ComplexityMetrics(activeRules, perspectives);
    deadCodeMetrics = new DeadCodeMetrics(activeRules, perspectives);
  }

  /**
   * Determines if sensor should execute on project
   * 
   * @return true if we are analysing DelphiLanguage project, false otherwise
   */

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return delphiProjectHelper.shouldExecuteOnProject();
  }

  /**
   * Analyses whole project with all metrics
   */

  @Override
  public void analyse(Project project, SensorContext sensorContext) {
    List<DelphiProject> projects = delphiProjectHelper.getWorkgroupProjects();
    for (DelphiProject delphiProject : projects)
    {
      CodeAnalysisCacheResults.resetCache();
      parseFiles(delphiProject, project);
      parsePackages(sensorContext);

      processFiles(sensorContext);
    }
  }

  /**
   * Calculate metrics for project files
   *
   * @param sensorContext Sensor context (provided by Sonar)
   */
  private void processFiles(SensorContext sensorContext) {
    DelphiUtils.LOG.info("Processing metrics...");
    ProgressReporter progressReporter = new ProgressReporter(resourceList.size(), 10, new ProgressReporterLogger(
      DelphiUtils.LOG));

    for (InputFile resource : resourceList) {
      DelphiUtils.LOG.debug(">> PROCESSING " + resource.file().getPath());

      processMetric(basicMetrics, sensorContext, resource);
      processMetric(complexityMetrics, sensorContext, resource);
      processMetric(deadCodeMetrics, sensorContext, resource);

      if (basicMetrics.hasMetric("PUBLIC_DOC_API") && complexityMetrics.hasMetric("PUBLIC_API")) {
        double undocumentedApi = DelphiUtils.checkRange(complexityMetrics.getMetric("PUBLIC_API") - basicMetrics.getMetric("PUBLIC_DOC_API"), 0.0, Double.MAX_VALUE);

        // Number of public API without a documentation block
        sensorContext.saveMeasure(resource, CoreMetrics.PUBLIC_UNDOCUMENTED_API, undocumentedApi);
      }

      progressReporter.progress();
    }

    DelphiUtils.LOG.info("Done");
  }

  public void processMetric(MetricsInterface metric, SensorContext sensorContext, InputFile resource) {
    if (metric.executeOnResource(resource)) {
      metric.analyse(resource, sensorContext, fileClasses.get(resource), fileFunctions.get(resource),
        units);
      InputFile inputFile = delphiProjectHelper.getFile(resource.file().getAbsolutePath());
      metric.save(inputFile, sensorContext);
    }
  }

  /**
   * Count the metrics for packages
   * 
   * @param sensorContext Sensor context (provided by Sonar)
   */
  private void parsePackages(SensorContext sensorContext) {
    for (Directory directory : packageList) {
      sensorContext.saveMeasure(directory, CoreMetrics.DIRECTORIES, 1.0);
      sensorContext.saveMeasure(directory, CoreMetrics.FILES, (double) filesCount.get(directory));
    }
  }

  // for debugging, prints file paths with message to debug file
  private void printFileList(String msg, List<File> list) {
    for (File f : list) {
      DelphiUtils.LOG.info(msg + f.getAbsolutePath());
    }
  }

  /**
   * Parse files with ANTLR
   * 
   * @param delphiProject DelphiLanguage project to parse
   * @param project Project
   */
  protected void parseFiles(DelphiProject delphiProject, Project project) {
    List<File> includedDirs = delphiProject.getIncludeDirectories();
    List<File> excludedDirs = delphiProjectHelper.getExcludedSources();
    List<File> sourceFiles = delphiProject.getSourceFiles();
    List<String> definitions = delphiProject.getDefinitions();

    DelphiSourceSanitizer.setIncludeDirectories(includedDirs);
    DelphiSourceSanitizer.setDefinitions(definitions);

    printFileList("Included: ", includedDirs);
    printFileList("Excluded: ", excludedDirs);

    DelphiUtils.LOG.info("Parsing project " + delphiProject.getName());

    ProgressReporter progressReporter = new ProgressReporter(sourceFiles.size(), 10, new ProgressReporterLogger(
      DelphiUtils.LOG));
    DelphiUtils.LOG.info("Files to parse: " + sourceFiles.size());

    ASTAnalyzer analyser = new DelphiASTAnalyzer(delphiProjectHelper);
    for (File delphiFile : sourceFiles) {
      final CodeAnalysisResults results = parseSourceFile(delphiFile, excludedDirs, analyser, project);
      if (results != null) {
        units.addAll(results.getCachedUnitsAsList());
      }
      progressReporter.progress();
    }

    DelphiUtils.LOG.info("Done");
  }

  private CodeAnalysisResults parseSourceFile(File sourceFile, List<File> excludedDirs, ASTAnalyzer analyzer,
    Project project) {
    if (delphiProjectHelper.isExcluded(sourceFile, excludedDirs)) {
      return null;
    }

    DelphiUtils.LOG.debug(">> PARSING " + sourceFile.getAbsolutePath());

    InputFile resource = delphiProjectHelper.getFile(sourceFile);

    Directory directory = delphiProjectHelper.getDirectory(sourceFile.getParentFile(), project);

    if (directory == null) {
      throw new IllegalArgumentException("Directory: " + sourceFile.getParentFile() + " not found.");
    }

    packageList.add(directory);

    if (filesCount.containsKey(directory)) {
      filesCount.put(directory, filesCount.get(directory) + 1);
    } else {
      filesCount.put(directory, Integer.valueOf(1));
    }
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
      DelphiUtils.LOG.error("Error while parsing " + sourceFile.getAbsolutePath());
      return null;
    }

    try {
      final CodeAnalysisResults results = analyser.analyze(ast);
      ++scannedFiles;
      return results;
    } catch (Exception e) {
      if (DelphiUtils.LOG.isDebugEnabled()) {
        DelphiUtils.LOG.debug("Error analyzing file: " + e.getMessage() + " " + sourceFile.getAbsolutePath(), e);
      } else {
        DelphiUtils.LOG.error("Error analyzing file: " + e.getMessage() + " " + sourceFile.getAbsolutePath());
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
