/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.pmd.profile;

import com.google.errorprone.annotations.FormatMethod;
import java.io.Reader;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;
import org.sonar.plugins.delphi.utils.PmdLevelUtils;

/** imports Delphi rules profile from Sonar */
@ServerSide
public class DelphiPmdProfileImporter extends ProfileImporter {
  private static final Logger LOG = Loggers.get(DelphiPmdProfileImporter.class);
  private final RuleFinder ruleFinder;
  private ValidationMessages messages;

  public DelphiPmdProfileImporter(RuleFinder ruleFinder) {
    super(DelphiPmdConstants.REPOSITORY_KEY, DelphiPmdConstants.REPOSITORY_NAME);
    setSupportedLanguages(DelphiLanguage.KEY);
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile importProfile(Reader pmdConfigurationFile, ValidationMessages messages) {
    this.messages = messages;
    DelphiRuleSet ruleSet = DelphiRuleSetHelper.createFrom(pmdConfigurationFile, messages);
    RulesProfile profile = RulesProfile.create();

    for (DelphiRule delphiRule : ruleSet.getRules()) {
      createActiveRule(delphiRule, profile);
    }
    return profile;
  }

  private void createActiveRule(DelphiRule delphiRule, RulesProfile profile) {
    String ruleName = delphiRule.getName();

    if (delphiRule.getClazz() == null) {
      addWarning("A PMD rule without 'class' attribute can't be imported. See '%s'", ruleName);
      return;
    }

    if (delphiRule.getClazz().equals(DelphiPmdConstants.TEMPLATE_XPATH_CLASS)) {
      addWarning(
          "XPath rule %s can't be imported automatically."
              + " The rule must be created manually through the SonarQube web interface.",
          ruleName);
      return;
    }

    RuleQuery query =
        RuleQuery.create().withRepositoryKey(DelphiPmdConstants.REPOSITORY_KEY).withKey(ruleName);

    Rule rule = ruleFinder.find(query);

    if (rule == null) {
      addWarning("Unable to import unknown PMD rule '%s'", ruleName);
      return;
    }

    Integer pmdLevel = delphiRule.getPriority();
    ActiveRule activeRule = profile.activateRule(rule, PmdLevelUtils.fromLevel(pmdLevel));
    setParameters(activeRule, delphiRule, rule);
  }

  private void setParameters(ActiveRule activeRule, DelphiRule delphiRule, Rule sonarRule) {
    List<DelphiRuleProperty> properties =
        delphiRule.getProperties().stream()
            .filter(Predicate.not(DelphiRuleProperty::isBuiltinProperty))
            .collect(Collectors.toList());

    for (DelphiRuleProperty property : properties) {
      if (sonarRule.getParam(property.getName()) == null) {
        addWarning(
            "The property '%s' is not supported in the PMD rule: %s",
            property.getName(), sonarRule.getName());
        continue;
      }

      activeRule.setParameter(property.getName(), property.getValue());
    }
  }

  @FormatMethod
  private void addWarning(String warning, Object... args) {
    if (messages != null) {
      messages.addWarningText(String.format(warning, args));
    }
    LOG.warn(warning);
  }
}
