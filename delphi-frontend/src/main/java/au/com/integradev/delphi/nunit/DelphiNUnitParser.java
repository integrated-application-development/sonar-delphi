/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.nunit;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.ParsingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Parses NUnit test reports from XML files. */
public final class DelphiNUnitParser {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiNUnitParser.class);

  private DelphiNUnitParser() {
    // Utility class
  }

  public static ResultsAggregator collect(File reportsDir) {
    LOG.info("Processing reports in {}", reportsDir);
    return parseFiles(getReports(reportsDir));
  }

  private static Collection<File> getReports(File path) {
    if (!path.isDirectory()) {
      LOG.warn("Reports directory not found: {}", path.getAbsolutePath());
      return Collections.emptyList();
    } else {
      Collection<File> files = findXmlFiles(path);
      LOG.info("Found {} XML files in {}", files.size(), path);
      return files;
    }
  }

  private static Collection<File> findXmlFiles(File dir) {
    return FileUtils.listFiles(
        dir, FileFilterUtils.suffixFileFilter(".xml"), TrueFileFilter.INSTANCE);
  }

  private static double getTimeAttributeInSeconds(String value) {
    try {
      double time = ParsingUtils.parseNumber(value, Locale.ENGLISH);
      return !Double.isNaN(time) ? time : 0;
    } catch (ParseException e) {
      LOG.warn("Couldn't parse time attribute: {}", value);
      return 0;
    }
  }

  private static String getNodeTextOrExcept(NamedNodeMap map, String nodeName)
      throws MissingNodeException {
    Node node = map.getNamedItem(nodeName);
    if (node == null) {
      throw new MissingNodeException(String.format("Node '%s' was missing.", nodeName));
    } else {
      return node.getTextContent();
    }
  }

  private static TestResult parseTestResult(NamedNodeMap testCase) throws MissingNodeException {
    String status = getNodeTextOrExcept(testCase, "result");
    Node duration = testCase.getNamedItem("duration");
    double durationSeconds =
        getTimeAttributeInSeconds(duration == null ? "" : duration.getTextContent());

    return new TestResult().setDurationSeconds(durationSeconds).setStatus(status);
  }

  private static void parse(File reportFile, ResultsAggregator results) {
    try {
      LOG.debug("Parsing NUnit report {}", reportFile);
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      doc.getDocumentElement().normalize();

      parseTestCases(results, doc.getElementsByTagName("test-case"));
    } catch (SAXException | ParserConfigurationException | IOException e) {
      LOG.error("Error while parsing report '{}':", reportFile, e);
    }
  }

  private static void parseTestCases(ResultsAggregator results, NodeList testCases) {
    for (int i = 0; i < testCases.getLength(); i++) {
      Node testCase = testCases.item(i);
      try {
        results.add(parseTestResult(testCase.getAttributes()));
      } catch (MissingNodeException e) {
        LOG.warn("Skipping test case because of exception while parsing:", e);
      }
    }
  }

  private static ResultsAggregator parseFiles(Collection<File> reports) {
    ResultsAggregator results = new ResultsAggregator();
    reports.forEach(report -> parse(report, results));
    return results;
  }

  private static final class MissingNodeException extends RuntimeException {
    private MissingNodeException(String msg) {
      super(msg);
    }
  }
}
