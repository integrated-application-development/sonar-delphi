/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.codecoverage.aqtime;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

/**
 * AQTime purifier
 */
public class AQTimeCoverageParser {

  private static final double ONE_HUNDRED = 100.00;
  private String prefix = ""; // table prefix
  private List<InputFile> sourceFiles;
  private List<File> sourceDirs;
  private List<File> excludedDirs;
  private Map<String, String> connectionProperties;

  /**
   * {@inheritDoc}
   */
  public void parse(Project project, SensorContext context) {
    DelphiUtils.getDebugLog().println(">> CODE COVERAGE STARTING");
    DelphiUtils.getDebugLog().println(">> DB PREFIX: " + prefix);
    try {
      AQTimeCoverageDao aqTimeCoverageDao = new AQTimeCoverageDao();
      aqTimeCoverageDao.setJdbcProps(connectionProperties);
      aqTimeCoverageDao.setDatabasePrefix(prefix);
      List<AQTimeCodeCoverage> aqTimeCodeCoverages = aqTimeCoverageDao.readAQTimeCodeCoverage();
      saveCoverageData(processFiles(aqTimeCodeCoverages), context);
    } catch (SQLException e) {
      DelphiUtils.LOG.error("AQTime SQL error: " + e.getMessage());
      DelphiUtils.getDebugLog().println("AQTime SQL error: " + e.getMessage());
    }
  }

  private void saveCoverageData(Map<DelphiFile, CoverageFileData> savedResources, SensorContext context) {
    for (CoverageFileData data : savedResources.values()) // save all resources
    {
      if (DelphiProjectHelper.getInstance().isExcluded(data.getResource().getPath(), excludedDirs)) {
        continue; // do NOT save, in excluded
      }
      Measure overallCoverage = new Measure(CoreMetrics.COVERAGE, data.getCoverage());
      Measure lineCoverage = new Measure(CoreMetrics.LINE_COVERAGE, data.getCoverage());
      Measure linesToCover = new Measure(CoreMetrics.LINES_TO_COVER, data.getTotalLines());
      Measure uncoveredLines = new Measure(CoreMetrics.UNCOVERED_LINES, data.getUncoveredLines());
      Measure lineHits = data.getLineHitsBuilder().build().setPersistenceMode(PersistenceMode.DATABASE);
      context.saveMeasure(data.getResource(), overallCoverage); // save overall file coverage
      context.saveMeasure(data.getResource(), lineCoverage); // save file coverage
      context.saveMeasure(data.getResource(), linesToCover); // save total lines to cover
      context.saveMeasure(data.getResource(), uncoveredLines); // save uncovered lines
      context.saveMeasure(data.getResource(), lineHits); // save line hits data
    }
  }

  private Map<DelphiFile, CoverageFileData> processFiles(List<AQTimeCodeCoverage> aqTimeCodeCoverages) throws SQLException {
    DelphiUtils.getDebugLog().println("Processing files...");
    
    ProgressReporter progressReporter = new ProgressReporter(aqTimeCodeCoverages.size(), 10, new ProgressReporterLogger(DelphiUtils.LOG) );
    
    Map<DelphiFile, CoverageFileData> savedResources = new HashMap<DelphiFile, CoverageFileData>(); // map of our sources and their data
    DelphiFile resource = null; // current file

    for (AQTimeCodeCoverage aqTimeCodeCoverage : aqTimeCodeCoverages) {
      progressReporter.progress();
      String path = processFileName(aqTimeCodeCoverage.getCoveredFileName(), sourceFiles); // convert relative file name to absolute path
      if (resource == null || !resource.getPath().equals(path)) {
        resource = DelphiFile.fromAbsolutePath(path, sourceDirs, false);
        if (resource == null) {
          continue;
        }
        savedResources.put(resource, new CoverageFileData(resource));
      }
      
      calculateCoverageData(savedResources.get(resource), aqTimeCodeCoverage.getLineNumber(), aqTimeCodeCoverage.getLineHits());

    }
    return savedResources;
  }

  private void calculateCoverageData(CoverageFileData fileData, int lineNumber, int lineHits) {
    fileData.setTotalLines(fileData.getTotalLines() + 1);
    if (lineHits == 0) {
      fileData.setUncoveredLines(fileData.getUncoveredLines() + 1);
    }
    if (fileData.getTotalLines() == 0) {
      fileData.setCoverage(0.00);
    } else {
      fileData.setCoverage(((fileData.getTotalLines() - fileData.getUncoveredLines()) / fileData.getTotalLines()) * ONE_HUNDRED);
    }
    fileData.getLineHitsBuilder().add(String.valueOf(lineNumber), lineHits); // add new line hit
  }

  /**
   * Gets source file path for file name
   * 
   * @param fileName
   *          Short file name
   * @param sourceFiles
   *          List of source files
   * @return Full path to file name
   */
  private String processFileName(String fileName, List<InputFile> sourceFiles) {
    String path = DelphiUtils.normalizeFileName(fileName);
    path = path.replaceAll("\\\\\\.\\.", ""); // erase '\..' prefixes
    path = path.replaceAll("\\.\\.", "");     // erase '..' prefixes

    for (InputFile source : sourceFiles) { // searching for a proper file in files list
      String sourcePath = DelphiUtils.normalizeFileName(source.getFile().getAbsolutePath());
      if (sourcePath.endsWith(path)) {
        path = sourcePath;
        break;
      }
    }

    return path;
  }

  public void setSourceFiles(List<InputFile> sourceFiles) {
    this.sourceFiles = sourceFiles;
  }

  public void setSourceDirectories(List<File> sourceDirs) {
    this.sourceDirs = sourceDirs;
  }

  public void setExcludeDirectories(List<File> excludedDirs) {
    this.excludedDirs = excludedDirs;
  }

  public void setConnectionProperties(Map<String, String> connectionProperties) {
    this.connectionProperties = connectionProperties;
    prefix = connectionProperties.get(DelphiPlugin.JDBC_DB_TABLE_PREFIX_KEY);
  }
}
