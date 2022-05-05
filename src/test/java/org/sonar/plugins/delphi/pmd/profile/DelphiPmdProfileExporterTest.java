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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
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
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiPmdProfileExporterTest {

  private final DelphiPmdProfileExporter exporter = new DelphiPmdProfileExporter();

  private static RulesProfile importProfile(String configuration) {
    var definitionProvider = mock(DelphiPmdRuleSetDefinitionProvider.class);
    DelphiRuleSet ruleSet = DelphiRuleSetHelper.createFrom(new StringReader(configuration));

    when(definitionProvider.getDefinition()).thenReturn(ruleSet);

    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition(definitionProvider);
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

    when(ruleFinder.find(any(RuleQuery.class)))
        .then(
            (Answer<Rule>)
                invocation -> {
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
      Rule newRule =
          Rule.create(rule.repository().key(), rule.key(), rule.name())
              .setDescription(rule.htmlDescription())
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

  private static String getRuleSetXml(String fileName) {
    try {
      File xmlFile = DelphiUtils.getResource("/org/sonar/plugins/delphi/pmd/xml/" + fileName);
      return DelphiUtils.readFileContent(xmlFile, StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void assertExportFormat(String importFile, String exportFile) {
    String importedXml = getRuleSetXml(importFile);
    String expected = getRuleSetXml(exportFile);

    StringWriter stringWriter = new StringWriter();
    exporter.exportProfile(importProfile(importedXml), stringWriter);

    assertThat(stringWriter).hasToString(expected);
  }

  @Test
  void testShouldExportPmdProfileOnWriter() {
    assertExportFormat("import_simple.xml", "export_simple.xml");
  }

  @Test
  void testShouldSkipBuiltinProperties() {
    assertExportFormat(
        "import_rule_with_builtin_properties.xml", "export_rule_with_builtin_properties.xml");
  }

  @Test
  void testShouldSkipEmptyProperty() {
    assertExportFormat("import_rule_with_empty_param.xml", "export_rule_with_empty_param.xml");
  }

  @Test
  void testShouldExportEmptyConfigurationAsXml() {
    final StringWriter writer = new StringWriter();

    exporter.exportProfile(RulesProfile.create(), writer);

    String expected =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<ruleset name=\"delph\">\n"
            + "  <description>Sonar Profile: delph</description>\n"
            + "</ruleset>\n";

    assertThat(writer).hasToString(expected);
  }

  @Test
  void testShouldAbortExportOnWriterException() throws IOException {
    String importedXml = getRuleSetXml("import_simple.xml");
    final Writer writer = mock(Writer.class);
    doThrow(new IOException("test")).when(writer).write(anyString());

    assertThatThrownBy(() -> exporter.exportProfile(importProfile(importedXml), writer))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(String.format(DelphiPmdProfileExporter.PROFILE_EXPORT_ERROR, "null"));
  }

  @Test
  void testShouldExportXPathRule() {
    Rule rule =
        Rule.create(DelphiPmdConstants.REPOSITORY_KEY, "MyXpathRule", "This is my own xpath rule.")
            .setConfigKey(DelphiPmdConstants.TEMPLATE_XPATH_CLASS)
            .setRepositoryKey(DelphiPmdConstants.REPOSITORY_KEY);
    rule.createParameter(DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM);

    RulesProfile profile = RulesProfile.create();
    ActiveRule xpath = profile.activateRule(rule, null);
    xpath.setParameter(DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM, "//FieldDeclaration");

    final StringWriter writer = new StringWriter();
    exporter.exportProfile(profile, writer);

    assertThat(writer).hasToString(getRuleSetXml("/xpath_rules.xml"));
  }
}
