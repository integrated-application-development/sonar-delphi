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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CharMatcher;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DelphiPmdProfileExporterTest {

  private static final CharMatcher EOLS = CharMatcher.anyOf("\n\r");
  private final DelphiPmdProfileExporter exporter = new DelphiPmdProfileExporter();

  @org.junit.Rule
  public ExpectedException exceptionCatcher = ExpectedException.none();

  private static RulesProfile importProfile(String configuration) {
    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);

    RulesDefinition.Repository repository = context.repository(DelphiPmdConstants.REPOSITORY_KEY);
    assertThat(repository).isNotNull();

    RuleFinder ruleFinder = createRuleFinder(repository.rules());
    DelphiPmdProfileImporter importer = new DelphiPmdProfileImporter(ruleFinder);

    return importer.importProfile(new StringReader(configuration), ValidationMessages.create());
  }

  private static RuleFinder createRuleFinder(final List<RulesDefinition.Rule> rules) {
    RuleFinder ruleFinder = mock(RuleFinder.class);
    final List<Rule> convertedRules = convert(rules);

    when(ruleFinder.find(any(RuleQuery.class))).then((Answer<Rule>) invocation -> {
      RuleQuery query = (RuleQuery) invocation.getArguments()[0];
      for (Rule rule : convertedRules) {
        if (query.getKey().equals(rule.getKey())) {
          return rule;
        }
      }
      return null;
    });
    return ruleFinder;
  }

  private static List<Rule> convert(List<RulesDefinition.Rule> rules) {
    List<Rule> results = new ArrayList<>(rules.size());
    for (RulesDefinition.Rule rule : rules) {
      Rule newRule = Rule.create(rule.repository().key(), rule.key(), rule.name())
          .setDescription(rule.htmlDescription())
          .setRepositoryKey(rule.repository().key())
          .setConfigKey(rule.internalKey());

      if (!rule.params().isEmpty()) {
        for (Param param : rule.params()) {
          newRule.createParameter(param.name()).setDefaultValue(param.defaultValue());
        }
      }
      results.add(newRule);
    }
    return results;
  }

  private static Condition<String> equalsIgnoreEOL(String text) {
    final String strippedText = EOLS.removeFrom(text);

    return new Condition<String>() {
      @Override
      public boolean matches(String value) {
        return EOLS.removeFrom(value).equals(strippedText);
      }
    }.as("equal to " + text);
  }

  private static String getRuleSetXml(String fileName) {
    try {
      File xmlFile = DelphiUtils.getResource("/org/sonar/plugins/delphi/pmd/xml/" + fileName);
      return DelphiUtils.readFileContent(xmlFile, StandardCharsets.UTF_8.name());
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  public void testShouldExportPmdProfileOnWriter() {
    String importedXml = getRuleSetXml("export_simple.xml");
    StringWriter stringWriter = new StringWriter();

    exporter.exportProfile(importProfile(importedXml), stringWriter);

    assertThat(stringWriter.toString()).satisfies(equalsIgnoreEOL(importedXml));
  }

  @Test
  public void testShouldAbortExportOnWriterException() throws IOException {
    String importedXml = getRuleSetXml("export_simple.xml");
    final Writer writer = mock(Writer.class);
    doThrow(new IOException("test")).when(writer).write(anyString());

    exceptionCatcher.expect(IllegalStateException.class);
    exceptionCatcher.expectMessage("An exception occurred while generating the PMD configuration file from profile: null");

    exporter.exportProfile(importProfile(importedXml), writer);
  }

  @Test
  public void testShouldSkipEmptyParam() {
    String importedXml = getRuleSetXml("export_rule_with_empty_param.xml");

    String expected =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<ruleset name=\"delph\">\n"
        + "  <description>Sonar Profile: delph</description>\n"
        + "  <rule class=\"org.sonar.plugins.delphi.pmd.rules.TooLongLineRule\" message=\"Line too long\" name=\"TooLongLineRule\" language=\"delph\">\n"
        + "    <priority>3</priority>\n"
        + "    <description>Code lines should not be too long.</description>\n"
        + "    <properties>\n"
        + "      <property name=\"baseEffort\" value=\"2min\" />\n"
        + "    </properties>\n"
        + "  </rule>\n"
        + "</ruleset>";

    final StringWriter writer = new StringWriter();
    exporter.exportProfile(importProfile(importedXml), writer);
    assertThat(writer.toString()).satisfies(equalsIgnoreEOL(expected));
  }

  @Test
  public void testShouldSkipAllEmptyParams() {
    String importedXml = getRuleSetXml("export_rule_with_all_params_empty.xml");

    String expected =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<ruleset name=\"delph\">\n"
        + "  <description>Sonar Profile: delph</description>\n"
        + "  <rule class=\"org.sonar.plugins.delphi.pmd.rules.TooLongLineRule\" message=\"Line too long\" name=\"TooLongLineRule\" language=\"delph\">\n"
        + "    <priority>3</priority>\n"
        + "    <description>Code lines should not be too long.</description>\n"
        + "  </rule>\n"
        + "</ruleset>";

    final StringWriter writer = new StringWriter();
    exporter.exportProfile(importProfile(importedXml), writer);
    assertThat(writer.toString()).satisfies(equalsIgnoreEOL(expected));
  }

  @Test
  public void testShouldExportEmptyConfigurationAsXml() {
    final StringWriter writer = new StringWriter();

    exporter.exportProfile(RulesProfile.create(), writer);

    String expected =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<ruleset name=\"delph\">\n"
        + "  <description>Sonar Profile: delph</description>\n"
        + "</ruleset>";

    assertThat(writer.toString()).satisfies(equalsIgnoreEOL(expected));
  }

  @Test
  public void testShouldExportXPathRule() {
    Rule rule = Rule.create(DelphiPmdConstants.REPOSITORY_KEY, "MyXpathRule", "This is my own xpath rule.")
        .setConfigKey(DelphiPmdConstants.XPATH_CLASS)
        .setRepositoryKey(DelphiPmdConstants.REPOSITORY_KEY);
    rule.createParameter(DelphiPmdConstants.XPATH_EXPRESSION_PARAM);

    RulesProfile profile = RulesProfile.create();
    ActiveRule xpath = profile.activateRule(rule, null);
    xpath.setParameter(DelphiPmdConstants.XPATH_EXPRESSION_PARAM, "//FieldDeclaration");

    final StringWriter writer = new StringWriter();
    exporter.exportProfile(profile, writer);

    String expected = getRuleSetXml("/export_xpath_rules.xml");
    assertThat(writer.toString()).satisfies(equalsIgnoreEOL(expected));
  }
}
