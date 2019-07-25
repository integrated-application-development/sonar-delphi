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
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
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

  public ActiveRulesRuleSetFactory(ActiveRules activeRules, String repositoryKey) {
    this.activeRules = activeRules;
    this.repositoryKey = repositoryKey;
  }

  @Override
  public DelphiRuleSet create() {

    final Collection<ActiveRule> rules = this.activeRules.findByRepository(repositoryKey);
    DelphiRuleSet ruleset = new DelphiRuleSet();
    ruleset.setName(repositoryKey);
    ruleset.setDescription(String.format("Sonar Profile: %s", repositoryKey));
    for (ActiveRule rule : rules) {
      String configKey = rule.internalKey();
      DelphiRule delphiRule = new DelphiRule(configKey, PmdLevelUtils.toLevel(rule.severity()));
      delphiRule.setName(rule.ruleKey().rule());
      addRuleProperties(rule, delphiRule);
      ruleset.addRule(delphiRule);

      delphiRule.processXpath(rule.ruleKey().rule());
    }
    return ruleset;
  }

  private void addRuleProperties(ActiveRule activeRule, DelphiRule pmdRule) {
    if ((activeRule.params() != null) && !activeRule.params().isEmpty()) {
      for (var activeRuleParam : activeRule.params().entrySet()) {
        var property = new DelphiRuleProperty(activeRuleParam.getKey(), activeRuleParam.getValue());
        pmdRule.addProperty(property);
      }
    }
  }

  @Override
  public void close() {
    // Do nothing
  }
}
