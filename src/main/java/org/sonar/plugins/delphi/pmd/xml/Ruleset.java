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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("ruleset")
/**
 * Class gathering a set of rules
 */
public class Ruleset {

  private String description;

  @XStreamImplicit
  private List<DelphiRule> delphiRules = new ArrayList<DelphiRule>();

  @XStreamOmitField
  @XStreamAlias(value = "exclude-pattern")
  private String excludePattern;

  @XStreamOmitField
  @XStreamAlias(value = "include-pattern")
  private String includePattern;

  /**
   * Default ctor
   */
  public Ruleset() {
  }

  /**
   * Ctor with description
   * 
   * @param description Rule set description
   */
  public Ruleset(String description) {
    this.description = description;
  }

  /**
   * @return List of DelphiLanguage Rules
   */
  public List<DelphiRule> getRules() {
    return delphiRules;
  }

  /**
   * Sets list of rule
   * 
   * @param delphiRules New rules
   */
  public void setRules(List<DelphiRule> delphiRules) {
    this.delphiRules = delphiRules;
  }

  /**
   * @return Rule set description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets rule set description
   * 
   * @param description new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Adds rule to set
   * 
   * @param delphiRule DelphiLanguage Rule to add
   */
  public void addRule(DelphiRule delphiRule) {
    delphiRules.add(delphiRule);
  }

  /**
   * @return exclude pattern
   */
  public String getExcludePattern() {
    return excludePattern;
  }

  /**
   * @return include pattern
   */
  public String getIncludePattern() {
    return includePattern;
  }
}
