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
package org.sonar.plugins.delphi.pmd.xml;

import static org.sonar.plugins.delphi.pmd.rules.DelphiRule.BASE_EFFORT;
import static org.sonar.plugins.delphi.pmd.rules.DelphiRule.SCOPE;
import static org.sonar.plugins.delphi.pmd.rules.DelphiRule.TEMPLATE;
import static org.sonar.plugins.delphi.pmd.rules.DelphiRule.TYPE;
import static org.sonar.plugins.delphi.pmd.rules.XPathBuiltinRule.BUILTIN_XPATH;

import java.util.Set;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;

public class DelphiRuleProperty {

  private final String name;
  private String value;

  private static final Set<PropertyDescriptor> BUILTIN_PROPERTIES =
      Set.of(BASE_EFFORT, SCOPE, TEMPLATE, TYPE, BUILTIN_XPATH);

  public DelphiRuleProperty(String name) {
    this(name, null);
  }

  public DelphiRuleProperty(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean isCdataValue() {
    return name.equals(DelphiPmdConstants.TEMPLATE_XPATH_EXPRESSION_PARAM)
        || name.equals(DelphiPmdConstants.BUILTIN_XPATH_EXPRESSION_PARAM);
  }

  public boolean isBuiltinProperty() {
    return BUILTIN_PROPERTIES.stream().anyMatch(builtin -> builtin.name().equals(name));
  }
}
