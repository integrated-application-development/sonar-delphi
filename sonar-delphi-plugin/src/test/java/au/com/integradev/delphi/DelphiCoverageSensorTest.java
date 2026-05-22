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
package au.com.integradev.delphi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.coverage.DelphiCodeCoverageParser;
import au.com.integradev.delphi.coverage.DelphiCoverageParser;
import au.com.integradev.delphi.coverage.DelphiCoverageParserFactory;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

class DelphiCoverageSensorTest {
  private static final String BASE_PATH = "/au/com/integradev/delphi/";
  private static final File BASE_DIR = DelphiUtils.getResource(BASE_PATH);
  private static final String COVERAGE_REPORT_PATH = BASE_PATH + "coverage";

  private final DefaultFileSystem fileSystem = new DefaultFileSystem(BASE_DIR);
  private final SensorContextTester context = SensorContextTester.create(fileSystem.baseDir());
  private final DelphiCoverageParserFactory coverageParserFactory =
      mock(DelphiCoverageParserFactory.class);
  private final DelphiCoverageParser coverageParser = mock(DelphiCodeCoverageParser.class);

  private DelphiCoverageSensor sensor;

  @BeforeEach
  void setupSensor() {
    sensor = new DelphiCoverageSensor(coverageParserFactory);
    when(coverageParserFactory.create()).thenReturn(coverageParser);
  }

  @Test
  void testToString() {
    final String toString = sensor.toString();
    assertThat(toString).isEqualTo("DelphiCoverageSensor");
  }

  @Test
  void testDescribe() {
    final DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();

    sensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("DelphiCoverageSensor");
    assertThat(descriptor.languages()).containsExactly(Delphi.KEY);
    assertThat(descriptor.type()).isEqualTo(InputFile.Type.MAIN);
  }

  @Test
  void testWhenBadCoverageReportDoNotInvokeParser() {
    context
        .settings()
        .setProperty(DelphiProperties.COVERAGE_REPORT_KEY, UUID.randomUUID().toString());

    sensor.execute(context);

    verify(coverageParser, never()).parse(any(), any());

    context.settings().setProperty(DelphiProperties.COVERAGE_REPORT_KEY, "</invalidPath");

    sensor.execute(context);

    verify(coverageParser, never()).parse(any(), any());
  }

  @Test
  void testWhenCoverageReportsExistParserIsInvoked() {
    context
        .settings()
        .setProperty(
            DelphiProperties.COVERAGE_REPORT_KEY,
            DelphiUtils.getResource(COVERAGE_REPORT_PATH).toString());

    sensor.execute(context);

    verify(coverageParser, times(1))
        .parse(any(), eq(DelphiUtils.getResource(COVERAGE_REPORT_PATH + "/Report.xml")));
  }
}
