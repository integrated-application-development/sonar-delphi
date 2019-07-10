/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.delphi.pmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterators;
import java.io.File;
import java.util.Iterator;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.plugins.delphi.core.DelphiLanguage;

public class DelphiPmdSensorTest {
  private final DelphiPmdExecutor executor = mock(DelphiPmdExecutor.class);
  private final DelphiPmdViolationRecorder violationRecorder = mock(DelphiPmdViolationRecorder.class);
  private final SensorContext context = mock(SensorContext.class);
  private final DefaultFileSystem fs = new DefaultFileSystem(new File("."));

  private DelphiPmdSensor sensor;

  private static DelphiRuleViolation violation() {
    return mock(DelphiRuleViolation.class);
  }

  private static Report report(DelphiRuleViolation... violations) {
    Report report = mock(Report.class);
    when(report.iterator()).thenReturn(Iterators.forArray(violations));
    return report;
  }

  @Before
  public void setupSensor() {
    sensor = new DelphiPmdSensor(executor, violationRecorder);
    when(executor.execute()).thenReturn(mock(Report.class));
    when(executor.shouldExecuteOnProject()).thenReturn(true);
  }

  @Test
  public void testShouldReportViolations() {
    addFile();
    final DelphiRuleViolation pmdViolation = violation();
    final Report report = report(pmdViolation);
    when(executor.execute()).thenReturn(report);

    sensor.execute(context);

    verify(violationRecorder).saveViolation(pmdViolation, context);
  }

  @Test
  public void testShouldNotReportZeroViolations() {
    final Report report = report();
    when(executor.execute()).thenReturn(report);

    sensor.execute(context);

    verify(violationRecorder, never()).saveViolation(any(DelphiRuleViolation.class), eq(context));
    verifyZeroInteractions(context);
  }

  @Test
  public void testSensorShouldNotRethrowOtherExceptions() {
    addFile();
    final RuntimeException expectedException = new RuntimeException();
    when(executor.execute()).thenThrow(expectedException);

    final Throwable thrown = catchThrowable(() -> sensor.execute(context));

    assertThat(thrown)
        .isInstanceOf(RuntimeException.class)
        .isEqualTo(expectedException);
  }

  @Test
  public void testToString() {
    final String toString = sensor.toString();
    assertThat(toString).isEqualTo("DelphiPmdSensor");
  }

  @Test
  public void testWhenDescribeCalledThenSensorDescriptionIsWritten() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(DelphiLanguage.KEY);
    verify(mockDescriptor).name("DelphiPmdSensor");
  }

  @SuppressWarnings("unchecked")
  private void mockEmptyReport() {
    final Report mockReport = mock(Report.class);
    final Iterator<RuleViolation> iterator = mock(Iterator.class);

    when(mockReport.iterator()).thenReturn(iterator);
    when(iterator.hasNext()).thenReturn(false);

    when(executor.execute()).thenReturn(mockReport);
  }

  private void addFile() {
    mockEmptyReport();
    File file = new File("x");
    fs.add(
      TestInputFileBuilder.create("sonar-pmd-test", file.getName())
        .setLanguage(DelphiLanguage.KEY)
        .setType(Type.MAIN)
        .build()
    );
  }

}
