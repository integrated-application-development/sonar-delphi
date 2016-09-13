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
package org.sonar.plugins.delphi.surefire;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.surefire.data.SurefireStaxHandler;
import org.sonar.plugins.surefire.data.UnitTestClassReport;
import org.sonar.plugins.surefire.data.UnitTestIndex;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Map;

/**
 * Parses unit test reports from XML file.
 */
public class DelphiSureFireParser {

  private static final Logger LOGGER = LoggerFactory
    .getLogger(DelphiSureFireParser.class);

  private static final String FILE_EXT = ".pas";
  private static final String ERROR_MSG = "Unit test file not found: ";
  private final DelphiProjectHelper delphiProjectHelper;

  /**
   * ctor
   * 
   * @param delphiProjectHelper DelphiProjectHelper
   */
  public DelphiSureFireParser(DelphiProjectHelper delphiProjectHelper) {
    this.delphiProjectHelper = delphiProjectHelper;
  }

  protected InputFile getUnitTestResource(String filename) {
    try {
      InputFile testFile = delphiProjectHelper.findTestFileInDirectories(filename);
      if (testFile != null) {
        // resource source code not saved, because tests files were
        // excluded from analysis, so read the test file and save its
        // source code
        // so Sonar could show it
        return testFile;
      }
      throw new FileNotFoundException();
    } catch (FileNotFoundException e) {
      DelphiUtils.LOG.warn(ERROR_MSG + filename + FILE_EXT);
    }
    return null;
  }

  public void collect(Project project, SensorContext context, File reportsDir) {
    File[] xmlFiles = getReports(reportsDir);
    if (xmlFiles.length > 0) {
      parseFiles(context, xmlFiles);
    }
  }

  private File[] getReports(File dir) {
    if (dir == null) {
      return new File[0];
    } else if (!dir.isDirectory()) {
      LOGGER.warn("Reports path not found: " + dir.getAbsolutePath());
      return new File[0];
    }
    File[] unitTestResultFiles = findXMLFilesStartingWith(dir, "TEST-");
    if (unitTestResultFiles.length == 0) {
      // maybe there's only a test suite result file
      unitTestResultFiles = findXMLFilesStartingWith(dir, "TESTS-");
    }
    return unitTestResultFiles;
  }

  private File[] findXMLFilesStartingWith(File dir, final String fileNameStart) {
    return dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(fileNameStart) && name.endsWith(".xml");
      }
    });
  }

  private void parseFiles(SensorContext context, File[] reports) {
    UnitTestIndex index = new UnitTestIndex();
    parseFiles(reports, index);
    sanitize(index);
    save(index, context);
  }

  private void parseFiles(File[] reports, UnitTestIndex index) {
    SurefireStaxHandler staxParser = new SurefireStaxHandler(index);
    StaxParser parser = new StaxParser((StaxParser.XmlStreamHandler) staxParser, false);
    for (File report : reports) {
      try {
        parser.parse(report);
      } catch (XMLStreamException e) {
        throw new RuntimeException(
          "Fail to parse the Surefire report: " + report, e);
      }
    }
  }

  private void sanitize(UnitTestIndex index) {
    for (String classname : index.getClassnames()) {
      if (StringUtils.contains(classname, "$")) {
        // Surefire reports classes whereas sonar supports files
        String parentClassName = StringUtils.substringBefore(classname, "$");
        index.merge(classname, parentClassName);
      }
    }
  }

  private void save(UnitTestIndex index, SensorContext context) {
    for (Map.Entry<String, UnitTestClassReport> entry : index.getIndexByClassname().entrySet()) {
      UnitTestClassReport report = entry.getValue();
      if (report.getTests() > 0) {
        InputFile resource = getUnitTestResource(entry.getKey());
        if (resource != null) {
          save(entry.getValue(), resource, context);
        } else {
          LOGGER.warn("Resource not found: {}", entry.getKey());
        }
      }
    }
  }

  private void save(UnitTestClassReport report, InputFile resource,
    SensorContext context) {
    double testsCount = report.getTests() - report.getSkipped();
    saveMeasure(context, resource, CoreMetrics.SKIPPED_TESTS, report.getSkipped());
    saveMeasure(context, resource, CoreMetrics.TESTS, testsCount);
    saveMeasure(context, resource, CoreMetrics.TEST_ERRORS, report.getErrors());
    saveMeasure(context, resource, CoreMetrics.TEST_FAILURES, report.getFailures());
    saveMeasure(context, resource, CoreMetrics.TEST_EXECUTION_TIME, report.getDurationMilliseconds());
    double passedTests = testsCount - report.getErrors() - report.getFailures();
    if (testsCount > 0) {
      double percentage = passedTests * 100d / testsCount;
      saveMeasure(context, resource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
    }
  }

  private void saveMeasure(SensorContext context, InputFile resource,
    Metric<? extends Number> metric, double value) {
    if (!Double.isNaN(value)) {
      context.saveMeasure(resource, metric, value);
    }
  }

}
