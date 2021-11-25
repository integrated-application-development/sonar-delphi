package org.sonar.plugins.delphi.nunit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiNUnitParserTest {

  private ResultsAggregator getResults(String path) {
    return DelphiNUnitParser.collect(DelphiUtils.getResource(path));
  }

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
    ResultsAggregator results = getResults("/org/sonar/plugins/delphi/nunit/reports/");

    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(12);
  }

  @Test
  void testCollectMalformedReport() {
    ResultsAggregator results = getResults("/org/sonar/plugins/delphi/nunit/reports/malformedXml");

    // malformed XML should be ignored (the directory also includes a single well-formed test-case)
    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(1);
    assertThat(results.getFailures()).isZero();
    assertThat(results.getSkipped()).isZero();
    assertThat(results.getDurationSeconds()).isEqualTo(0.704);
  }

  @Test
  void testCollectReportWithBadResultAttributes() {
    ResultsAggregator results =
        getResults("/org/sonar/plugins/delphi/nunit/reports/badResultAttributes");

    // test-case missing a 'result' attribute should be ignored.
    // test-case with invalid 'result' attribute should be treated as a failure.
    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(1);
    assertThat(results.getFailures()).isEqualTo(1);
    assertThat(results.getSkipped()).isZero();
    assertThat(results.getDurationSeconds()).isEqualTo(0.002);
  }

  @Test
  void testCollectReportWithBadDurationAttributes() {
    ResultsAggregator results =
        getResults("/org/sonar/plugins/delphi/nunit/reports/badDurationAttributes");

    // missing or invalid duration should be treated as zero
    assertThat(results).isNotNull();
    assertThat(results.getTestsRun()).isEqualTo(2);
    assertThat(results.getFailures()).isZero();
    assertThat(results.getSkipped()).isZero();
    assertThat(results.getDurationSeconds()).isZero();
  }
}
