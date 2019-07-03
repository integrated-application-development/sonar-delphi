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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;

/**
 * Delphi PMD rule loaded from xml file
 */
public final class DelphiRule {

  private String ref;
  private Integer priority;
  private String name;
  private String message;
  private String clazz;
  private String description;
  private String example;
  private List<DelphiRuleProperty> properties;

  public DelphiRule() {
  }

  /**
   * @param ref Rule ref
   */
  public DelphiRule(String ref) {
    this(ref, null);
  }

  /**
   * @param clazz The class which implements this rule
   * @param priority Rule priority
   */
  public DelphiRule(String clazz, @Nullable Integer priority) {
    this.clazz = clazz;
    this.priority = priority;
    properties = new ArrayList<>();
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

  public String getFullDescription() {
    String desc = "";
    if (description != null) {
      desc += "<p>" + description + "</p>";
    }
    if (example != null) {
      desc += "<pre>" + example + "</pre>";
    }
    return desc;
  }

  public void processXpath(String sonarRuleKey) {
    if (DelphiPmdConstants.XPATH_CLASS.equals(ref)) {
      ref = null;
      DelphiRuleProperty xpathMessage = getProperty(DelphiPmdConstants.XPATH_MESSAGE_PARAM);
      if (xpathMessage == null) {
        throw new IllegalArgumentException("Property '" + DelphiPmdConstants.XPATH_MESSAGE_PARAM +
            "' should be set for PMD rule " + sonarRuleKey);
      }

      message = xpathMessage.getValue();
      removeProperty(DelphiPmdConstants.XPATH_MESSAGE_PARAM);
      DelphiRuleProperty xpathExp = getProperty(DelphiPmdConstants.XPATH_EXPRESSION_PARAM);

      if (xpathExp == null) {
        throw new IllegalArgumentException("Property '" + DelphiPmdConstants.XPATH_EXPRESSION_PARAM
            + "' should be set for PMD rule " + sonarRuleKey);
      }

      xpathExp.setCdataValue(xpathExp.getValue());
      clazz = DelphiPmdConstants.XPATH_CLASS;
      name = sonarRuleKey;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DelphiRule that = (DelphiRule) o;
    return clazz.equals(that.clazz);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz);
  }
}
