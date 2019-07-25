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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStreamReader;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;

/** Delphi rules definition */
@ServerSide
public class DelphiPmdRulesDefinition implements RulesDefinition {
  private static final String RULES_XML = "/org/sonar/plugins/delphi/pmd/rules.xml";

  public DelphiPmdRulesDefinition() {
    // do nothing
  }

  @Override
  public void define(Context context) {
    NewRepository repository =
        context
            .createRepository(DelphiPmdConstants.REPOSITORY_KEY, DelphiLanguage.KEY)
            .setName(DelphiPmdConstants.REPOSITORY_NAME);

    extractRulesData(repository);

    repository.done();
  }

  private void extractRulesData(NewRepository repository) {
    var rulesReader = new InputStreamReader(getClass().getResourceAsStream(RULES_XML), UTF_8);
    DelphiRuleSet ruleSet = DelphiRuleSetHelper.createFrom(rulesReader);

    for (org.sonar.api.rules.Rule rule : ruleSet.getSonarRules()) {
      NewRule newRule =
          repository
              .createRule(rule.getKey())
              .setName(rule.getName())
              .setHtmlDescription(rule.getDescription())
              .setInternalKey(rule.getConfigKey())
              .setSeverity(rule.getSeverity().name());

      newRule.setDebtRemediationFunction(
          newRule
              .debtRemediationFunctions()
              .constantPerIssue(rule.getParam("baseEffort").getDefaultValue()));

      for (RuleParam param : rule.getParams()) {
        newRule
            .createParam(param.getKey())
            .setDefaultValue(param.getDefaultValue())
            .setType(RuleParamType.parse(param.getType()))
            .setDescription(param.getDescription());
      }
    }
  }
}
