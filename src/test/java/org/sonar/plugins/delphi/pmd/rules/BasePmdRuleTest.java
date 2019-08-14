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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.hamcrest.Matcher;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.DelphiPmdConfiguration;
import org.sonar.plugins.delphi.pmd.DelphiPmdExecutor;
import org.sonar.plugins.delphi.pmd.DelphiPmdSensor;
import org.sonar.plugins.delphi.pmd.DelphiPmdViolationRecorder;
import org.sonar.plugins.delphi.pmd.DelphiTestFileBuilder;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

abstract class BasePmdRuleTest {

  private static final String ROOT_DIR_NAME = "/org/sonar/plugins/delphi/pmd";
  private static final File ROOT_DIR = DelphiUtils.getResource(ROOT_DIR_NAME);

  protected DelphiPmdSensor sensor;
  private Collection<Issue> issues = new ArrayList<>();
  private File baseDir;

  public void execute(DelphiTestFileBuilder builder) {
    configureTest(builder);

    SensorContextTester sensorContext = SensorContextTester.create(baseDir);
    sensor.execute(sensorContext);
    issues = sensorContext.allIssues();

    assertThat("Errors: " + sensor.getErrors(), sensor.getErrors(), empty());
  }

  private void configureTest(DelphiTestFileBuilder builder) {
    File testFile = builder.buildFile(ROOT_DIR);
    testFile.deleteOnExit();

    String relativePathTestFile =
        DelphiUtils.getRelativePath(testFile, Collections.singletonList(ROOT_DIR));

    configureTest(ROOT_DIR_NAME + "/" + relativePathTestFile, builder);
  }

  private void configureTest(String testFileName, DelphiTestFileBuilder builder) {
    SensorContextTester sensorContext = SensorContextTester.create(ROOT_DIR);

    DelphiProjectHelper delphiProjectHelper = DelphiTestUtils.mockProjectHelper();
    when(delphiProjectHelper.workDir()).thenReturn(new File("target"));
    when(delphiProjectHelper.testTypeRegex()).thenReturn("TTestSuite_.*");

    baseDir = DelphiUtils.getResource(ROOT_DIR_NAME);
    File srcFile = DelphiUtils.getResource(testFileName);
    StringBuilder builderSourceCode = builder.getSourceCode(true);

    InputFile inputFile =
        TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, srcFile)
            .setModuleBaseDir(baseDir.toPath())
            .setContents(builderSourceCode.toString())
            .setLanguage(DelphiLanguage.KEY)
            .build();

    sensorContext.fileSystem().add(inputFile);

    DelphiProject delphiProject = new DelphiProject("Default Project");
    delphiProject.setSourceFiles(Collections.singletonList(srcFile));

    when(delphiProjectHelper.getProjects()).thenReturn(Collections.singletonList(delphiProject));
    when(delphiProjectHelper.getFile(anyString())).thenReturn(inputFile);

    String fileName = getClass().getResource("/org/sonar/plugins/delphi/pmd/rules.xml").getPath();
    File rulesFile = new File(fileName);

    ActiveRules rulesProfile = mockActiveRules(rulesFile);

    var pmdConfig = mock(DelphiPmdConfiguration.class);
    when(pmdConfig.dumpXmlRuleSet(any(String.class), any(String.class))).thenReturn(rulesFile);

    var executor = new DelphiPmdExecutor(delphiProjectHelper, rulesProfile, pmdConfig);
    var violationRecorder = new DelphiPmdViolationRecorder(delphiProjectHelper, rulesProfile);

    sensor = new DelphiPmdSensor(executor, violationRecorder);
  }

  private ActiveRules mockActiveRules(File rulesFile) {
    final DelphiRuleSet ruleSet;

    try {
      ruleSet = DelphiRuleSetHelper.createFrom(new FileReader(rulesFile, StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    ActiveRules rulesProfile = mock(ActiveRules.class);
    when(rulesProfile.find(any(RuleKey.class)))
        .thenAnswer(
            (Answer<ActiveRule>)
                invocation -> {
                  RuleKey ruleKey = (RuleKey) invocation.getArguments()[0];
                  return makeActiveRule(ruleKey, ruleSet);
                });
    return rulesProfile;
  }

  private ActiveRule makeActiveRule(RuleKey ruleKey, DelphiRuleSet ruleSet) {
    NewActiveRule.Builder builder = new NewActiveRule.Builder();

    for (DelphiRule rule : ruleSet.getRules()) {
      if (!rule.getName().equals(ruleKey.rule())) {
        continue;
      }

      for (DelphiRuleProperty property : rule.getProperties()) {
        builder.setParam(property.getName(), property.getValue());
      }
    }

    NewActiveRule newActiveRule = builder.setRuleKey(ruleKey).build();
    ActiveRules rules = new ActiveRulesBuilder().addRule(newActiveRule).build();

    return rules.find(ruleKey);
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
    TextRange primaryLocation = issue.primaryLocation().textRange();
    assertThat(primaryLocation, is(not(nullValue())));

    int line = primaryLocation.start().line();
    String message = issue.primaryLocation().message();

    return String.format("Issue [ruleKey=%s, message=%s, line=%s]", issue.ruleKey(), message, line);
  }

  @SuppressWarnings("unchecked")
  protected void assertIssues(Matcher matcher) {
    assertThat(stringifyIssues(), issues, matcher);
  }
}
