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
package au.com.integradev.delphi.executor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.core.DelphiLanguage;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;

class DelphiMetricsExecutorTest {
  private static final String ROOT_PATH = "/au/com/integradev/delphi/projects/SimpleProject/tools/";
  private static final String ACCESSORS_TEST = "AccessorsTest.pas";
  private static final String COMMENTS_TEST = "CommentsTest.pas";
  private static final String FUNCTION_TEST = "FunctionTest.pas";
  private static final String GLOBALS_TEST = "GlobalsTest.pas";

  private static final File ROOT_DIR = DelphiUtils.getResource(ROOT_PATH);

  private DelphiMetricsExecutor executor;
  private SensorContextTester sensorContext;
  private FileLinesContext fileLinesContext;
  private ExecutorContext context;
  private String componentKey;

  @BeforeEach
  void setup() {
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);

    executor = new DelphiMetricsExecutor(fileLinesContextFactory);
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
    checkCodeLines(
        Set.of(
            1, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 18, 20, 21, 22, 23, 25, 26, 27, 29, 30, 31,
            33, 34, 35, 37),
        37);
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
    checkCodeLines(Set.of(1, 3, 6, 7, 8, 11, 14, 15, 16, 17, 21, 23, 24, 38, 42), 42);
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
    checkCodeLines(
        Set.of(
            1, 3, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 22, 24, 26, 27, 28, 29, 30,
            31, 32, 33, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 48, 49, 50, 51, 52, 53, 55,
            56, 57, 58, 59, 60, 62, 63, 65, 67),
        67);
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
    checkCodeLines(Set.of(1, 3, 5, 6, 7, 8, 9, 10, 12, 14, 15, 20, 21, 22, 23, 24, 25, 27), 27);
  }

  private void execute(String resourcePath) {
    try {
      File resource = DelphiUtils.getResource(resourcePath);
      DelphiInputFile file =
          DelphiInputFile.from(
              TestInputFileBuilder.create("moduleKey", ROOT_DIR, resource)
                  .setContents(FileUtils.readFileToString(resource, UTF_8.name()))
                  .setLanguage(DelphiLanguage.KEY)
                  .setType(InputFile.Type.MAIN)
                  .build(),
              mockConfig());
      componentKey = file.getInputFile().key();
      executor.execute(context, file);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private <T extends Serializable> void checkMetric(Metric<T> metric, T value) {
    assertThat(sensorContext.measure(componentKey, metric).value())
        .as(metric.getDescription())
        .isEqualTo(value);
  }

  void checkCodeLines(Set<Integer> codeLines, int totalLines) {
    for (int i = 1; i <= totalLines; i++) {
      verify(fileLinesContext)
          .setIntValue(CoreMetrics.NCLOC_DATA_KEY, i, codeLines.contains(i) ? 1 : 0);
    }
  }

  private static DelphiFileConfig mockConfig() {
    TypeFactory typeFactory =
        new TypeFactory(
            DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT, DelphiProperties.COMPILER_VERSION_DEFAULT);
    DelphiFileConfig mock = mock(DelphiFileConfig.class);
    when(mock.getEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    when(mock.getTypeFactory()).thenReturn(typeFactory);
    when(mock.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(mock.getDefinitions()).thenReturn(Collections.emptySet());
    return mock;
  }
}
