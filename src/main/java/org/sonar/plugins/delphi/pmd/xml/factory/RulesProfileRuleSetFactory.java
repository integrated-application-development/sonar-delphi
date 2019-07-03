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

import java.util.List;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.utils.PmdLevelUtils;

/**
 * Factory class to create {@link DelphiRuleSet} out of {@link RulesProfile}.
 */
public class RulesProfileRuleSetFactory implements RuleSetFactory {

  private final RulesProfile rulesProfile;
  private final String repositoryKey;

  public RulesProfileRuleSetFactory(RulesProfile rulesProfile, String repositoryKey) {
    this.rulesProfile = rulesProfile;
    this.repositoryKey = repositoryKey;
  }

  @Override
  public DelphiRuleSet create() {

    final DelphiRuleSet ruleSet = new DelphiRuleSet();
    ruleSet.setName(repositoryKey);
    ruleSet.setDescription(String.format("Sonar Profile: %s", repositoryKey));

    final List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(repositoryKey);

    for (ActiveRule activeRule : activeRules) {
      if (activeRule.getRule().getRepositoryKey().equals(repositoryKey)) {
        String configKey = activeRule.getRule().getConfigKey();
        Integer level = PmdLevelUtils.toLevel(activeRule.getSeverity().name());
        DelphiRule rule = new DelphiRule(configKey, level);
        addRuleProperties(activeRule, rule);
        ruleSet.addRule(rule);
        rule.processXpath(activeRule.getRuleKey());
      }
    }

    return ruleSet;
  }

  private void addRuleProperties(ActiveRule activeRule, DelphiRule pmdRule) {
    if ((activeRule.getActiveRuleParams() != null) && !activeRule.getActiveRuleParams().isEmpty()) {
      for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
        String key = activeRuleParam.getRuleParam().getKey();
        String value = activeRuleParam.getValue();
        pmdRule.addProperty(new DelphiRuleProperty(key, value));
      }
    }
  }

  @Override
  public void close() {
    // Unnecessary in this class.
  }
}
