/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
