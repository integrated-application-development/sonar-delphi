package org.sonar.plugins.delphi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonar.plugins.delphi.IntegrationTests.createScanner;
import static org.sonar.plugins.delphi.IntegrationTests.getProjectMeasureAsDouble;

import com.sonar.orchestrator.Orchestrator;
import org.junit.ClassRule;
import org.junit.Test;

public class DelphiCpdExecutorIT {
  private static final String SIMPLE_PROJECT_KEY = "delphi-cpd-simple-test";
  private static final String LITERALS_PROJECT_KEY = "delphi-cpd-literals-test";
  private static final String WHITESPACE_PROJECT_KEY = "delphi-cpd-whitespace-test";

  @ClassRule public static final Orchestrator ORCHESTRATOR = IntegrationTests.ORCHESTRATOR;

  @Test
  public void testShouldDetectDuplicates() {
    ORCHESTRATOR.executeBuild(createScanner("cpd-simple", SIMPLE_PROJECT_KEY));
    assertThat(getProjectMeasureAsDouble("duplicated_lines", SIMPLE_PROJECT_KEY), is(26.0));
    assertThat(getProjectMeasureAsDouble("duplicated_blocks", SIMPLE_PROJECT_KEY), is(2.0));
  }

  @Test
  public void testShouldDetectDuplicatesByNormalizingLiterals() {
    ORCHESTRATOR.executeBuild(createScanner("cpd-literals", LITERALS_PROJECT_KEY));
    assertThat(getProjectMeasureAsDouble("duplicated_lines", LITERALS_PROJECT_KEY), is(26.0));
    assertThat(getProjectMeasureAsDouble("duplicated_blocks", LITERALS_PROJECT_KEY), is(2.0));
  }

  @Test
  public void testShouldDetectDuplicatesByIgnoringWhitespace() {
    ORCHESTRATOR.executeBuild(createScanner("cpd-whitespace", WHITESPACE_PROJECT_KEY));
    assertThat(getProjectMeasureAsDouble("duplicated_lines", WHITESPACE_PROJECT_KEY), is(33.0));
    assertThat(getProjectMeasureAsDouble("duplicated_blocks", WHITESPACE_PROJECT_KEY), is(2.0));
  }
}
