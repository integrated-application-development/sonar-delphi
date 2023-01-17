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
package au.com.integradev.delphi.pmd.profile;

import static au.com.integradev.delphi.pmd.DelphiPmdConstants.BASE_EFFORT;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SCOPE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SECURITY_STANDARD_CWE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.SECURITY_STANDARD_OWASP;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TEMPLATE;
import static au.com.integradev.delphi.pmd.DelphiPmdConstants.TYPE;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import au.com.integradev.delphi.core.DelphiLanguage;
import au.com.integradev.delphi.pmd.DelphiPmdConstants;
import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.pmd.xml.DelphiRuleSet;
import au.com.integradev.delphi.utils.PmdLevelUtils;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;

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
      extractSecurityStandards(pmdRule, sonarRule);
    }

    repository.done();
  }

  private static void extractSeverity(DelphiRule pmdRule, NewRule sonarRule) {
    String severity =
        Objects.requireNonNullElse(
            PmdLevelUtils.severityFromLevel(pmdRule.getPriority()), Severity.defaultSeverity());

    sonarRule.setSeverity(severity);
  }

  private static void extractDebtRemediationFunction(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty baseEffortProperty = pmdRule.getProperty(BASE_EFFORT.name());

    String error = String.format(UNDEFINED_BASE_EFFORT, pmdRule.getName());
    Preconditions.checkArgument(baseEffortProperty != null, error);

    sonarRule.setDebtRemediationFunction(
        sonarRule.debtRemediationFunctions().constantPerIssue(baseEffortProperty.getValue()));
  }

  private static void extractScope(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty scopeProperty = pmdRule.getProperty(SCOPE.name());
    if (scopeProperty != null) {
      sonarRule.setScope(RuleScope.valueOf(scopeProperty.getValue()));
    }
  }

  private static void extractTemplate(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty templateProperty = pmdRule.getProperty(TEMPLATE.name());
    if (templateProperty != null) {
      sonarRule.setTemplate(Boolean.parseBoolean(templateProperty.getValue()));
    }
  }

  private static void extractType(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty typeProperty = pmdRule.getProperty(TYPE.name());
    if (typeProperty != null) {
      sonarRule.setType(RuleType.valueOf(typeProperty.getValue()));
    }
  }

  private static void extractProperties(DelphiRule pmdRule, NewRule sonarRule) {
    List<DelphiRuleProperty> properties =
        pmdRule.getProperties().stream()
            .filter(not(DelphiRuleProperty::isBuiltinProperty))
            .collect(Collectors.toList());

    if (pmdRule.isCustomRule() || pmdRule.isTemplateRule()) {
      // The scope property should be exposed for user overrides via the SonarQube web interface,
      // but only on template rules or custom rules derived from templates.
      DelphiRuleProperty scopeProperty = pmdRule.getProperty(SCOPE.name());
      sonarRule
          .createParam(SCOPE.name())
          .setDefaultValue(scopeProperty == null ? SCOPE.defaultValue() : scopeProperty.getValue())
          .setType(RuleParamType.STRING)
          .setDescription(SCOPE.description());
    }

    for (DelphiRuleProperty property : properties) {
      sonarRule
          .createParam(property.getName())
          .setDefaultValue(property.getValue())
          .setType(isNumeric(property.getValue()) ? RuleParamType.INTEGER : RuleParamType.STRING)
          .setDescription(property.getName());
    }
  }

  private static void extractSecurityStandards(DelphiRule pmdRule, NewRule sonarRule) {
    DelphiRuleProperty cweProperty = pmdRule.getProperty(SECURITY_STANDARD_CWE.name());
    if (cweProperty != null) {
      Arrays.stream(cweProperty.getValue().split("\\|"))
          .map(Integer::parseInt)
          .forEach(sonarRule::addCwe);
    }

    DelphiRuleProperty owaspProperty = pmdRule.getProperty(SECURITY_STANDARD_OWASP.name());
    if (owaspProperty != null) {
      Arrays.stream(owaspProperty.getValue().split("\\|"))
          .map(OwaspTop10::valueOf)
          .forEach(sonarRule::addOwaspTop10);
    }
  }
}
