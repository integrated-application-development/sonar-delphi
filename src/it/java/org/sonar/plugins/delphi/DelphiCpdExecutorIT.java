package org.sonar.plugins.delphi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.delphi.IntegrationTestSuite.createScanner;
import static org.sonar.plugins.delphi.IntegrationTestSuite.getProjectMeasureAsDouble;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.plugins.delphi.extension.OrchestratorExtension;

class DelphiCpdExecutorIT {
  private static final String SIMPLE_PROJECT_KEY = "delphi-cpd-simple-test";
  private static final String LITERALS_PROJECT_KEY = "delphi-cpd-literals-test";
  private static final String WHITESPACE_PROJECT_KEY = "delphi-cpd-whitespace-test";

  @RegisterExtension
  static final OrchestratorExtension ORCHESTRATOR = IntegrationTestSuite.ORCHESTRATOR;

  @Test
  void testShouldDetectDuplicates() {
    ORCHESTRATOR.getOrchestrator().executeBuild(createScanner("cpd-simple", SIMPLE_PROJECT_KEY));
    assertThat(getProjectMeasureAsDouble("duplicated_lines", SIMPLE_PROJECT_KEY)).isEqualTo(26.0);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks", SIMPLE_PROJECT_KEY)).isEqualTo(2.0);
  }

  @Test
  void testShouldDetectDuplicatesByNormalizingLiterals() {
    ORCHESTRATOR
        .getOrchestrator()
        .executeBuild(createScanner("cpd-literals", LITERALS_PROJECT_KEY));
    assertThat(getProjectMeasureAsDouble("duplicated_lines", LITERALS_PROJECT_KEY)).isEqualTo(26.0);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks", LITERALS_PROJECT_KEY)).isEqualTo(2.0);
  }

  @Test
  void testShouldDetectDuplicatesByIgnoringWhitespace() {
    ORCHESTRATOR
        .getOrchestrator()
        .executeBuild(createScanner("cpd-whitespace", WHITESPACE_PROJECT_KEY));
    assertThat(getProjectMeasureAsDouble("duplicated_lines", WHITESPACE_PROJECT_KEY))
        .isEqualTo(33.0);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks", WHITESPACE_PROJECT_KEY))
        .isEqualTo(2.0);
  }
}
