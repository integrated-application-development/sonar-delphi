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
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.surefire.data.UnitTestClassReport;
import org.sonar.plugins.surefire.data.UnitTestIndex;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.sonar.plugins.surefire.data.UnitTestResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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

  public void collect(SensorContext context, File reportsDir) {
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

  private static long getTimeAttributeInMS(String value) {
    try {
      Double time = ParsingUtils.parseNumber(value, Locale.ENGLISH);
      return !Double.isNaN(time) ? (long) ParsingUtils.scaleValue(time * 1000.0D, 3) : 0L;
    } catch (ParseException e) {
      return 0L;
    }
  }

  private static UnitTestResult parseTestResult(NamedNodeMap testCase) {
    UnitTestResult detail = new UnitTestResult();
    String name = testCase.getNamedItem("name").getTextContent();
    String classname = testCase.getNamedItem("classname").getTextContent();
    String testCaseName =  StringUtils.contains(classname, "$") ? StringUtils.substringAfter(classname, "$") + "/" + name : name;
    detail.setName(testCaseName);
    String status = "ok";
    String time = testCase.getNamedItem("time").getTextContent();
    Long duration = null;
    String success = testCase.getNamedItem("success").getTextContent();
    if (success != "True") {
      duration = 0L;
      status = "failure";
    }
    if (duration == null) {
      duration = getTimeAttributeInMS(time);
    }

    detail.setDurationMilliseconds(duration);
    detail.setStatus(status);
    return detail;
  }

  void parse(File reportFile, UnitTestIndex index)
  {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      // normalize text representation
      doc.getDocumentElement().normalize();

      NodeList testsuites = doc.getElementsByTagName("testsuite");

      for (int f = 0; f < testsuites.getLength(); f++) {
        Element testSuite = (Element)testsuites.item(f);
        String testSuiteName = testSuite.getAttributes().getNamedItem("name").getTextContent();
        NodeList testCases = testSuite.getElementsByTagName("testcase");
        for (int n = 0; n < testCases.getLength(); n++)
        {
          Node testCase = testCases.item(n);
          String testClassName = testCase.getAttributes().getNamedItem("classname").getTextContent();
//          String testClassName = getClassname(testCase, testSuiteClassName);
          UnitTestClassReport classReport = index.index(testClassName);
          classReport.add(parseTestResult(testCase.getAttributes()));
        }
      }
    } catch (SAXParseException err) {
      DelphiUtils.LOG.info("SAXParseException");
    } catch (SAXException e) {
      DelphiUtils.LOG.info("SAXException");
      Exception x = e.getException ();
      ((x == null) ? e : x).printStackTrace ();
    } catch (Throwable t) {
      DelphiUtils.LOG.info("Throwable");
      t.printStackTrace ();
    }
  }

  private void parseFiles(File[] reports, UnitTestIndex index) {
    for (File report : reports) {
        parse(report, index);
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
    int testsCount = report.getTests() - report.getSkipped();
    context.<Integer>newMeasure().forMetric(CoreMetrics.SKIPPED_TESTS).on(resource).withValue(report.getSkipped()).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.TESTS).on(resource).withValue(testsCount).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.TEST_ERRORS).on(resource).withValue(report.getErrors()).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.TEST_FAILURES).on(resource).withValue(report.getFailures()).save();
    context.<Long>newMeasure().forMetric(CoreMetrics.TEST_EXECUTION_TIME).on(resource).withValue(report.getDurationMilliseconds()).save();

    int passedTests = testsCount - report.getErrors() - report.getFailures();
    if (testsCount > 0) {
      double percentage = passedTests * 100d / testsCount;
      context.<Double>newMeasure().forMetric(CoreMetrics.TEST_SUCCESS_DENSITY).on(resource).withValue(ParsingUtils.scaleValue(percentage)).save();
    }
  }

}
