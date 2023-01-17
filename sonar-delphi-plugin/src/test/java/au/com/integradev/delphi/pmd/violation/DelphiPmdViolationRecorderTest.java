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
package au.com.integradev.delphi.pmd.violation;

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SCOPE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiPlugin;
import au.com.integradev.delphi.core.DelphiLanguage;
import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import au.com.integradev.delphi.pmd.DelphiPmdConstants;
import au.com.integradev.delphi.symbol.scope.DelphiScope;
import au.com.integradev.delphi.symbol.scope.TypeScope;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.ScopedType;
import java.io.File;
import java.util.Collections;
import java.util.Optional;
import net.sourceforge.pmd.Rule;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
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

class DelphiPmdViolationRecorderTest {
  private static final String RULE_KEY = "RULE";

  private final File baseDir = new File("").getAbsoluteFile();
  private final DefaultFileSystem spiedFs = spy(new DefaultFileSystem(baseDir));
  private final Configuration configuration = mock(Configuration.class);
  private final EnvironmentVariableProvider environmentVariableProvider =
      mock(EnvironmentVariableProvider.class);
  private final DelphiProjectHelper projectHelper =
      new DelphiProjectHelper(configuration, spiedFs, environmentVariableProvider);
  private final ActiveRules mockActiveRules = mock(ActiveRules.class);
  private final SensorContext mockContext = mock(SensorContext.class);

  private final DelphiPmdViolationRecorder violationRecorder =
      new DelphiPmdViolationRecorder(projectHelper, mockActiveRules);

