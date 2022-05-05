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

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;

/** Factory class to create {@link DelphiRuleSet} out of XML. */
public class XmlRuleSetFactory implements RuleSetFactory, Closeable {
  private static final Logger LOG = Loggers.get(XmlRuleSetFactory.class);
  private static final String INVALID_INPUT = "The PMD configuration file is not valid";

  private final Reader source;
  private final ValidationMessages messages;

  public XmlRuleSetFactory(Reader source, @Nullable ValidationMessages messages) {
    this.source = source;
    this.messages = messages;
  }

  private List<Element> getChildren(Element parent, String child, @Nullable Namespace namespace) {
    if (namespace == null) {
      namespace = Namespace.NO_NAMESPACE;
    }

    List<?> children = parent.getChildren(child, namespace);
    return children.stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .collect(Collectors.toList());
  }

  private Element getChild(Element parent, String child, @Nullable Namespace namespace) {
    final List<Element> children = getChildren(parent, child, namespace);

    return (children != null && !children.isEmpty()) ? children.get(0) : null;
  }

  private void parsePmdProperties(Element eltRule, DelphiRule rule, @Nullable Namespace namespace) {
    for (Element eltProperties : getChildren(eltRule, "properties", namespace)) {
      for (Element eltProperty : getChildren(eltProperties, "property", namespace)) {
        String name = eltProperty.getAttributeValue("name");

        DelphiRuleProperty property = new DelphiRuleProperty(name);
        parsePmdPropertyValue(eltProperty, property, namespace);

        rule.addProperty(property);
      }
    }
  }

  private void parsePmdPropertyValue(
      Element eltProperty, DelphiRuleProperty property, @Nullable Namespace namespace) {
    if (isXpathProperty(property)) {
      Element xpathElement = getChild(eltProperty, "value", namespace);
      if (xpathElement != null) {
        property.setValue(xpathElement.getValue().trim());
      }
      return;
    }

    property.setValue(eltProperty.getAttributeValue("value"));
  }

  private boolean isXpathProperty(DelphiRuleProperty property) {
    return DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM.equals(property.getName())
        || DelphiPmdConstants.BUILTIN_XPATH_EXPRESSION_PARAM.equals(property.getName());
  }

  private void parsePmdPriority(Element eltRule, DelphiRule rule, @Nullable Namespace namespace) {
    for (Element eltPriority : getChildren(eltRule, "priority", namespace)) {
      rule.setPriority(Integer.valueOf(eltPriority.getValue()));
    }
  }

  private void parseDescription(Element eltRule, DelphiRule rule, @Nullable Namespace namespace) {
    for (Element eltDescription : getChildren(eltRule, "description", namespace)) {
      rule.setDescription(eltDescription.getText().trim());
    }
  }

  private void parseExample(Element eltRule, DelphiRule rule, @Nullable Namespace namespace) {
    for (Element eltExample : getChildren(eltRule, "example", namespace)) {
      rule.setExample(eltExample.getText().trim());
    }
  }

  /**
   * Closes all resources.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Override
  public void close() throws IOException {
    source.close();
  }

  /**
   * Parses the given Reader for PmdRuleSets.
   *
   * @return The extracted PmdRuleSet - empty in case of problems, never null.
   */
  @Override
  public DelphiRuleSet create() {
    final SAXBuilder builder = new SAXBuilder();
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    final Document dom;
    try {
      dom = builder.build(source);
    } catch (JDOMException | IOException e) {
      if (messages != null) {
        messages.addErrorText(INVALID_INPUT + ": " + e.getMessage());
      }
      LOG.debug(INVALID_INPUT, e);
      return new DelphiRuleSet();
    }

    final Element eltResultset = dom.getRootElement();
    final Namespace namespace = eltResultset.getNamespace();
    final DelphiRuleSet result = new DelphiRuleSet();

    final String name = eltResultset.getAttributeValue("name");
    final Element descriptionElement = getChild(eltResultset, "description", namespace);

    result.setName(name);

    if (descriptionElement != null) {
      result.setDescription(descriptionElement.getValue());
    }

    for (Element eltRule : getChildren(eltResultset, "rule", namespace)) {
      DelphiRule delphiRule = new DelphiRule(eltRule.getAttributeValue("class"));
      delphiRule.setName(eltRule.getAttributeValue("name"));
      delphiRule.setMessage(eltRule.getAttributeValue("message"));
      parsePmdPriority(eltRule, delphiRule, namespace);
      parsePmdProperties(eltRule, delphiRule, namespace);
      parseDescription(eltRule, delphiRule, namespace);
      parseExample(eltRule, delphiRule, namespace);
      result.addRule(delphiRule);
    }

    return result;
  }
}
