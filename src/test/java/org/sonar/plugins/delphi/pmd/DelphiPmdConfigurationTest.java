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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.sourceforge.pmd.Report;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;

public class DelphiPmdConfigurationTest {

  private static final File WORK_DIR = new File("test-work-dir");

  private final FileSystem fs = mock(FileSystem.class);
  private DelphiPmdConfiguration configuration;
  private MapSettings settings;

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @BeforeClass
  public static void createTempDir() {
    deleteTempDir();
    WORK_DIR.mkdir();
  }

  @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
  @AfterClass
  public static void deleteTempDir() {
    if (WORK_DIR.exists()) {
      for (File file : WORK_DIR.listFiles()) {
        file.delete();
      }
      WORK_DIR.delete();
    }
  }

  @Before
  public void setUpPmdConfiguration() {
    settings = new MapSettings();
    DelphiPmdRuleSetDefinitionProvider provider = new DelphiPmdRuleSetDefinitionProvider();
    configuration = new DelphiPmdConfiguration(fs, settings.asConfig(), provider);
  }

  @Test
  public void testShouldDumpXmlRuleSet() throws IOException {
    when(fs.workDir()).thenReturn(WORK_DIR);

    File rulesFile = configuration.dumpXmlRuleSet("pmd", "<rules>");

    assertThat(rulesFile).isEqualTo(new File(WORK_DIR, "pmd.xml"));
    assertThat(Files.readAllLines(rulesFile.toPath(), UTF_8)).containsExactly("<rules>");
  }

  @Test
  public void testShouldFailToDumpXmlRuleSet() {
    when(fs.workDir()).thenReturn(new File("xxx"));

    final Throwable thrown = catchThrowable(() -> configuration.dumpXmlRuleSet("pmd", "<xml>"));

    assertThat(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to save the PMD configuration");
  }

  @Test
  public void testShouldDumpXmlReport() throws IOException {
    when(fs.workDir()).thenReturn(WORK_DIR);

    settings.setProperty(DelphiPlugin.GENERATE_PMD_REPORT_XML_KEY, true);
    Path reportFile = configuration.dumpXmlReport(new Report());

    assertThat(reportFile.toFile()).isEqualTo(new File(WORK_DIR, "pmd-result.xml"));
    List<String> writtenLines = Files.readAllLines(reportFile, UTF_8);
    assertThat(writtenLines).hasSize(6);
    assertThat(writtenLines.get(1)).contains("<pmd");
  }

  @Test
  public void testShouldFailToDumpXmlReport() {
    when(fs.workDir()).thenReturn(new File("xxx"));

    settings.setProperty(DelphiPlugin.GENERATE_PMD_REPORT_XML_KEY, true);

    final Throwable thrown = catchThrowable(() -> configuration.dumpXmlReport(new Report()));

    assertThat(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to save the PMD report");
  }

  @Test
  public void testShouldIgnoreXmlReportWhenPropertyIsNotSet() {
    Path reportFile = configuration.dumpXmlReport(new Report());

    assertThat(reportFile).isNull();
    verifyZeroInteractions(fs);
  }
}
