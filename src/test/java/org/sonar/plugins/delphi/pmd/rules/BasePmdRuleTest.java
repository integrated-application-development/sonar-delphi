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
package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.DelphiSensor;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.executor.DelphiCpdExecutor;
import org.sonar.plugins.delphi.executor.DelphiHighlightExecutor;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.executor.DelphiMetricsExecutor;
import org.sonar.plugins.delphi.executor.DelphiPmdExecutor;
import org.sonar.plugins.delphi.executor.DelphiSymbolTableExecutor;
import org.sonar.plugins.delphi.pmd.DelphiPmdConfiguration;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.FilePosition;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import org.sonar.plugins.delphi.pmd.violation.DelphiPmdViolationRecorder;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.PmdLevelUtils;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

public abstract class BasePmdRuleTest {
  private static final String ROOT_DIR_NAME = "/org/sonar/plugins/delphi/pmd";
  private static final File ROOT_DIR = DelphiUtils.getResource(ROOT_DIR_NAME);

  protected DelphiSensor sensor;
  private SensorContextTester sensorContext;
  private Collection<Issue> issues = new ArrayList<>();
  private DelphiRuleSet ruleSet;
  private DelphiPmdRuleSetDefinitionProvider ruleProvider =
      new DelphiPmdRuleSetDefinitionProvider();

  @Before
  public void setupRuleSet() {
    ruleSet = ruleProvider.getDefinition();
  }

  public void execute(DelphiTestFileBuilder builder) {
    configureTest(builder);

    sensor.execute(sensorContext);
    issues = sensorContext.allIssues();

    assertThat("Errors: " + sensor.getErrors(), sensor.getErrors(), empty());
  }

  private void configureTest(DelphiTestFileBuilder builder) {
    sensorContext = SensorContextTester.create(ROOT_DIR);
    builder.setBaseDir(ROOT_DIR);

    builder.printSourceCode();
    builder.setBaseDir(ROOT_DIR);

    InputFile inputFile = builder.inputFile();
    File srcFile = new File(inputFile.uri());
    DefaultFileSystem fs = sensorContext.fileSystem();
    fs.setWorkDir(ROOT_DIR.toPath());
    fs.add(inputFile);

    DelphiProjectHelper delphiProjectHelper = mock(DelphiProjectHelper.class);
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(true);
    when(delphiProjectHelper.workDir()).thenReturn(new File("target"));
    when(delphiProjectHelper.testTypeRegex()).thenReturn("(?i)TTestSuite_.*");
    when(delphiProjectHelper.getFile(any(File.class))).thenReturn(inputFile);

    DelphiProject delphiProject = new DelphiProject("Default Project");
    delphiProject.setSourceFiles(Collections.singletonList(srcFile));

    when(delphiProjectHelper.getProjects()).thenReturn(Collections.singletonList(delphiProject));
    when(delphiProjectHelper.getFile(anyString())).thenReturn(inputFile);

    ActiveRules rulesProfile = makeActiveRules();
    Configuration config = sensorContext.config();
    DelphiPmdConfiguration pmdConfig = new DelphiPmdConfiguration(fs, config, ruleProvider);

    var violationRecorder = new DelphiPmdViolationRecorder(delphiProjectHelper, rulesProfile);
    var executor = new DelphiPmdExecutor(sensorContext, rulesProfile, pmdConfig, violationRecorder);

    DelphiMasterExecutor masterExecutor =
        new DelphiMasterExecutor(
            new DelphiHighlightExecutor(),
            new DelphiCpdExecutor(),
            new DelphiMetricsExecutor(),
            new DelphiSymbolTableExecutor(),
            executor);

    sensor = new DelphiSensor(delphiProjectHelper, masterExecutor);
  }

  private ActiveRules makeActiveRules() {
    ActiveRulesBuilder activeRulesBuilder = new ActiveRulesBuilder();

    for (DelphiRule rule : ruleProvider.getDefinition().getRules()) {
      NewActiveRule.Builder ruleBuilder =
          new NewActiveRule.Builder()
              .setRuleKey(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, rule.getName()))
              .setTemplateRuleKey(rule.getTemplateName())
              .setName(rule.getName())
              .setInternalKey(rule.getClazz())
              .setSeverity(PmdLevelUtils.severityFromLevel(rule.getPriority()))
              .setLanguage(DelphiLanguage.KEY);

      for (DelphiRuleProperty property : rule.getProperties()) {
        if (property.isBuiltinProperty()) {
          continue;
        }

        ruleBuilder.setParam(property.getName(), property.getValue());
      }

      activeRulesBuilder.addRule(ruleBuilder.build());
    }

    return activeRulesBuilder.build();
  }

  protected void addRule(DelphiRule rule) {
    ruleSet.addRule(rule);
  }

  private String stringifyIssues() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    for (Issue issue : issues) {
      builder.append(stringifyIssue(issue)).append(", ");
    }
    builder.append("]");
    return builder.toString();
  }

  private String stringifyIssue(Issue issue) {
    IssueLocation primaryLocation = issue.primaryLocation();
    TextRange textRange = primaryLocation.textRange();
    int line = textRange == null ? FilePosition.UNDEFINED_LINE : textRange.start().line();
    String message = issue.primaryLocation().message();

    return String.format("Issue [ruleKey=%s, message=%s, line=%d]", issue.ruleKey(), message, line);
  }

  @SuppressWarnings("unchecked")
  protected void assertIssues(Matcher matcher) {
    assertThat(stringifyIssues(), issues, matcher);
  }
}
