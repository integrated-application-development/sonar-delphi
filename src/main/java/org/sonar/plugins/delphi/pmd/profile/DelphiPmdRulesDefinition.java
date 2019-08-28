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

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.BASE_EFFORT;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.SCOPE;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.TEMPLATE;
import static org.sonar.plugins.delphi.pmd.DelphiPmdConstants.TYPE;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.utils.PmdLevelUtils;

/** Delphi rules definition */
@ServerSide
public class DelphiPmdRulesDefinition implements RulesDefinition {
  public static final String UNDEFINED_BASE_EFFORT =
      "Builtin property 'baseEffort' must be defined for rule: %s";

  private final DelphiPmdRuleSetDefinitionProvider ruleSetDefinitionProvider;

  public DelphiPmdRulesDefinition(DelphiPmdRuleSetDefinitionProvider ruleSetDefinitionProvider) {
    this.ruleSetDefinitionProvider = ruleSetDefinitionProvider;
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
    DelphiRuleSet ruleSet = ruleSetDefinitionProvider.getDefinition();

    for (DelphiRule pmdRule : ruleSet.getRules()) {
      NewRule sonarRule =
          repository
              .createRule(pmdRule.getName())
              .setName(pmdRule.getMessage())
              .setHtmlDescription(pmdRule.getHtmlDescription())
              .setInternalKey(pmdRule.getClazz());

      extractSeverity(pmdRule, sonarRule);
      extractDebtRemediationFunction(pmdRule, sonarRule);
      extractScope(pmdRule, sonarRule);
      extractTemplate(pmdRule, sonarRule);
      extractType(pmdRule, sonarRule);
      extractProperties(pmdRule, sonarRule);
    }
  }

  private void extractSeverity(DelphiRule pmdRule, NewRule sonarRule) {
    String severity =
        Objects.requireNonNullElse(
            PmdLevelUtils.severityFromLevel(pmdRule.getPriority()), Severity.defaultSeverity());

    sonarRule.setSeverity(severity);
  }

  private void extractDebtRemediationFunction(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty baseEffortProperty = pmdRule.getProperty(BASE_EFFORT.name());

    String error = String.format(UNDEFINED_BASE_EFFORT, pmdRule.getName());
    Preconditions.checkArgument(baseEffortProperty != null, error);

    sonarRule.setDebtRemediationFunction(
        sonarRule.debtRemediationFunctions().constantPerIssue(baseEffortProperty.getValue()));
  }

  private void extractScope(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty scopeProperty = pmdRule.getProperty(SCOPE.name());
    if (scopeProperty != null) {
      sonarRule.setScope(RuleScope.valueOf(scopeProperty.getValue()));
    }
  }

  private void extractTemplate(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty templateProperty = pmdRule.getProperty(TEMPLATE.name());
    if (templateProperty != null) {
      sonarRule.setTemplate(Boolean.parseBoolean(templateProperty.getValue()));
    }
  }

  private void extractType(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty typeProperty = pmdRule.getProperty(TYPE.name());
    if (typeProperty != null) {
      sonarRule.setType(RuleType.valueOf(typeProperty.getValue()));
    }
  }

  private void extractProperties(DelphiRule pmdRule, NewRule sonarRule) {
    for (DelphiRuleProperty property : pmdRule.getProperties()) {
      if (property.isBuiltinProperty()) {
        continue;
      }

      sonarRule
          .createParam(property.getName())
          .setDefaultValue(property.getValue())
          .setType(isNumeric(property.getValue()) ? RuleParamType.INTEGER : RuleParamType.STRING)
          .setDescription(property.getName());
    }
  }
}
