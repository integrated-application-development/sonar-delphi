/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.executor;

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.BASE_EFFORT;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SCOPE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.pmd.DelphiPmdConfiguration;
import au.com.integradev.delphi.pmd.DelphiPmdConstants;
import au.com.integradev.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import au.com.integradev.delphi.pmd.violation.DelphiPmdViolationRecorder;
import au.com.integradev.delphi.pmd.violation.DelphiRuleViolation;
import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.pmd.xml.DelphiRuleSet;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import net.sourceforge.pmd.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;

class DelphiPmdExecutorTest {

  private static final String ROOT_PATH = "/au/com/integradev/delphi";
  private static final File ROOT_DIR = DelphiUtils.getResource(ROOT_PATH);

  private SensorContext context;
  private DelphiPmdExecutor executor;
  private DelphiPmdConfiguration pmdConfiguration;
  private DelphiPmdViolationRecorder violationRecorder;

  @BeforeEach
  void setup() {
    DefaultFileSystem fileSystem = new DefaultFileSystem(ROOT_DIR).setWorkDir(ROOT_DIR.toPath());
    Configuration configuration = mock(Configuration.class);
    DelphiPmdRuleSetDefinitionProvider provider = new DelphiPmdRuleSetDefinitionProvider();

    context = spy(SensorContextTester.create(ROOT_DIR));
    ActiveRules rules = mock(ActiveRules.class);
    pmdConfiguration = spy(new DelphiPmdConfiguration(fileSystem, configuration, provider));
    violationRecorder = mock(DelphiPmdViolationRecorder.class);

    executor =
        spy(new DelphiPmdExecutor(context, rules, pmdConfiguration, violationRecorder, provider));
    executor.setup();
  }

  @Test
  void testNonexistentRuleSetIllegalState() {
    File badFile = mock(File.class);
    when(badFile.getAbsolutePath()).thenReturn("does/not/exist.xml");
    when(pmdConfiguration.dumpXmlRuleSet(anyString(), anyString())).thenReturn(badFile);

    assertThatThrownBy(executor::setup).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testAddBuiltinProperties() {
    // The SwallowedExceptionsRule has 3 builtin properties: BASE_EFFORT, SCOPE and TYPE
    DelphiRule rule = new DelphiRule();
    rule.setName("SwallowedExceptionsRule");
    rule.setClazz("au.com.integradev.delphi.pmd.rules.SwallowedExceptionsRule");
    rule.setPriority(2);

    DelphiRuleSet ruleSet = new DelphiRuleSet();
    ruleSet.addRule(rule);

    executor.addBuiltinProperties(ruleSet);

    assertThat(rule.getProperties()).hasSize(3);
    assertThat(rule.getProperty(BASE_EFFORT.name())).isNotNull();
    assertThat(rule.getProperty(SCOPE.name())).isNotNull();
    assertThat(rule.getProperty(TYPE.name())).isNotNull();
  }

  @Test
  void testAddBuiltinPropertiesToCustomRule() {
    // A custom template rule, created via the SonarQube web interface
    // The XPathRule template has 2 builtin properties: BASE_EFFORT and TEMPLATE
    // The TEMPLATE property should not be inherited by the custom rule
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty scopeProperty = new DelphiRuleProperty(SCOPE.name(), "TEST");

    rule.setName("SomeCustomXPathRule");
    rule.setTemplateName("XPathRule");
    rule.setClazz(DelphiPmdConstants.TEMPLATE_XPATH_CLASS);
    rule.setPriority(2);
    rule.addProperty(scopeProperty);

    DelphiRuleSet ruleSet = new DelphiRuleSet();
    ruleSet.addRule(rule);

    executor.addBuiltinProperties(ruleSet);

    assertThat(rule.getProperties()).hasSize(2);
    assertThat(rule.getProperty(BASE_EFFORT.name())).isNotNull();
    assertThat(rule.getProperty(SCOPE.name())).isEqualTo(scopeProperty);
  }

  @Test
  void testAddBuiltinPropertiesToNonexistentRule() {
    // An undefined rule in ActiveRules should be impossible
    // Undefined -> The rule is not specified in rules.xml, either concretely or as a template
    DelphiRule rule = new DelphiRule();
    rule.setName("NonexistentRule");
    rule.setClazz("au.com.integradev.delphi.pmd.rules.NonexistentRule");
    rule.setPriority(2);

    DelphiRuleSet ruleSet = new DelphiRuleSet();
    ruleSet.addRule(rule);

    assertThatThrownBy(() -> executor.addBuiltinProperties(ruleSet))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Rule definition not found for NonexistentRule");
  }

  @Test
  void testShouldNotReportZeroViolations() {
    // The report is empty if executor.execute(DelphiFile) is never called
    executor.complete();

    verify(violationRecorder, never()).saveViolation(any(DelphiRuleViolation.class), eq(context));
  }

  @Test
  void testViolationRecorderExceptionShouldThrowFatalError() {
    doAnswer(
            invocation -> {
              ((Report) invocation.getArgument(0))
                  .addRuleViolation(mock(DelphiRuleViolation.class));
              return null;
            })
        .when(pmdConfiguration)
        .dumpXmlReport(any(Report.class));

    doThrow(new RuntimeException()).when(violationRecorder).saveViolation(any(), any());
    assertThatThrownBy(() -> executor.complete()).isInstanceOf(FatalExecutorError.class);
  }
}
