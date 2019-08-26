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
package org.sonar.plugins.delphi.pmd.violation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.SCOPE;

import java.io.File;
import java.util.Optional;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import org.junit.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;

public class DelphiPmdViolationRecorderTest {
  private final File baseDir = new File("").getAbsoluteFile();
  private final DefaultFileSystem spiedFs = spy(new DefaultFileSystem(baseDir));
  private final Configuration configuration = mock(Configuration.class);
  private final DelphiProjectHelper projectHelper = new DelphiProjectHelper(configuration, spiedFs);
  private final ActiveRules mockActiveRules = mock(ActiveRules.class);
  private final SensorContext mockContext = mock(SensorContext.class);

  private final DelphiPmdViolationRecorder violationRecorder =
      new DelphiPmdViolationRecorder(projectHelper, mockActiveRules);

  @Test
  public void testShouldConvertPmdViolationToSonarViolation() {
    final ActiveRule rule = createRuleInActiveRules();
    final File file = new File(baseDir, "FileWithViolation.java");
    final DefaultInputFile inputFile1 = addToFileSystem(file);
    final RuleViolation pmdViolation = mockPmdViolation(file, "RULE");
    final NewIssue newIssue = mock(NewIssue.class);
    final NewIssueLocation issueLocation = mock(NewIssueLocation.class);

    when(mockContext.newIssue()).thenReturn(newIssue);
    when(newIssue.forRule(rule.ruleKey())).thenReturn(newIssue);
    when(newIssue.newLocation()).thenReturn(issueLocation);
    when(newIssue.at(issueLocation)).thenReturn(newIssue);
    when(issueLocation.on(inputFile1)).thenReturn(issueLocation);
    when(issueLocation.message("Description")).thenReturn(issueLocation);
    when(issueLocation.at(any(TextRange.class))).thenReturn(issueLocation);

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verify(mockContext).newIssue();
    verify(newIssue).save();
  }

  @Test
  public void testShouldIgnoreViolationInTestCodeWhenScopeIsMain() {
    createRuleInActiveRules();
    final File file = new File(baseDir, "FileWithViolation.java");
    addToFileSystem(file);
    final RuleViolation pmdViolation = mockPmdViolation(file, "RULE");

    when(pmdViolation.getClassName()).thenReturn("Test_Method");
    when(pmdViolation.getRule().getProperty(SCOPE)).thenReturn(RuleScope.MAIN.name());
    when(configuration.get(DelphiPlugin.TEST_TYPE_REGEX_KEY)).thenReturn(Optional.of("Test_.*"));

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verifyZeroInteractions(mockActiveRules);
    verifyZeroInteractions(mockContext);
  }

  @Test
  public void testShouldIgnoreViolationInMainCodeWhenScopeIsTest() {
    createRuleInActiveRules();
    final File file = new File(baseDir, "FileWithViolation.java");
    addToFileSystem(file);
    final RuleViolation pmdViolation = mockPmdViolation(file, "RULE");

    when(pmdViolation.getClassName()).thenReturn("MainMethod");
    when(pmdViolation.getRule().getProperty(SCOPE)).thenReturn(RuleScope.TEST.name());
    when(configuration.get(DelphiPlugin.TEST_TYPE_REGEX_KEY)).thenReturn(Optional.of("Test_.*"));

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verifyZeroInteractions(mockActiveRules);
    verifyZeroInteractions(mockContext);
  }

  @Test
  public void testShouldIgnoreViolationOnUnknownResource() {
    final File unknownFile = new File(baseDir, "UNKNOWN.pas");
    final RuleViolation pmdViolation = mockPmdViolation(unknownFile, "RULE");

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verifyZeroInteractions(mockActiveRules);
    verifyZeroInteractions(mockContext);
    verify(spiedFs).inputFile(any(FilePredicate.class));
  }

  @Test
  public void testShouldIgnoreViolationOnUnknownRule() {
    final File file = new File("src/FileWithUnknownViolations.Pas");
    addToFileSystem(file);
    final String ruleName = "UNKNOWN";
    final RuleViolation pmdViolation = mockPmdViolation(file, ruleName);
    final RuleKey expectedRuleKey1 = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, ruleName);
    final RuleKey expectedRuleKey2 = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, ruleName);

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verify(spiedFs).inputFile(any(FilePredicate.class));
    verify(mockActiveRules).find(expectedRuleKey1);
    verify(mockActiveRules).find(expectedRuleKey2);
    verifyZeroInteractions(mockContext);
  }

  private DefaultInputFile addToFileSystem(File file) {
    DefaultInputFile inputFile =
        TestInputFileBuilder.create("test", spiedFs.baseDir(), file.getAbsoluteFile())
            .setContents("This\nis\na text\nfile.")
            .setLanguage(DelphiLanguage.KEY)
            .build();
    spiedFs.add(inputFile);
    return inputFile;
  }

  private ActiveRule createRuleInActiveRules() {
    ActiveRule sonarRule = mock(ActiveRule.class);
    RuleKey ruleKey = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, "RULE");
    when(mockActiveRules.find(ruleKey)).thenReturn(sonarRule);
    when(sonarRule.ruleKey()).thenReturn(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, "RULE"));
    return sonarRule;
  }

  private RuleViolation mockPmdViolation(File file, String ruleName) {
    final Rule rule = mock(Rule.class);
    final RuleViolation pmdViolation = mock(RuleViolation.class);

    when(rule.getName()).thenReturn(ruleName);
    when(rule.getProperty(SCOPE)).thenReturn(RuleScope.ALL.name());
    when(pmdViolation.getFilename()).thenReturn(file.getAbsolutePath());
    when(pmdViolation.getBeginLine()).thenReturn(2);
    when(pmdViolation.getDescription()).thenReturn("Description");
    when(pmdViolation.getRule()).thenReturn(rule);

    return pmdViolation;
  }
}
