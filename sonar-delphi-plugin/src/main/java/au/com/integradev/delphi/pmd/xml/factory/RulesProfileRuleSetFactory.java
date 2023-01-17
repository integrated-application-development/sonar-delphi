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
package au.com.integradev.delphi.pmd.xml.factory;

import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.pmd.xml.DelphiRuleSet;
import au.com.integradev.delphi.utils.PmdLevelUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.Rule;

/** Factory class to create {@link DelphiRuleSet} out of {@link RulesProfile}. */
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
        String clazz = activeRule.getRule().getConfigKey();
        Integer level = PmdLevelUtils.toLevel(activeRule.getSeverity().name());
        String name = activeRule.getRule().getKey();
        String templateName = null;
        String message = activeRule.getRule().getName();
        String description = activeRule.getRule().getDescription();

        Rule template = activeRule.getRule().getTemplate();
        if (template != null) {
          templateName = template.getKey();
        }

        DelphiRule rule = new DelphiRule(clazz, level);
        rule.setName(name);
        rule.setTemplateName(templateName);
        rule.setMessage(message);
        parseDescription(rule, description);

        addRuleProperties(activeRule, rule);

        ruleSet.addRule(rule);
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

  /**
   * Takes an HTML string and parses out the description and example
   *
   * <p>This is needed because PMD separates the description and example into separate XML elements.
   * Meanwhile, Sonar is given one HTML description which we created by appending the PMD
   * description and example in {@link DelphiRule#getHtmlDescription()} As a result, we can't
   * reverse-engineer a PMD rule from an {@link ActiveRule} without parsing that HTML.
   *
   * @param pmdRule The rule we are creating a description and/or example for
   * @param htmlDescription HTML containing a description and/or example
   */
  private void parseDescription(DelphiRule pmdRule, String htmlDescription) {
    if (htmlDescription == null) {
      return;
    }

    Pattern descriptionPattern = Pattern.compile("(?<=<p>)(.*)(?=</p>)", Pattern.DOTALL);
    Matcher descriptionMatcher = descriptionPattern.matcher(htmlDescription);
    if (descriptionMatcher.find()) {
      pmdRule.setDescription(descriptionMatcher.group(1));
    }

    Pattern examplePattern = Pattern.compile("(?<=<pre>)(.*)(?=</pre>)", Pattern.DOTALL);
    Matcher exampleMatcher = examplePattern.matcher(htmlDescription);
    if (exampleMatcher.find()) {
      pmdRule.setExample(exampleMatcher.group(1));
    }
  }
}
