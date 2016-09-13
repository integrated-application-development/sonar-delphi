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

import org.sonar.api.rules.RuleParam;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRulesUtils;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import java.util.List;

/**
 * Delphi rules definition
 */
public class DelphiPmdRuleDefinition implements RulesDefinition {

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(DelphiPmdConstants.REPOSITORY_KEY, DelphiLanguage.KEY)
      .setName(DelphiPmdConstants.REPOSITORY_NAME);

    List<org.sonar.api.rules.Rule> rules = DelphiRulesUtils.getInitialReferential();

    // TODO Review
    // https://github.com/SonarCommunity/sonar-pmd/blob/master/src/main/java/org/sonar/plugins/pmd/PmdRulesDefinition.java
    for (org.sonar.api.rules.Rule rule : rules) {
      NewRule newRule = repository.createRule(rule.getKey())
        .setName(rule.getName())
        .setHtmlDescription(rule.getDescription())
        .setInternalKey(rule.getConfigKey())
        .setSeverity(rule.getSeverity().name());
      for (RuleParam param : rule.getParams()) {
        newRule.createParam(param.getKey())
          .setDefaultValue(param.getDefaultValue())
          .setType(RuleParamType.parse(param.getType()))
          .setDescription(param.getDescription());
      }
    }

    SqaleXmlLoader.load(repository, "/org/sonar/plugins/delphi/sqale/delphi-model.xml");

    repository.done();

  }

}
