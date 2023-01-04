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
package org.sonar.plugins.delphi.pmd.xml.factory;

import java.util.Collection;
import java.util.Map;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.utils.PmdLevelUtils;

/**
 * Factory class to create {@link org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet} out of {@link
 * org.sonar.api.batch.rule.ActiveRules}.
 */
public class ActiveRulesRuleSetFactory implements RuleSetFactory {

  private final ActiveRules activeRules;
  private final String repositoryKey;
  private final SonarProduct sonarProduct;
  private final DelphiPmdRuleSetDefinitionProvider pmdRuleSetDefinitionProvider;

  public ActiveRulesRuleSetFactory(
      ActiveRules activeRules,
      String repositoryKey,
      SonarProduct sonarProduct,
      DelphiPmdRuleSetDefinitionProvider pmdRuleSetDefinitionProvider) {
    this.activeRules = activeRules;
    this.repositoryKey = repositoryKey;
    this.sonarProduct = sonarProduct;
    this.pmdRuleSetDefinitionProvider = pmdRuleSetDefinitionProvider;
  }

  @Override
  public DelphiRuleSet create() {
    final Collection<ActiveRule> rules = this.activeRules.findByRepository(repositoryKey);
    DelphiRuleSet ruleset = new DelphiRuleSet();
    ruleset.setName(repositoryKey);
    ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));

    for (ActiveRule rule : rules) {
      String pmdClassName = getFullyQualifiedRuleClassName(rule);
      DelphiRule delphiRule = new DelphiRule(pmdClassName, getPmdLevel(rule));
      delphiRule.setName(rule.ruleKey().rule());
      delphiRule.setTemplateName(rule.templateRuleKey());
      addRuleProperties(rule, delphiRule);
      ruleset.addRule(delphiRule);
    }

    return ruleset;
  }

  private String getFullyQualifiedRuleClassName(ActiveRule activeRule) {
    var pmdRule =
        pmdRuleSetDefinitionProvider.getDefinition().getRules().stream()
            .filter(r -> activeRuleMatchesPmdRule(activeRule, r))
            .findFirst();

    if (pmdRule.isEmpty()) {
      throw new RuntimeException(
          "Rule name "
              + activeRule.ruleKey().toString()
              + " (template name "
              + activeRule.templateRuleKey()
              + ") does not correspond to PMD rule class");
    } else {
      return pmdRule.get().getClazz();
    }
  }

  private boolean activeRuleMatchesPmdRule(ActiveRule activeRule, DelphiRule pmdRule) {
    String templateRuleKey = activeRule.templateRuleKey();
    return (templateRuleKey != null && templateRuleKey.equals(pmdRule.getName()))
        || activeRule.ruleKey().rule().equals(pmdRule.getName());
  }

  private Integer getPmdLevel(ActiveRule rule) {
    if (sonarProduct == SonarProduct.SONARLINT) {
      // SonarLint doesn't implement ActiveRule::severity
      return 3;
    }
    return PmdLevelUtils.toLevel(rule.severity());
  }

  private void addRuleProperties(ActiveRule activeRule, DelphiRule pmdRule) {
    Map<String, String> params = activeRule.params();
    if (params == null || params.isEmpty()) {
      return;
    }

    for (var activeRuleParam : params.entrySet()) {
      var property = new DelphiRuleProperty(activeRuleParam.getKey(), activeRuleParam.getValue());
      pmdRule.addProperty(property);
    }
  }
}
