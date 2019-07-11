package org.sonar.plugins.delphi.pmd.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.PmdLevelUtils;

/*
 * Class containing set of PMD rules
 */
public class DelphiRuleSet {

  private String name;
  private String description;
  private final List<DelphiRule> pmdRules = new ArrayList<>();
  private final List<Rule> sonarRules = new ArrayList<>();

  public List<DelphiRule> getPmdRules() {
    return pmdRules;
  }

  public List<Rule> getSonarRules() {
    return sonarRules;
  }

  public void addRule(DelphiRule pmdRule) {
    pmdRules.add(pmdRule);
    sonarRules.add(makeSonarRule(pmdRule));
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Serializes the PMD RuleSet into an XML document.
   *
   * @param destination The writer to which the XML document shall be written.
   */
  public void writeTo(Writer destination) {
    Element eltRuleset = new Element("ruleset");
    addAttribute(eltRuleset, "name", name);
    addChild(eltRuleset, "description", description);
    for (DelphiRule delphiRule : pmdRules) {
      Element eltRule = new Element("rule");
      addAttribute(eltRule, "class", delphiRule.getClazz());
      addAttribute(eltRule, "message", delphiRule.getMessage());
      addAttribute(eltRule, "name", delphiRule.getName());
      addAttribute(eltRule, "language", DelphiLanguage.KEY);
      addChild(eltRule, "priority", String.valueOf(delphiRule.getPriority()));
      addChild(eltRule, "description", delphiRule.getDescription());

      if (delphiRule.hasProperties()) {
        Element ruleProperties = processRuleProperties(delphiRule);
        if (ruleProperties.getContentSize() > 0) {
          eltRule.addContent(ruleProperties);
        }
      }

      addChild(eltRule, "example", new CDATA(delphiRule.getExample()));

      eltRuleset.addContent(eltRule);
    }
    XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
    try {
      serializer.output(new Document(eltRuleset), destination);
    } catch (IOException e) {
      throw new IllegalStateException("An exception occurred while serializing DelphiRuleSet.", e);
    }
  }

  private void addChild(Element elt, String name, @Nullable String text) {
    if (text != null) {
      elt.addContent(new Element(name).setText(text));
    }
  }

  private void addChild(Element elt, String name, Content content) {
    if (!content.getValue().isEmpty()) {
      elt.addContent(new Element(name).setContent(content));
    }
  }

  private void addAttribute(Element elt, String name, @Nullable String value) {
    if (value != null) {
      elt.setAttribute(name, value);
    }
  }

  private Element processRuleProperties(DelphiRule delphiRule) {
    Element eltProperties = new Element("properties");
    for (DelphiRuleProperty prop : delphiRule.getProperties()) {
      if (isPropertyValueNotEmpty(prop)) {
        Element eltProperty = new Element("property");
        eltProperty.setAttribute("name", prop.getName());
        if (prop.isCdataValue()) {
          Element eltValue = new Element("value");
          eltValue.addContent(new CDATA(prop.getCdataValue()));
          eltProperty.addContent(eltValue);
        } else {
          eltProperty.setAttribute("value", prop.getValue());
        }
        eltProperties.addContent(eltProperty);
      }
    }
    return eltProperties;
  }

  private boolean isPropertyValueNotEmpty(DelphiRuleProperty prop) {
    if (prop.isCdataValue()) {
      return StringUtils.isNotEmpty(prop.getCdataValue());
    }
    return StringUtils.isNotEmpty(prop.getValue());
  }

  private static Rule makeSonarRule(DelphiRule fRule) {
    Rule rule =
        Rule.create(DelphiPmdConstants.REPOSITORY_KEY, fRule.getName(), fRule.getMessage())
            .setLanguage(DelphiLanguage.KEY)
            .setSeverity(PmdLevelUtils.fromLevel(fRule.getPriority()))
            .setDescription(fRule.getHtmlDescription())
            .setConfigKey(fRule.getClazz());

    List<RuleParam> ruleParams = new ArrayList<>();
    if (fRule.getProperties() != null) {
      for (DelphiRuleProperty property : fRule.getProperties()) {
        RuleParam param =
            rule.createParameter()
                .setKey(property.getName())
                .setDescription(property.getName())
                .setType("s");

        if (org.apache.commons.lang.StringUtils.isNumeric(property.getValue())) {
          param.setType("i");
        }

        param.setDefaultValue(property.getValue());

        ruleParams.add(param);
      }
    }
    rule.setParams(ruleParams);
    return rule;
  }
}
