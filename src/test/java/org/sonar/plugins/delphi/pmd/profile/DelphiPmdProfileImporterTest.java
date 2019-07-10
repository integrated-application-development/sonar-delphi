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
package org.sonar.plugins.delphi.pmd.profile;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.utils.PmdLevelUtils.fromLevel;
import static org.sonar.plugins.delphi.utils.PmdLevelUtils.toLevel;

public class DelphiPmdProfileImporterTest {
  private static final int DEFAULT_LEVEL = 1;
  private DelphiPmdProfileImporter importer;
  private ValidationMessages messages;

  private static Reader read(String fileName) {
    try {
      File xmlFile = DelphiUtils.getResource("/org/sonar/plugins/delphi/pmd/xml/" + fileName);
      String xml = DelphiUtils.readFileContent(xmlFile, StandardCharsets.UTF_8.name());
      return new StringReader(xml);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static RuleFinder createRuleFinder() {
    RuleFinder ruleFinder = mock(RuleFinder.class);
    when(ruleFinder.find(any(RuleQuery.class)))
        .then(
            (Answer<Rule>)
                invocation -> {
                  RuleQuery query = (RuleQuery) invocation.getArguments()[0];
                  Rule rule =
                      Rule.create(query.getRepositoryKey(), query.getKey(), "")
                          .setConfigKey("ConfigKey:" + query.getKey())
                          .setSeverity(fromLevel(DEFAULT_LEVEL));

                  if (rule.getKey().equals("TooManyArgumentsRule")) {
                    rule.createParameter("limit");
                  }

                  if (rule.getKey().equals("MyXpathRule")) {
                    rule.createParameter("xpath");
                  }

                  return rule;
                });
    return ruleFinder;
  }

  private static ActiveRule getRule(RulesProfile profile, String key) {
    return profile.getActiveRule(DelphiPmdConstants.REPOSITORY_KEY, key);
  }

  @Before
  public void setUpImporter() {
    messages = ValidationMessages.create();
    importer = new DelphiPmdProfileImporter(createRuleFinder());
  }

  @Test
  public void testShouldImportSimpleProfile() {
    Reader reader = read("simple.xml");

    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules(), hasSize(3));
    assertThat(getRule(profile, "InterfaceNameRule"), is(not(nullValue())));
    assertThat(getRule(profile, "TooManyArgumentsRule"), is(not(nullValue())));
    assertThat(getRule(profile, "TooManyVariablesRule"), is(not(nullValue())));
    assertFalse(messages.hasErrors());
  }

  @Test
  public void testShouldImportXpathRule() {
    Reader reader = read("export_xpath_rules.xml");

    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules(), hasSize(1));
    assertThat(getRule(profile, "MyXpathRule"), is(not(nullValue())));
  }

  @Test
  public void testShouldImportParameter() {
    Reader reader = read("simple.xml");

    RulesProfile profile = importer.importProfile(reader, messages);
    ActiveRule activeRule = getRule(profile, "TooManyArgumentsRule");

    assertThat(activeRule.getParameter("limit"), is("6"));
  }

  @Test
  public void testShouldImportDefaultPriority() {
    Reader reader = read("simple.xml");

    RulesProfile profile = importer.importProfile(reader, messages);
    ActiveRule interfaceNameRule = getRule(profile, "InterfaceNameRule");

    assertThat(toLevel(interfaceNameRule.getSeverity().name()), is(DEFAULT_LEVEL));
  }

  @Test
  public void testShouldImportPriority() {
    Reader reader = read("simple.xml");

    RulesProfile profile = importer.importProfile(reader, messages);

    ActiveRule tooManyArgumentsRule = getRule(profile, "TooManyArgumentsRule");
    ActiveRule tooManyVariablesRule = getRule(profile, "TooManyVariablesRule");

    assertThat(toLevel(tooManyArgumentsRule.getSeverity().name()), is(2));
    assertThat(toLevel(tooManyVariablesRule.getSeverity().name()), is(3));
  }

  @Test
  public void testShouldImportPmdConfigurationWithUnknownNodes() {
    Reader reader = read("complex-with-unknown-nodes.xml");

    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules(), hasSize(3));
    assertThat(getRule(profile, "InterfaceNameRule"), is(not(nullValue())));
    assertThat(getRule(profile, "TooManyArgumentsRule"), is(not(nullValue())));
    assertThat(getRule(profile, "TooManyVariablesRule"), is(not(nullValue())));
    assertFalse(messages.hasErrors());
  }

  @Test
  public void testShouldHandleUnsupportedProperty() {
    Reader reader = read("import_rule_with_unsupported_property.xml");

    RulesProfile profile = importer.importProfile(reader, messages);
    ActiveRule tooManyVariablesRule = getRule(profile, "TooManyVariablesRule");

    assertThat(tooManyVariablesRule.getParameter("limit"), is(nullValue()));
    assertThat(messages.getWarnings(), hasSize(1));
  }

  @Test
  public void testShouldFailOnInvalidXml() {
    Reader reader = new StringReader("not xml");

    importer.importProfile(reader, messages);

    assertThat(messages.getErrors(), hasSize(1));
  }

  @Test
  public void testShouldWarnOnUnknownRule() {
    Reader reader = read("simple.xml");

    importer = new DelphiPmdProfileImporter(mock(RuleFinder.class));
    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules(), is(empty()));
    assertThat(messages.getWarnings(), hasSize(3));
  }

  @Test
  public void testShouldWarnOnRuleClassMissing() {
    Reader reader = read("import_rule_with_missing_class.xml");

    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules(), is(empty()));
    assertThat(messages.getWarnings(), hasSize(1));
  }

  @Test
  public void testMessagesCanBeNull() {
    Reader reader = read("import_rule_with_missing_class.xml");

    RulesProfile profile = importer.importProfile(reader, null);

    assertThat(profile.getActiveRules(), is(empty()));
  }
}