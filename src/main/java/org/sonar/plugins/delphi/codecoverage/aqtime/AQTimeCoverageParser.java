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

    private String prefix = ""; // table prefix
    private List<InputFile> sourceFiles;
    private Map<String, String> connectionProperties;
    private DelphiProjectHelper delphiProjectHelper;

    public AQTimeCoverageParser(DelphiProjectHelper delphiProjectHelper) {
        this.delphiProjectHelper = delphiProjectHelper;
    }

    public void parse(SensorContext context) {
        DelphiUtils.LOG.debug("Code Coverage starting...");
        AQTimeCoverageDao aqTimeCoverageDao = new AQTimeCoverageDao();
        aqTimeCoverageDao.setJdbcProps(connectionProperties);
        aqTimeCoverageDao.setDatabasePrefix(prefix);
        List<AQTimeCodeCoverage> aqTimeCodeCoverages = aqTimeCoverageDao.readAQTimeCodeCoverage();
        saveCoverageData(processFiles(aqTimeCodeCoverages), context);
    }

    private void saveCoverageData(Map<InputFile, CoverageFileData> savedResources, SensorContext context) {
        for (CoverageFileData data : savedResources.values()) // save all
                                                              // resources
        {
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

        InputFile resource = null; // current file

        for (AQTimeCodeCoverage aqTimeCodeCoverage : aqTimeCodeCoverages) {
            progressReporter.progress();

            // convert relative file name to absolute path
            String path = processFileName(aqTimeCodeCoverage.getCoveredFileName(), sourceFiles);

            if (resource == null || !resource.absolutePath().equals(aqTimeCodeCoverage.getCoveredFileName())) {
                resource = delphiProjectHelper.getFile(path);
                if (resource == null) {
                    continue;
                }
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

        fileData.getLineHitsBuilder().add(String.valueOf(lineNumber), lineHits); // add
                                                                                 // new
                                                                                 // line
                                                                                 // hit
    }

    /**
     * Gets source file path for file name
     * 
     * @param fileName Short file name
     * @param sourceFiles List of source files
     * @return Full path to file name
     */
    private String processFileName(String fileName, List<InputFile> sourceFiles) {
        String path = DelphiUtils.normalizeFileName(fileName);
        path = path.replaceAll("\\\\\\.\\.", ""); // erase '\..' prefixes
        path = path.replaceAll("\\.\\.", ""); // erase '..' prefixes

        for (InputFile source : sourceFiles) { // searching for a proper file in
                                               // files list
            String sourcePath = DelphiUtils.normalizeFileName(source.file().getAbsolutePath());
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

    public void setConnectionProperties(Map<String, String> connectionProperties) {
        this.connectionProperties = connectionProperties;
        prefix = connectionProperties.get(DelphiPlugin.JDBC_DB_TABLE_PREFIX_KEY);
    }
}
