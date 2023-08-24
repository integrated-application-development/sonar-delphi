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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.sonar.api.measures.CoreMetrics.*;

import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;

class DelphiNUnitSensorTest {
  private static final String PROJECT_DIR = "/au/com/integradev/delphi/nunit";

  private MapSettings settings;
  private SensorContextTester context;
  private DelphiNUnitSensor sensor;

  @BeforeEach
  void setup() {
    context = SensorContextTester.create(DelphiUtils.getResource(PROJECT_DIR));
    settings = new MapSettings();
    sensor = new DelphiNUnitSensor(settings.asConfig());
  }

  void assertAllMeasuresEmpty() {
    assertThat(context.measures(context.project().key())).isEmpty();
  }

  @Test
  void testToString() {
    assertThat(sensor).hasToString("DelphiNUnitSensor");
  }

  @Test
  void testDescribe() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(Delphi.KEY);
    verify(mockDescriptor).name("Delphi NUnit Sensor");
  }

  @Test
  void testExecute() {
    assertAllMeasuresEmpty();
    settings.setProperty(DelphiProperties.NUNIT_REPORT_PATHS_PROPERTY, "./");
    sensor.execute(context);

    assertThat(context.measure(context.project().key(), TESTS).value()).isEqualTo(8);
    assertThat(context.measure(context.project().key(), SKIPPED_TESTS).value()).isEqualTo(1);
    assertThat(context.measure(context.project().key(), TEST_FAILURES).value()).isEqualTo(3);
    assertThat(context.measure(context.project().key(), TEST_EXECUTION_TIME).value())
        .isEqualTo(1561L);
  }

  @Test
  void testExecuteWithNoReportPath() {
    sensor.execute(context);
    assertAllMeasuresEmpty();
  }

  @Test
  void testExecuteWithInvalidReportPath() {
    settings.setProperty(
        DelphiProperties.NUNIT_REPORT_PATHS_PROPERTY, UUID.randomUUID().toString());
    sensor.execute(context);
    assertAllMeasuresEmpty();
  }
}
