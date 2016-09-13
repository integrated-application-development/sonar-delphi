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
  public static Ruleset buildRuleSetFromXml(String configuration) {
    XStream xstream = new XStream();
    xstream.setClassLoader(DelphiRulesUtils.class.getClassLoader());
    xstream.processAnnotations(Ruleset.class);
    xstream.processAnnotations(DelphiRule.class);
    xstream.processAnnotations(Property.class);
    xstream.aliasSystemAttribute("ref", "class");

    return (Ruleset) xstream.fromXML(configuration);
  }

  /**
   * Builds rule sets from ruleset tree
   *
   * @param tree Rule set tree
   * @return Rule sets
   */
  public static String buildXmlFromRuleset(Ruleset tree) {
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
   * @return COnfiguration string
   */
  public static String getConfigurationFromFile(String path) {
    String configuration = null;
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

  public static final String RULES_PATH = RESOURCE_PATH + "rules.xml";

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
  public static List<Rule> parseReferential(String path) {
    Ruleset ruleset = DelphiRulesUtils.buildRuleSetFromXml(DelphiRulesUtils.getConfigurationFromFile(path));
    List<Rule> rulesRepository = new ArrayList<Rule>();
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
   * @return a list of ActiveRules
   */
  public static List<ActiveRule> importConfiguration(String configuration, List<Rule> rulesRepository) {
    Ruleset ruleset = DelphiRulesUtils.buildRuleSetFromXml(configuration);
    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    for (DelphiRule fRule : ruleset.getRules()) {
      ActiveRule activeRule = createActiveRule(fRule, rulesRepository);
      if (activeRule != null) {
        activeRules.add(activeRule);
      }
    }
    return activeRules;
  }

  /**
   * Exports configuration
   * @param activeProfile The currrent active quality profile
   * @return The active rules as XML String
   */
  public static String exportConfiguration(RulesProfile activeProfile) {
    Ruleset tree = buildRulesetFromActiveProfile(activeProfile
      .getActiveRulesByRepository(DelphiPmdConstants.REPOSITORY_KEY));
    return DelphiRulesUtils.buildXmlFromRuleset(tree);
  }

  private static Rule createRepositoryRule(DelphiRule fRule) {
    RulePriority priority = severityFromLevel(fRule.getPriority());

    Rule rule = Rule.create(DelphiPmdConstants.REPOSITORY_KEY, fRule.getName(), fRule.getMessage()).setSeverity(
      priority);

    rule.setDescription(fRule.getDescription());
    rule.setTags(fRule.getTags());
    rule.setConfigKey(fRule.getClazz());
    List<RuleParam> ruleParams = new ArrayList<RuleParam>();
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

  private static ActiveRule createActiveRule(DelphiRule fRule, List<Rule> rulesRepository) {
    String name = fRule.getName();
    RulePriority fRulePriority = severityFromLevel(fRule.getPriority());

    for (Rule rule : rulesRepository) {
      if (rule.getKey().equals(name)) {
        RulePriority priority = fRulePriority != null ? fRulePriority : rule.getSeverity();
        ActiveRule activeRule = new ActiveRule(null, rule, priority);
        activeRule.setActiveRuleParams(buildActiveRuleParams(fRule, rule, activeRule));
        return activeRule;
      }
    }
    return null;
  }

  static List<ActiveRuleParam>
    buildActiveRuleParams(DelphiRule delphiRule, Rule repositoryRule, ActiveRule activeRule) {
    List<ActiveRuleParam> activeRuleParams = new ArrayList<ActiveRuleParam>();
    if (delphiRule.getProperties() != null) {
      for (Property property : delphiRule.getProperties()) {
        if (repositoryRule.getParams() != null) {
          for (RuleParam ruleParam : repositoryRule.getParams()) {
            if (ruleParam.getKey().equals(property.getName())) {
              activeRuleParams.add(new ActiveRuleParam(activeRule, ruleParam, property.getValue()));
            }
          }
        }
      }
    }
    return activeRuleParams;
  }

  static Ruleset buildRulesetFromActiveProfile(List<ActiveRule> activeRules) {
    Ruleset ruleset = new Ruleset();
    for (ActiveRule activeRule : activeRules) {
      if (activeRule.getRule().getRepositoryKey().equals(DelphiPmdConstants.REPOSITORY_KEY)) {
        String key = activeRule.getRule().getKey();
        String priority = severityToLevel(activeRule.getPriority());
        List<Property> properties = new ArrayList<Property>();

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
    if ("1".equals(level)) {
      return RulePriority.BLOCKER;
    }
    if ("2".equals(level)) {
      return RulePriority.CRITICAL;
    }
    if ("3".equals(level)) {
      return RulePriority.MAJOR;
    }
    if ("4".equals(level)) {
      return RulePriority.MINOR;
    }
    if ("5".equals(level)) {
      return RulePriority.INFO;
    }
    return null;
  }

  private static String severityToLevel(RulePriority priority) {
    if (priority.equals(RulePriority.BLOCKER)) {
      return "1";
    }
    if (priority.equals(RulePriority.CRITICAL)) {
      return "2";
    }
    if (priority.equals(RulePriority.MAJOR)) {
      return "3";
    }
    if (priority.equals(RulePriority.MINOR)) {
      return "4";
    }
    if (priority.equals(RulePriority.INFO)) {
      return "5";
    }
    throw new IllegalArgumentException("Level not supported: " + priority);
  }

  public static boolean isStringNumeric( String str )
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
