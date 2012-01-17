/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Delphi rule loaded from xml file
 */
@XStreamAlias("rule")
public class DelphiRule implements Comparable<String> {

  @XStreamAlias("class")
  @XStreamAsAttribute
  private String clazz;

  @XStreamAsAttribute
  private String message;

  @XStreamAsAttribute
  private String name;

  private String priority;

  private List<Property> properties;

  @XStreamOmitField
  private String description;

  @XStreamOmitField
  private String category;

  @XStreamOmitField
  private String exclude;

  @XStreamOmitField
  private String example;

  /**
   * Ctor
   * 
   * @param clazz
   *          Rule class
   */
  public DelphiRule(String clazz) {
    this(clazz, null);
  }

  /**
   * Ctor
   * 
   * @param clazz
   *          Rule class
   * @param priority
   *          Rule priority
   */
  public DelphiRule(String clazz, String priority) {
    this.clazz = clazz;
    this.priority = priority;
  }

  /**
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * sets name
   * 
   * @param name
   *          new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return rule class
   */
  public String getClazz() {
    return clazz;
  }

  /**
   * Sets properties
   * 
   * @param properties
   *          Properties to set
   */
  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  /**
   * @return Properties
   */
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Compares two class names lexicographically
   * 
   * @return the value 0 if the argument string is equal to this string; a value less than 0 if this string is lexicographically less than
   *         the string argument; and a value greater than 0 if this string is lexicographically greater than the string argument.
   */
  @Override
  public int compareTo(String o) {
    return o.compareTo(clazz);
  }

  /**
   * @return rule priority
   */
  public String getPriority() {
    return priority;
  }

  /**
   * sets priority
   * 
   * @param priority
   *          new priority
   */
  public void setPriority(String priority) {
    this.priority = priority;
  }

  /**
   * adds property
   */
  public void addProperty(Property property) {
    if (properties == null) {
      properties = new ArrayList<Property>();
    }
    properties.add(property);
  }

  /**
   * @return rule message
   */
  public String getMessage() {
    return message;
  }

  /**
   * sets message
   * 
   * @param message
   *          New rule message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return rule description
   */
  public String getDescription() {
    String desc = "";
    if (description != null) {
      desc += "<p>" + description + "</p>";
    }
    if (example != null) {
      desc += "<pre>" + example + "</pre>";
    }
    return desc;
  }

  /**
   * @return rule category
   */
  public String getCategory() {
    return category;
  }

  /**
   * @return exclude string
   */
  public String getExclude() {
    return exclude;
  }

}
