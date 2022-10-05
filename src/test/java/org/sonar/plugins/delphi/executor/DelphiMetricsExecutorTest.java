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
package org.sonar.plugins.delphi.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.Serializable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiInputFile;
import org.sonar.plugins.delphi.symbol.SymbolTable;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

class DelphiMetricsExecutorTest {
  private static final String ROOT_PATH = "/org/sonar/plugins/delphi/projects/SimpleProject/tools/";
  private static final String ACCESSORS_TEST = "AccessorsTest.pas";
  private static final String COMMENTS_TEST = "CommentsTest.pas";
  private static final String FUNCTION_TEST = "FunctionTest.pas";
  private static final String GLOBALS_TEST = "GlobalsTest.pas";

  private static final File ROOT_DIR = DelphiUtils.getResource(ROOT_PATH);

  private DelphiMetricsExecutor executor;
  private SensorContextTester sensorContext;
  private ExecutorContext context;
  private String componentKey;

  @BeforeEach
  void setup() {
    executor = new DelphiMetricsExecutor();
    sensorContext = SensorContextTester.create(ROOT_DIR);
    context = new ExecutorContext(sensorContext, mock(SymbolTable.class));
  }

  @Test
  void testAccessorsFile() {
    execute(ACCESSORS_TEST);
    checkMetric(CoreMetrics.CLASSES, 1);
    checkMetric(CoreMetrics.FUNCTIONS, 4);
    checkMetric(CoreMetrics.COMPLEXITY, 4);
    checkMetric(CoreMetrics.COMMENT_LINES, 0);
    checkMetric(CoreMetrics.STATEMENTS, 1);
    checkMetric(CoreMetrics.NCLOC, 28);
    checkMetric(CoreMetrics.COGNITIVE_COMPLEXITY, 0);
  }

  @Test
  void testCommentsFile() {
    execute(COMMENTS_TEST);
    checkMetric(CoreMetrics.CLASSES, 1);
    checkMetric(CoreMetrics.FUNCTIONS, 0);
    checkMetric(CoreMetrics.COMPLEXITY, 0);
    checkMetric(CoreMetrics.COMMENT_LINES, 13);
    checkMetric(CoreMetrics.STATEMENTS, 0);
    checkMetric(CoreMetrics.NCLOC, 15);
    checkMetric(CoreMetrics.COGNITIVE_COMPLEXITY, 0);
  }

  @Test
  void testFunctionFile() {
    execute(FUNCTION_TEST);
    checkMetric(CoreMetrics.CLASSES, 1);
    checkMetric(CoreMetrics.FUNCTIONS, 5);
    checkMetric(CoreMetrics.COMPLEXITY, 8);
    checkMetric(CoreMetrics.COMMENT_LINES, 1);
    checkMetric(CoreMetrics.STATEMENTS, 12);
    checkMetric(CoreMetrics.NCLOC, 54);
    checkMetric(CoreMetrics.COGNITIVE_COMPLEXITY, 3);
  }

  @Test
  void testGlobalsFile() {
    execute(GLOBALS_TEST);
    checkMetric(CoreMetrics.CLASSES, 1);
    checkMetric(CoreMetrics.FUNCTIONS, 1);
    checkMetric(CoreMetrics.COMPLEXITY, 1);
    checkMetric(CoreMetrics.COMMENT_LINES, 1);
    checkMetric(CoreMetrics.STATEMENTS, 3);
    checkMetric(CoreMetrics.NCLOC, 18);
    checkMetric(CoreMetrics.COGNITIVE_COMPLEXITY, 0);
  }

  private void execute(String filename) {
    DelphiInputFile file = DelphiTestFileBuilder.fromResource(ROOT_PATH + filename).delphiFile();
    componentKey = file.getInputFile().key();
    executor.execute(context, file);
  }

  private <T extends Serializable> void checkMetric(Metric<T> metric, T value) {
    assertThat(sensorContext.measure(componentKey, metric).value())
        .as(metric.getDescription())
        .isEqualTo(value);
  }
}
