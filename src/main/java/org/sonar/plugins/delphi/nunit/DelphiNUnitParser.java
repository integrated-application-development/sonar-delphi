package org.sonar.plugins.delphi.nunit;

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
import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Parses NUnit test reports from XML files. */
public class DelphiNUnitParser {
  private static class MissingNodeException extends RuntimeException {
    private MissingNodeException(String msg) {
      super(msg);
    }
  }

  private static final Logger LOG = Loggers.get(DelphiNUnitParser.class);

  private DelphiNUnitParser() {}

  public static ResultsAggregator collect(@NotNull File reportsDir) {
    LOG.info("Processing reports in {}", reportsDir);
    return parseFiles(getReports(reportsDir));
  }

  private static Collection<File> getReports(@NotNull File path) {
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
        LOG.warn("Skipping test case because of exception while parsing: {}", e);
      }
    }
  }

  private static ResultsAggregator parseFiles(Collection<File> reports) {
    ResultsAggregator results = new ResultsAggregator();
    reports.forEach(report -> parse(report, results));
    return results;
  }
}
