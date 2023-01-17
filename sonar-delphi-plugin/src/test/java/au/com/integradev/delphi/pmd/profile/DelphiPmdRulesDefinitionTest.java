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
package au.com.integradev.delphi.pmd.profile;

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.BASE_EFFORT;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SCOPE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.pmd.DelphiPmdConstants;
import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.pmd.xml.DelphiRuleSet;
import au.com.integradev.delphi.pmd.xml.DelphiRuleSetHelper;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.server.rule.RulesDefinition;

class DelphiPmdRulesDefinitionTest {

  @Test
  void testShouldDefineRules() {
    var provider = mock(DelphiPmdRuleSetDefinitionProvider.class);
    when(provider.getDefinition()).thenReturn(getRuleSet("import_simple.xml"));

    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition(provider);
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(DelphiPmdConstants.REPOSITORY_KEY);

    assertThat(repository).isNotNull();
    assertThat(repository.rules()).hasSize(3);
    assertThat(repository.rule("InterfaceNameRule")).isNotNull();
    assertThat(repository.rule("TooManyArgumentsRule")).isNotNull();
    assertThat(repository.rule("TooManyVariablesRule")).isNotNull();
  }

  @Test
  void testSecurityStandardsShouldBeExtracted() {
    var provider = mock(DelphiPmdRuleSetDefinitionProvider.class);
    when(provider.getDefinition()).thenReturn(getRuleSet("import_security_hotspots.xml"));

    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition(provider);
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(DelphiPmdConstants.REPOSITORY_KEY);

    assertThat(repository).isNotNull();
    assertThat(repository.rules()).hasSize(1);

    RulesDefinition.Rule hotspot = repository.rule("DoNotJumpOutTheWindowRule");
    assertThat(hotspot).isNotNull();
    assertThat(hotspot.securityStandards())
        .containsExactlyInAnyOrder(
            "cwe:1", "cwe:2", "cwe:3", "owaspTop10:a1", "owaspTop10:a2", "owaspTop10:a3");
  }

  @Test
  void testScopePropertyOnTemplateAndCustomRules() {
    DelphiRule templateDefinition = new DelphiRule();
    templateDefinition.setName("MyTemplateRule");
    templateDefinition.setMessage("Test");
    templateDefinition.setDescription("Test");
    templateDefinition.addProperty(new DelphiRuleProperty(BASE_EFFORT.name(), "1min"));
    templateDefinition.addProperty(new DelphiRuleProperty(SCOPE.name(), "MAIN"));
    templateDefinition.addProperty(new DelphiRuleProperty(TEMPLATE.name(), "true"));

    DelphiRule customDefinition = new DelphiRule();
    customDefinition.setName("MyCustomRule");
    customDefinition.setMessage("Test");
    customDefinition.setDescription("Test");
    customDefinition.addProperty(new DelphiRuleProperty(BASE_EFFORT.name(), "1min"));
    customDefinition.addProperty(new DelphiRuleProperty(SCOPE.name(), "TEST"));
    customDefinition.setTemplateName(templateDefinition.getName());

    DelphiRuleSet ruleSet = new DelphiRuleSet();
    ruleSet.addRule(templateDefinition);
    ruleSet.addRule(customDefinition);

    var provider = mock(DelphiPmdRuleSetDefinitionProvider.class);
    when(provider.getDefinition()).thenReturn(ruleSet);

    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition(provider);
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(DelphiPmdConstants.REPOSITORY_KEY);

    assertThat(repository).isNotNull();
    assertThat(repository.rules()).hasSize(2);

    RulesDefinition.Rule template = repository.rule(templateDefinition.getName());
    assertThat(template).isNotNull();
    assertThat(template.scope()).isEqualTo(RuleScope.MAIN);

    RulesDefinition.Rule custom = repository.rule(customDefinition.getName());
    assertThat(custom).isNotNull();
    assertThat(custom.scope()).isEqualTo(RuleScope.TEST);
  }

  @Test
  void testShouldAbortExportOnWriterException() {
    var provider = mock(DelphiPmdRuleSetDefinitionProvider.class);
    when(provider.getDefinition())
        .thenReturn(getRuleSet("definition_missing_required_property.xml"));

    DelphiPmdRulesDefinition definition = new DelphiPmdRulesDefinition(provider);
    RulesDefinition.Context context = new RulesDefinition.Context();

    assertThatThrownBy(() -> definition.define(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            String.format(DelphiPmdRulesDefinition.UNDEFINED_BASE_EFFORT, "InterfaceNameRule"));
  }

  private static DelphiRuleSet getRuleSet(String fileName) {
    try {
      File xmlFile = DelphiUtils.getResource("/au/com/integradev/delphi/pmd/xml/" + fileName);
      return DelphiRuleSetHelper.createFrom(new FileReader(xmlFile, StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