  DelphiPmdViolationRecorderTest() {
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);
    RuleKey ruleKey = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, RULE_KEY);
    ActiveRule activeRule = mock(ActiveRule.class);
    when(mockActiveRules.find(ruleKey)).thenReturn(activeRule);
    when(activeRule.ruleKey()).thenReturn(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, RULE_KEY));
  }

  @Test
  void testShouldConvertPmdViolationToSonarViolation() {
    final File file = new File(baseDir, "FileWithViolation.java");
    final DefaultInputFile inputFile = addToFileSystem(file);
    final DelphiRuleViolation pmdViolation = mockViolation(file);
    final NewIssue newIssue = mock(NewIssue.class);
    final NewIssueLocation issueLocation = mock(NewIssueLocation.class);

    when(mockContext.newIssue()).thenReturn(newIssue);
    when(newIssue.forRule(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, RULE_KEY)))
        .thenReturn(newIssue);
    when(newIssue.newLocation()).thenReturn(issueLocation);
    when(newIssue.at(issueLocation)).thenReturn(newIssue);
    when(issueLocation.on(inputFile)).thenReturn(issueLocation);
    when(issueLocation.message("Description")).thenReturn(issueLocation);
    when(issueLocation.at(any(TextRange.class))).thenReturn(issueLocation);

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verify(mockContext).newIssue();
    verify(newIssue).save();
  }

  @Test
  void testShouldRecordViolationInTestCodeWhenScopeIsTest() {
    final File file = new File(baseDir, "FileWithViolation.java");
    InputFile inputFile = addToFileSystem(file);
    final DelphiRuleViolation pmdViolation = mockViolation(file);

    Type testSuiteType = mock(Type.class);
    when(testSuiteType.isSubTypeOf("TEST")).thenReturn(true);
    when(pmdViolation.getClassType()).thenReturn(testSuiteType);
    when(pmdViolation.getRule().getProperty(SCOPE)).thenReturn(RuleScope.TEST.name());
    when(configuration.get(DelphiPlugin.TEST_SUITE_TYPE_KEY)).thenReturn(Optional.of("TEST"));

    final NewIssue newIssue = mock(NewIssue.class);
    final NewIssueLocation issueLocation = mock(NewIssueLocation.class);

    when(mockContext.newIssue()).thenReturn(newIssue);
    when(newIssue.forRule(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, RULE_KEY)))
        .thenReturn(newIssue);
    when(newIssue.newLocation()).thenReturn(issueLocation);
    when(newIssue.at(issueLocation)).thenReturn(newIssue);
    when(issueLocation.on(inputFile)).thenReturn(issueLocation);
    when(issueLocation.message("Description")).thenReturn(issueLocation);
    when(issueLocation.at(any(TextRange.class))).thenReturn(issueLocation);

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verify(mockContext).newIssue();
    verify(newIssue).save();
  }

  @Test
  void testShouldIgnoreViolationInTestCodeWhenScopeIsMain() {
    final File file = new File(baseDir, "FileWithViolation.java");
    addToFileSystem(file);
    final DelphiRuleViolation pmdViolation = mockViolation(file);

    Type testSuiteType = mock(Type.class);
    when(testSuiteType.isSubTypeOf("TEST")).thenReturn(true);
    when(pmdViolation.getClassType()).thenReturn(testSuiteType);
    when(pmdViolation.getRule().getProperty(SCOPE)).thenReturn(RuleScope.MAIN.name());
    when(configuration.get(DelphiPlugin.TEST_SUITE_TYPE_KEY)).thenReturn(Optional.of("TEST"));

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verifyNoMoreInteractions(mockActiveRules);
    verifyNoMoreInteractions(mockContext);
  }

  @Test
  void testShouldIgnoreViolationInNestedTestCodeWhenScopeIsMain() {
    final File file = new File(baseDir, "FileWithViolation.java");
    addToFileSystem(file);
    final DelphiRuleViolation pmdViolation = mockViolation(file);

    Type testSuiteType = mock(Type.class);
    TypeScope testSuitTypeScope = mock(TypeScope.class);
    DelphiScope scope = mock(DelphiScope.class);
    ScopedType nestedType = mock(ScopedType.class);

    when(testSuiteType.isSubTypeOf("TEST")).thenReturn(true);
    when(testSuitTypeScope.getType()).thenReturn(testSuiteType);
    when(scope.getParent()).thenReturn(testSuitTypeScope);
    when(nestedType.typeScope()).thenReturn(scope);
    when(pmdViolation.getClassType()).thenReturn(nestedType);
    when(pmdViolation.getRule().getProperty(SCOPE)).thenReturn(RuleScope.MAIN.name());
    when(configuration.get(DelphiPlugin.TEST_SUITE_TYPE_KEY)).thenReturn(Optional.of("TEST"));

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verifyNoMoreInteractions(mockActiveRules);
    verifyNoMoreInteractions(mockContext);
  }

  @Test
  void testShouldIgnoreViolationInMainCodeWhenScopeIsTest() {
    final File file = new File(baseDir, "FileWithViolation.java");
    addToFileSystem(file);
    final DelphiRuleViolation pmdViolation = mockViolation(file);

    Type testSuiteType = mock(Type.class);
    when(testSuiteType.isSubTypeOf("TEST")).thenReturn(false);
    when(pmdViolation.getClassType()).thenReturn(testSuiteType);
    when(pmdViolation.getRule().getProperty(SCOPE)).thenReturn(RuleScope.TEST.name());
    when(configuration.get(DelphiPlugin.TEST_SUITE_TYPE_KEY)).thenReturn(Optional.of("TEST"));

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verifyNoMoreInteractions(mockActiveRules);
    verifyNoMoreInteractions(mockContext);
  }

  @Test
  void testShouldIgnoreViolationWhenSuppressed() {
    final File file = new File(baseDir, "FileWithViolation.java");
    addToFileSystem(file);
    final DelphiRuleViolation pmdViolation = mockViolation(file);

    when(pmdViolation.isSuppressed()).thenReturn(true);
    violationRecorder.saveViolation(pmdViolation, mockContext);

    verifyNoMoreInteractions(mockActiveRules);
    verifyNoMoreInteractions(mockContext);
  }

  @Test
  void testShouldThrowOnUnknownResource() {
    final File unknownFile = new File(baseDir, "UNKNOWN.pas");
    final DelphiRuleViolation pmdViolation = mockViolation(unknownFile);
    final NewIssue newIssue = mock(NewIssue.class);

    when(mockContext.newIssue()).thenReturn(newIssue);
    when(newIssue.forRule(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, RULE_KEY)))
        .thenReturn(newIssue);

    assertThatThrownBy(() -> violationRecorder.saveViolation(pmdViolation, mockContext))
        .isInstanceOf(RuntimeException.class)
        .hasMessageMatching("Input file could not be found: '.*UNKNOWN.pas'");
  }

  @Test
  void testShouldIgnoreViolationOnUnknownRule() {
    final File file = new File("src/FileWithUnknownViolations.pas");
    addToFileSystem(file);

    final String ruleName = "UNKNOWN";
    final Rule unknown = mock(Rule.class);
    when(unknown.getName()).thenReturn(ruleName);
    when(unknown.getProperty(SCOPE)).thenReturn(RuleScope.ALL.name());

    final DelphiRuleViolation pmdViolation = mockViolation(file);
    when(pmdViolation.getRule()).thenReturn(unknown);
    final RuleKey expectedRuleKey1 = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, ruleName);
    final RuleKey expectedRuleKey2 = RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, ruleName);

    violationRecorder.saveViolation(pmdViolation, mockContext);

    verify(mockActiveRules).find(expectedRuleKey1);
    verify(mockActiveRules).find(expectedRuleKey2);
    verify(spiedFs, never()).inputFile(any(FilePredicate.class));
    verifyNoMoreInteractions(mockContext);
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

  private DelphiRuleViolation mockViolation(File file) {
    final Rule rule = mock(Rule.class);
    final DelphiRuleViolation pmdViolation = mock(DelphiRuleViolation.class);

    when(rule.getName()).thenReturn(RULE_KEY);
    when(rule.getProperty(SCOPE)).thenReturn(RuleScope.ALL.name());
    when(pmdViolation.getFilename()).thenReturn(file.getAbsolutePath());
    when(pmdViolation.getBeginLine()).thenReturn(2);
    when(pmdViolation.getDescription()).thenReturn("Description");
    when(pmdViolation.getRule()).thenReturn(rule);
    when(pmdViolation.isSuppressed()).thenReturn(false);

    return pmdViolation;
  }
}
