/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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

import java.text.ParseException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.ParsingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class NUnitFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(NUnitFileParser.class);

  public void parse(Document doc, ResultsAggregator results) {
    doc.getDocumentElement().normalize();
    parseTestCases(results, doc.getElementsByTagName("test-case"));
  }

  private void parseTestCases(ResultsAggregator results, NodeList testCases) {
    for (int i = 0; i < testCases.getLength(); i++) {
      Node testCase = testCases.item(i);
      try {
        results.add(parseTestResult(testCase.getAttributes()));
      } catch (NUnitParseException e) {
        LOG.warn("Skipping test case because of exception while parsing:", e);
      }
    }
  }

  protected abstract TestResult parseTestResult(NamedNodeMap testCase) throws NUnitParseException;

  protected double getTimeAttributeInSeconds(String value) {
    try {
      double time = ParsingUtils.parseNumber(value, Locale.ENGLISH);
      return !Double.isNaN(time) ? time : 0;
    } catch (ParseException e) {
      LOG.warn("Couldn't parse time attribute: {}", value);
      return 0;
    }
  }

  protected String getNodeTextOrExcept(NamedNodeMap map, String nodeName)
      throws NUnitParseException {
    Node node = map.getNamedItem(nodeName);
    if (node == null) {
      throw new NUnitParseException(String.format("Node '%s' was missing.", nodeName));
    } else {
      return node.getTextContent();
    }
  }
}
