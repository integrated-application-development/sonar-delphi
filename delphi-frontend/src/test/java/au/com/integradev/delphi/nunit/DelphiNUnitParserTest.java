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

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DelphiNUnitParserTest {
  @Test
  void testCollectNonExistentDirectory() {
    ResultsAggregator results = DelphiNUnitParser.collect(new File(UUID.randomUUID().toString()));

    // no results should be recorded
    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isZero();
    assertThat(results.getFailures()).isZero();
    assertThat(results.getSkipped()).isZero();
    assertThat(results.getDurationSeconds()).isZero();
  }

  @Test
  void testCollectFromParentDir() {
    ResultsAggregator results = getResults(getPath("v3"));

    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(20);
  }

  @ParameterizedTest
  @ValueSource(strings = {"v2", "v3"})
  void testParseReport(String version) {
    ResultsAggregator results = getResults(getPath(version) + "/normal");

    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(8);
    assertThat(results.getSkipped()).isEqualTo(1);
    assertThat(results.getFailures()).isEqualTo(3);
  }

  @ParameterizedTest
  @ValueSource(strings = {"v2", "v3"})
  void testParseReportWithUnusualStatusCasing(String version) {
    ResultsAggregator results = getResults(getPath(version) + "/unusualStatusCasing");

    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(8);
    assertThat(results.getSkipped()).isEqualTo(1);
    assertThat(results.getFailures()).isEqualTo(3);
  }

  @ParameterizedTest
  @ValueSource(strings = {"v2", "v3"})
  void testCollectMalformedReport(String version) {
    ResultsAggregator results = getResults(getPath(version) + "/malformedXml");

    // malformed XML should be ignored (the directory also includes a single well-formed test-case)
    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(1);
    assertThat(results.getFailures()).isZero();
    assertThat(results.getSkipped()).isZero();
    assertThat(results.getDurationSeconds()).isEqualTo(0.704);
  }

  @ParameterizedTest
  @ValueSource(strings = {"v2", "v3"})
  void testCollectReportWithBadResultAttributes(String version) {
    ResultsAggregator results = getResults(getPath(version) + "/badResultAttributes");

    // test-case missing a 'result' attribute should be ignored.
    // test-case with invalid 'result' attribute should be treated as a failure.
    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(1);
    assertThat(results.getFailures()).isEqualTo(1);
    assertThat(results.getSkipped()).isZero();
    assertThat(results.getDurationSeconds()).isEqualTo(0.002);
  }

  @ParameterizedTest
  @ValueSource(strings = {"v2", "v3"})
  void testCollectReportWithBadDurationAttributes(String version) {
    ResultsAggregator results = getResults(getPath(version) + "/badDurationAttributes");

    // missing or invalid duration should be treated as zero
    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(2);
    assertThat(results.getFailures()).isZero();
    assertThat(results.getSkipped()).isZero();
    assertThat(results.getDurationSeconds()).isZero();
  }

  private static ResultsAggregator getResults(String path) {
    return DelphiNUnitParser.collect(DelphiUtils.getResource(path));
  }

  private static String getPath(String versionText) {
    return String.format("/au/com/integradev/delphi/nunit/reports/%s", versionText);
  }
}
