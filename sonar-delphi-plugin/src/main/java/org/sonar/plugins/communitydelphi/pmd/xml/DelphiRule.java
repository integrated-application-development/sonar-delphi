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
package org.sonar.plugins.communitydelphi.pmd.xml;

import static org.sonar.plugins.communitydelphi.pmd.DelphiPmdConstants.TEMPLATE;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** Delphi PMD rule loaded from xml file */
public final class DelphiRule {

  private Integer priority;
  private String name;
  private String templateName;
  private String message;
  private String clazz;
  private String description;
  private String example;
  private List<DelphiRuleProperty> properties = new ArrayList<>();

  public DelphiRule() {}

  /**
   * @param clazz The class which implements this rule
   */
  public DelphiRule(String clazz) {
    this(clazz, null);
  }

  /**
   * @param clazz The class which implements this rule
   * @param priority Rule priority
   */
  public DelphiRule(String clazz, @Nullable Integer priority) {
    this.clazz = clazz;
    this.priority = priority;
  }

  public List<DelphiRuleProperty> getProperties() {
    return properties;
  }

  public void setProperties(List<DelphiRuleProperty> properties) {
    this.properties = properties;
  }

  public DelphiRuleProperty getProperty(String propertyName) {
    for (DelphiRuleProperty prop : properties) {
      if (propertyName.equals(prop.getName())) {
        return prop;
      }
    }
    return null;
  }

  @Nullable
  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public void addProperty(DelphiRuleProperty property) {
    properties.add(property);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public void removeProperty(String propertyName) {
    DelphiRuleProperty prop = getProperty(propertyName);
    properties.remove(prop);
  }

  public boolean hasProperties() {
    return !properties.isEmpty();
  }

  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public void setExample(@Nullable String example) {
    this.example = example;
  }

  @Nullable
  String getDescription() {
    return description;
  }

  @Nullable
  public String getExample() {
    return example;
  }

  public String getHtmlDescription() {
    String desc = "";
    if (description != null) {
      desc += "<p>" + description + "</p>";
    }
    if (example != null) {
      desc += "<pre>" + example + "</pre>";
    }
    return desc;
  }

  public boolean isCustomRule() {
    DelphiRuleProperty templateProperty = getProperty(TEMPLATE.name());
    return templateProperty != null && Boolean.parseBoolean(templateProperty.getValue());
  }

  public boolean isTemplateRule() {
    return templateName != null;
  }
}
