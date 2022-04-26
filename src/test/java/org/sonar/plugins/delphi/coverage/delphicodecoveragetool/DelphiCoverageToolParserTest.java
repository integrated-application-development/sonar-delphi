/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.coverage.delphicodecoveragetool;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.msbuild.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiCoverageToolParserTest {

  private static final String BASE_REPORT_PATH =
      "/org/sonar/plugins/delphi/projects/SimpleProject/delphicodecoveragereports/";
  private static final String INVALID_LINE_HITS = BASE_REPORT_PATH + "InvalidLineHits.xml";
  private static final String NO_LINE_HITS = BASE_REPORT_PATH + "NoLineHits.xml";
  private static final String INVALID_STRUCTURE = BASE_REPORT_PATH + "InvalidStructure.xml";
  private static final String NORMAL_COVERAGE = BASE_REPORT_PATH + "NormalCoverage.xml";
  private static final String NORMAL_COVERAGE_PART_2 = BASE_REPORT_PATH + "NormalCoverage2.xml";

  private static final String GLOBALS_FILENAME = "Globals.pas";
  private static final String GLOBALS_FILE_KEY = ":" + GLOBALS_FILENAME;
  private static final String MAIN_WINDOW_FILENAME = "MainWindow.pas";
  private static final String MAIN_WINDOW_FILE_KEY = ":" + MAIN_WINDOW_FILENAME;

  private SensorContextTester context;
  private EnvironmentVariableProvider environmentVariableProvider;
  private File baseDir;
  private DelphiProjectHelper delphiProjectHelper;
  private DelphiCodeCoverageToolParser parser;

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/projects/SimpleProject";

  private void addFile(String fileName) throws IOException {
    File file = DelphiUtils.getResource(fileName);
    final InputFile inputFile =
        TestInputFileBuilder.create("", baseDir, file)
            .setLanguage(DelphiLanguage.KEY)
            .setContents(DelphiUtils.readFileContent(file, delphiProjectHelper.encoding()))
            .build();
    context.fileSystem().add(inputFile);
  }

  @BeforeEach
  void setup() throws IOException {
    baseDir = DelphiUtils.getResource(ROOT_NAME);
    context = SensorContextTester.create(baseDir);

    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);

    delphiProjectHelper =
        new DelphiProjectHelper(
            context.config(), context.fileSystem(), environmentVariableProvider);
    parser = new DelphiCodeCoverageToolParser(delphiProjectHelper);

    addFile(ROOT_NAME + "/" + GLOBALS_FILENAME);
    addFile(ROOT_NAME + "/" + MAIN_WINDOW_FILENAME);
  }

  @Test
  void testWhenValidReportLineHitsAreExtracted() {
    parser.parse(context, DelphiUtils.getResource(NORMAL_COVERAGE));

    assertThat(context.lineHits(GLOBALS_FILE_KEY, 16)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(GLOBALS_FILE_KEY, 17)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(GLOBALS_FILE_KEY, 23)).isEqualTo((Integer) 0);

    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 31)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 36)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 37)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 38)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 39)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 40)).isEqualTo((Integer) 1);
  }

  @Test
  void testLineHitsFromDifferentReportsAreMerged() {
    parser.parse(context, DelphiUtils.getResource(NORMAL_COVERAGE));
    parser.parse(context, DelphiUtils.getResource(NORMAL_COVERAGE_PART_2));

    assertThat(context.lineHits(GLOBALS_FILE_KEY, 16)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(GLOBALS_FILE_KEY, 17)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(GLOBALS_FILE_KEY, 23)).isEqualTo((Integer) 1);
  }

  void testReportFileIsIgnored(File file) {
    SensorContext mockContext = mock(SensorContext.class);
    assertThatCode(() -> parser.parse(mockContext, file)).doesNotThrowAnyException();
    verify(mockContext, times(0)).newCoverage();
  }

  @Test
  void testInvalidXmlReportIsIgnored() {
    testReportFileIsIgnored(DelphiUtils.getResource(INVALID_STRUCTURE));
  }

  @Test
  void testBadExistentFileIsIgnored() {
    File file = mock(File.class);
    when(file.exists()).thenReturn(true);
    testReportFileIsIgnored(file);
  }

  @Test
  void testNonExistentReportIsIgnored() {
    testReportFileIsIgnored(new File(UUID.randomUUID().toString()));
  }

  @Test
  void testReportWithNoLineHitsIsIgnored() {
    testReportFileIsIgnored(DelphiUtils.getResource(NO_LINE_HITS));
  }

  @Test
  void testWhenPartiallyValidReportOnlyValidLineHitsAreRecorded() {
    parser.parse(context, DelphiUtils.getResource(INVALID_LINE_HITS));

    assertThat(context.lineHits(GLOBALS_FILE_KEY, 16)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(GLOBALS_FILE_KEY, 27)).isNull();

    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 52)).isEqualTo((Integer) 2);
    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 53)).isNull();

    assertThat(context.lineHits(MAIN_WINDOW_FILE_KEY, 32)).isNull();
  }
}
