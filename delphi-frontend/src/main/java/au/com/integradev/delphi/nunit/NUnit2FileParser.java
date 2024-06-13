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

import au.com.integradev.delphi.nunit.TestResult.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NUnit2FileParser extends NUnitFileParser {
  private static final Logger LOG = LoggerFactory.getLogger(NUnit2FileParser.class);

  @Override
  protected TestResult parseTestResult(NamedNodeMap testCase) throws NUnitParseException {
    String status = getNodeTextOrExcept(testCase, "result");
    Node durationNode = testCase.getNamedItem("time");
    double duration =
        getTimeAttributeInSeconds(durationNode == null ? "" : durationNode.getTextContent());

    return new TestResult(parseTestCaseStatus(status), duration);
  }

  private TestResult.Status parseTestCaseStatus(String statusText) {
    switch (statusText.toLowerCase()) {
      case "success":
        return Status.PASSED;
      case "ignored":
        return Status.SKIPPED;
      case "failure":
      case "error":
        return Status.FAILED;
      default:
        LOG.warn("Unexpected test result status: '{}'. Treating as Failure.", statusText);
        return Status.FAILED;
    }
  }
}
