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
package org.sonar.plugins.delphi.pmd.xml;


import com.thoughtworks.xstream.XStream;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.*;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;

import java.io.InputStream;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utilities for Delphi xml rules
 */
public final class DelphiRulesUtils {

  /**
   * Default c-tor
   */
  private DelphiRulesUtils() {
  }

  /**
   * Builds rule sets from xml file
   *
   * @param configuration Xml file configuration
   * @return Rule set
   */
  private static Ruleset buildRuleSetFromXml(String configuration) {
    XStream xstream = new XStream();
    xstream.setClassLoader(DelphiRulesUtils.class.getClassLoader());
    xstream.processAnnotations(Ruleset.class);
    xstream.processAnnotations(DelphiRule.class);
    xstream.processAnnotations(Property.class);
    xstream.aliasSystemAttribute("ref", "class");

    return (Ruleset) xstream.fromXML(configuration);
  }

  /**
   * Builds rule sets from Ruleset tree
   *
   * @param tree Rule set tree
   * @return Rule sets
   */
  private static String buildXmlFromRuleset(Ruleset tree) {
    XStream xstream = new XStream();
    xstream.setClassLoader(DelphiRulesUtils.class.getClassLoader());
    xstream.processAnnotations(Ruleset.class);
    xstream.processAnnotations(DelphiRule.class);
    xstream.processAnnotations(Property.class);
    return addHeaderToXml(xstream.toXML(tree));
  }

  private static String addHeaderToXml(String xmlModules) {
    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    return header + xmlModules;
  }

  /**
   * Gets a configuration from file
   *
   * @param path File path
   * @return Configuration string
   */
  private static String getConfigurationFromFile(String path) {
    String configuration;
    try {
      InputStream inputStream = Ruleset.class.getResourceAsStream(path);
      Scanner s = new Scanner(inputStream).useDelimiter("\\A");
      configuration = s.hasNext() ? s.next() : "";
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to read configuration file for the profile : " + path, e);
    }
    return configuration;


  }

  private static final String RESOURCE_PATH = "/org/sonar/plugins/delphi/pmd/";

  private static final String RULES_PATH = RESOURCE_PATH + "rules.xml";

  /**
   * @return rules xml file
   */
  public static List<Rule> getInitialReferential() {
    return parseReferential(RULES_PATH);
  }

  /**
   * Parses rules xml file
   *
   * @param path Xml file path
   * @return List of rules
   */
  private static List<Rule> parseReferential(String path) {
    Ruleset ruleset = DelphiRulesUtils.buildRuleSetFromXml(DelphiRulesUtils.getConfigurationFromFile(path));
    List<Rule> rulesRepository = new ArrayList<>();
    for (DelphiRule fRule : ruleset.getRules()) {
      rulesRepository.add(createRepositoryRule(fRule));
    }
    return rulesRepository;
  }

  /**
   * Imports configuration. Transform a list of Rule in ActiveRule.
   *
   * @param configuration rules configuration content
   * @param rulesRepository list os Rules
   * @param profile rules profile
   */
  public static void importConfiguration(String configuration, List<Rule> rulesRepository, RulesProfile profile)
  {
    Ruleset ruleset = DelphiRulesUtils.buildRuleSetFromXml(configuration);
    for (DelphiRule fRule : ruleset.getRules()) {
      createActiveRule(fRule, rulesRepository, profile);
    }
  }

  /**
   * Exports configuration
   * @param activeProfile The current active quality profile
   * @return The active rules as XML String
   */
  public static String exportConfiguration(RulesProfile activeProfile) {
    Ruleset tree = buildRulesetFromActiveProfile(activeProfile
      .getActiveRulesByRepository(DelphiPmdConstants.REPOSITORY_KEY));
    return buildXmlFromRuleset(tree);
  }

  private static Rule createRepositoryRule(DelphiRule fRule) {
    Rule rule = Rule.create(DelphiPmdConstants.REPOSITORY_KEY, fRule.getName(), fRule.getMessage()).setSeverity(
        severityFromLevel(fRule.getPriority()));

    rule.setDescription(fRule.getDescription());
    rule.setTags(fRule.getTags());
    rule.setConfigKey(fRule.getClazz());
    List<RuleParam> ruleParams = new ArrayList<>();
    if (fRule.getProperties() != null) {
      for (Property property : fRule.getProperties()) {
        RuleParam param = rule.createParameter()
          .setKey(property.getName())
          .setDescription(property.getName())
          .setType("s");

        if (isStringNumeric(property.getValue())) {
          param.setType("i");
        }

        param.setDefaultValue(property.getValue());

        ruleParams.add(param);
      }
    }
    rule.setParams(ruleParams);
    return rule;
  }

  private static void createActiveRule(DelphiRule fRule, List<Rule> rulesRepository, RulesProfile profile) {
    String name = fRule.getName();
    RulePriority fRulePriority = severityFromLevel(fRule.getPriority());

    for (Rule rule : rulesRepository) {
      if (rule.getKey().equals(name)) {
        RulePriority priority = fRulePriority != null ? fRulePriority : rule.getSeverity();
        ActiveRule activeRule = profile.activateRule(rule, priority);
        buildActiveRuleParams(fRule, rule, activeRule);
      }
    }
  }

  private static void buildActiveRuleParams(DelphiRule delphiRule, Rule repositoryRule, ActiveRule activeRule) {
    if (delphiRule.getProperties() != null) {
      for (Property property : delphiRule.getProperties()) {
        if (repositoryRule.getParams() != null) {
          for (RuleParam ruleParam : repositoryRule.getParams()) {
            if (ruleParam.getKey().equals(property.getName())) {
              activeRule.setParameter(ruleParam.getKey(), property.getValue());
            }
          }
        }
      }
    }
  }

  private static Ruleset buildRulesetFromActiveProfile(List<ActiveRule> activeRules) {
    Ruleset ruleset = new Ruleset();
    for (ActiveRule activeRule : activeRules) {
      if (activeRule.getRule().getRepositoryKey().equals(DelphiPmdConstants.REPOSITORY_KEY)) {
        String key = activeRule.getRule().getKey();
        String priority = severityToLevel(activeRule.getSeverity());
        List<Property> properties = new ArrayList<>();

        DelphiRule delphiRule = new DelphiRule(activeRule.getConfigKey(), priority);
        delphiRule.setName(key);
        for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
          properties.add(new Property(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue()));
        }
        delphiRule.setProperties(properties);
        delphiRule.setMessage(activeRule.getRule().getName());
        ruleset.addRule(delphiRule);
      }
    }
    return ruleset;
  }

  private static RulePriority severityFromLevel(String level) {
    try {
      int intLevel = 5 - Integer.valueOf(level);
      return RulePriority.valueOfInt(intLevel);
    } catch (Exception e) {
      return null;
    }
  }

  private static String severityToLevel(RulePriority priority) {
    return ((Integer)(5 - priority.ordinal())).toString();
  }

  private static boolean isStringNumeric(String str)
  {
    DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
    char localeMinusSign = currentLocaleSymbols.getMinusSign();

    if ( !Character.isDigit( str.charAt( 0 ) ) && str.charAt( 0 ) != localeMinusSign ) return false;

    boolean isDecimalSeparatorFound = false;
    char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

    for ( char c : str.substring( 1 ).toCharArray() )
    {
      if ( !Character.isDigit( c ) )
      {
        if ( c == localeDecimalSeparator && !isDecimalSeparatorFound )
        {
          isDecimalSeparatorFound = true;
          continue;
        }
        return false;
      }
    }
    return true;
  }

}
