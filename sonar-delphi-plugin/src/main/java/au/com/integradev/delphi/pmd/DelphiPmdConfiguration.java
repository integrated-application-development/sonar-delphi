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
package au.com.integradev.delphi.pmd;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import au.com.integradev.delphi.pmd.xml.DelphiRule;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public class DelphiPmdConfiguration extends PMDConfiguration {
  private static final Logger LOG = Loggers.get(DelphiPmdConfiguration.class);
  private static final String PMD_RESULT_XML = "pmd-result.xml";
  private static final String PMD_CONFIG_SAVE_FAILURE = "Failed to save the PMD configuration";
  private static final String PMD_REPORT_SAVE_FAILURE = "Failed to save the PMD report";

  private final FileSystem fileSystem;
  private final Configuration settings;
  private final DelphiPmdRuleSetDefinitionProvider ruleSetDefinitionProvider;

  public DelphiPmdConfiguration(
      FileSystem fileSystem,
      Configuration settings,
      DelphiPmdRuleSetDefinitionProvider ruleSetDefinitionProvider) {
    this.fileSystem = fileSystem;
    this.settings = settings;
    this.ruleSetDefinitionProvider = ruleSetDefinitionProvider;
  }

  public DelphiRule getRuleDefinition(DelphiRule rule) {
    List<DelphiRule> ruleDefinitions = ruleSetDefinitionProvider.getDefinition().getRules();

    return findRuleByName(ruleDefinitions, rule.getTemplateName())
        .or(() -> findRuleByName(ruleDefinitions, rule.getName()))
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format("Rule definition not found for %s", rule.getName())));
  }

  private static Optional<DelphiRule> findRuleByName(
      List<DelphiRule> ruleDefinitions, String name) {
    return ruleDefinitions.stream().filter(def -> def.getName().equals(name)).findFirst();
  }

  private static String reportToString(Report report) throws IOException {
    StringWriter output = new StringWriter();

    Renderer xmlRenderer = new XMLRenderer();
    xmlRenderer.setWriter(output);
    xmlRenderer.start();
    xmlRenderer.renderFileReport(report);
    xmlRenderer.end();

    return output.toString();
  }

  public File dumpXmlRuleSet(String repositoryKey, String rulesXml) {
    try {
      File configurationFile = writeToWorkingDirectory(rulesXml, repositoryKey + ".xml").toFile();

      LOG.info("PMD configuration: {}", configurationFile.getAbsolutePath());

      return configurationFile;
    } catch (IOException e) {
      throw new IllegalStateException(PMD_CONFIG_SAVE_FAILURE, e);
    }
  }

  /**
   * Writes an XML Report about the analyzed project into the current working directory unless
   * <b>sonar.pmd.generateXml</b> is set to false.
   *
   * @param report The report which shall be written into an XML file.
   * @return Path to the report
   */
  public Path dumpXmlReport(Report report) {
    if (!settings.getBoolean(DelphiProperties.GENERATE_PMD_REPORT_XML_KEY).orElse(false)) {
      return null;
    }

    try {
      final String reportAsString = reportToString(report);
      final Path reportFile = writeToWorkingDirectory(reportAsString, PMD_RESULT_XML);

      LOG.info("PMD output report: " + reportFile.toString());

      return reportFile;
    } catch (IOException e) {
      throw new IllegalStateException(PMD_REPORT_SAVE_FAILURE, e);
    }
  }

  private Path writeToWorkingDirectory(String content, String fileName) throws IOException {
    final Path targetPath = fileSystem.workDir().toPath().resolve(fileName);
    Files.write(targetPath, content.getBytes(StandardCharsets.UTF_8));

    return targetPath;
  }
}
