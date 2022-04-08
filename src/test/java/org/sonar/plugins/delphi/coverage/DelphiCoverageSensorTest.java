package org.sonar.plugins.delphi.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.coverage.delphicodecoveragetool.DelphiCodeCoverageToolParser;
import org.sonar.plugins.delphi.project.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiCoverageSensorTest {
  private static final String BASE_PATH = "/org/sonar/plugins/delphi/projects/";
  private static final File BASE_DIR = DelphiUtils.getResource(BASE_PATH);
  private static final String COVERAGE_REPORTS =
      BASE_PATH + "SimpleProject/delphicodecoveragereports";

  private final DefaultFileSystem fileSystem = new DefaultFileSystem(BASE_DIR);
  private final SensorContextTester context = SensorContextTester.create(fileSystem.baseDir());
  private final DelphiProjectHelper delphiProjectHelper = mock(DelphiProjectHelper.class);
  private final DelphiCoverageParserFactory coverageParserFactory =
      mock(DelphiCoverageParserFactory.class);
  private final DelphiCoverageParser coverageParser = mock(DelphiCodeCoverageToolParser.class);

  private DelphiCoverageSensor sensor;

  @BeforeEach
  void setupSensor() {
    sensor = new DelphiCoverageSensor(delphiProjectHelper, coverageParserFactory);
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(true);
    when(coverageParserFactory.getParser(anyString(), any()))
        .thenReturn(Optional.of(coverageParser));

    context
        .settings()
        .setProperty(DelphiPlugin.COVERAGE_TOOL_KEY, DelphiCodeCoverageToolParser.KEY);
  }

  @Test
  void testToString() {
    final String toString = sensor.toString();
    assertThat(toString).isEqualTo("DelphiCoverageSensor");
  }

  @Test
  void testDescribe() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(DelphiLanguage.KEY);
    verify(mockDescriptor).name("DelphiCoverageSensor");
  }

  @Test
  void testWhenBadCoverageReportDoNotInvokeParser() {
    context.settings().setProperty(DelphiPlugin.COVERAGE_REPORT_KEY, UUID.randomUUID().toString());

    sensor.execute(context);

    verify(coverageParser, never()).parse(any(), any());

    context.settings().setProperty(DelphiPlugin.COVERAGE_REPORT_KEY, "</invalidPath");

    sensor.execute(context);

    verify(coverageParser, never()).parse(any(), any());
  }

  @Test
  void testWhenNoCoverageToolDoNotInvokeParser() {
    context.settings().removeProperty(DelphiPlugin.COVERAGE_TOOL_KEY);
    sensor.execute(context);

    verify(coverageParser, never()).parse(any(), any());
  }

  @Test
  void testWhenCoverageReportsExistParserIsInvoked() {
    context
        .settings()
        .setProperty(
            DelphiPlugin.COVERAGE_REPORT_KEY, DelphiUtils.getResource(COVERAGE_REPORTS).toString());

    sensor.execute(context);

    verify(coverageParser, times(5)).parse(any(), any());
    verify(coverageParser, times(1))
        .parse(any(), eq(DelphiUtils.getResource(COVERAGE_REPORTS + "/InvalidLineHits.xml")));
    verify(coverageParser, times(1))
        .parse(any(), eq(DelphiUtils.getResource(COVERAGE_REPORTS + "/InvalidStructure.xml")));
    verify(coverageParser, times(1))
        .parse(any(), eq(DelphiUtils.getResource(COVERAGE_REPORTS + "/NoLineHits.xml")));
    verify(coverageParser, times(1))
        .parse(any(), eq(DelphiUtils.getResource(COVERAGE_REPORTS + "/NormalCoverage.xml")));
    verify(coverageParser, times(1))
        .parse(any(), eq(DelphiUtils.getResource(COVERAGE_REPORTS + "/NormalCoverage2.xml")));
  }
}
