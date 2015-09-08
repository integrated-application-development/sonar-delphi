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
package org.sonar.plugins.delphi.codecoverage.aqtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.codecoverage.CoverageFileData;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

/**
 * AQTime purifier
 */
public class AQTimeCoverageParser implements DelphiCodeCoverageParser {

  private String tablePrefix = "";
  private Map<String, String> connectionProperties;
  private DelphiProjectHelper delphiProjectHelper;

  public AQTimeCoverageParser(DelphiProjectHelper delphiProjectHelper) {
    this.delphiProjectHelper = delphiProjectHelper;
  }

  @Override
  public void parse(SensorContext context) {
    DelphiUtils.LOG.debug("Code Coverage starting...");
    AQTimeCoverageDao aqTimeCoverageDao = new AQTimeCoverageDao();
    aqTimeCoverageDao.setJdbcProps(connectionProperties);
    aqTimeCoverageDao.setDatabasePrefix(tablePrefix);
    List<AQTimeCodeCoverage> aqTimeCodeCoverages = aqTimeCoverageDao.readAQTimeCodeCoverage();
    saveCoverageData(processFiles(aqTimeCodeCoverages), context);
  }

  private void saveCoverageData(Map<InputFile, CoverageFileData> savedResources, SensorContext context) {
    for (CoverageFileData data : savedResources.values()) {
      // TODO sonar.delphi.codecoverage.excluded property
      // if
      // (DelphiProjectHelper.getInstance().isExcluded(data.getResource().file().getPath(),
      // excludedDirs)) {
      // continue; // do NOT save, in excluded
      // }

      Measure<Double> overallCoverage = new Measure<Double>(CoreMetrics.COVERAGE, data.getCoverage());
      Measure<Double> lineCoverage = new Measure<Double>(CoreMetrics.LINE_COVERAGE, data.getCoverage());
      Measure<Double> linesToCover = new Measure<Double>(CoreMetrics.LINES_TO_COVER, data.getTotalLines());
      Measure<Double> uncoveredLines = new Measure<Double>(CoreMetrics.UNCOVERED_LINES, data.getUncoveredLines());
      Measure<?> lineHits = data.getLineHitsBuilder().build().setPersistenceMode(PersistenceMode.DATABASE);

      context.saveMeasure(data.getResource(), overallCoverage);
      context.saveMeasure(data.getResource(), lineCoverage);
      context.saveMeasure(data.getResource(), linesToCover);
      context.saveMeasure(data.getResource(), uncoveredLines);
      context.saveMeasure(data.getResource(), lineHits);
    }
  }

  private Map<InputFile, CoverageFileData> processFiles(List<AQTimeCodeCoverage> aqTimeCodeCoverages) {
    ProgressReporter progressReporter = new ProgressReporter(aqTimeCodeCoverages.size(), 10,
      new ProgressReporterLogger(DelphiUtils.LOG));

    Map<InputFile, CoverageFileData> savedResources = new HashMap<InputFile, CoverageFileData>();

    for (AQTimeCodeCoverage aqTimeCodeCoverage : aqTimeCodeCoverages) {
      progressReporter.progress();

      String path = normalizeFileName(aqTimeCodeCoverage.getCoveredFileName());

      InputFile resource = delphiProjectHelper.getFile(path);
      if (resource == null) {
        continue;
      }
      if (!savedResources.containsKey(resource)) {
        savedResources.put(resource, new CoverageFileData(resource));
      }

      calculateCoverageData(savedResources.get(resource), aqTimeCodeCoverage.getLineNumber(),
        aqTimeCodeCoverage.getLineHits());

    }
    return savedResources;
  }

  private void calculateCoverageData(CoverageFileData fileData, int lineNumber, int lineHits) {
    fileData.setTotalLines(fileData.getTotalLines() + 1);
    if (lineHits == 0) {
      fileData.setUncoveredLines(fileData.getUncoveredLines() + 1);
    }

    fileData.getLineHitsBuilder().add(String.valueOf(lineNumber), lineHits);
  }

  /**
   * Convert relative file name to absolute path
   * 
   * @param fileName Short file name
   * @return Normalized file name
   */
  private String normalizeFileName(String fileName) {
    String path = DelphiUtils.normalizeFileName(fileName);
    // erase '\..' prefixes
    path = path.replaceAll("\\\\\\.\\.", "");
    // erase '..' prefixes
    path = path.replaceAll("\\.\\.", "");

    return path;
  }

  public void setConnectionProperties(Map<String, String> connectionProperties) {
    this.connectionProperties = connectionProperties;
    tablePrefix = connectionProperties.get(DelphiPlugin.JDBC_DB_TABLE_PREFIX_KEY);
  }
}
