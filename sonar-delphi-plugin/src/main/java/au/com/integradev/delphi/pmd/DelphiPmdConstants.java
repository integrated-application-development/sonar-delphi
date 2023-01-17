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
package au.com.integradev.delphi.pmd;

import au.com.integradev.delphi.pmd.rules.XPathBuiltinRule;
import au.com.integradev.delphi.pmd.rules.XPathTemplateRule;
import java.util.List;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.constraints.NumericConstraints;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.rules.RuleType;

/** Constants for Delphi pmd */
public final class DelphiPmdConstants {

  public static final String REPOSITORY_KEY = "delph";
  public static final String REPOSITORY_NAME = "Delphi PMD";

  public static final String RULES_XML = "/au/com/integradev/delphi/pmd/rules.xml";

  public static final String TEMPLATE_XPATH_CLASS = XPathTemplateRule.class.getName();
  public static final String TEMPLATE_XPATH_EXPRESSION_PARAM = XPathRule.XPATH_DESCRIPTOR.name();
  public static final String BUILTIN_XPATH_EXPRESSION_PARAM = XPathBuiltinRule.BUILTIN_XPATH.name();

  public static final String SUPPRESSION_TAG = "NOSONAR";

  public static final PropertyDescriptor<Integer> LIMIT =
      PropertyFactory.intProperty("limit")
          .desc("The max limit.")
          .require(NumericConstraints.positive())
          .defaultValue(1)
          .build();

  public static final PropertyDescriptor<String> BASE_EFFORT =
      PropertyFactory.stringProperty("baseEffort")
          .desc("The base effort to correct")
          .defaultValue("")
          .build();

  public static final PropertyDescriptor<String> SCOPE =
      PropertyFactory.stringProperty("scope")
          .desc("The type of code this rule should apply to. Options are: 'ALL', 'MAIN', 'TEST'")
          .defaultValue(RuleScope.ALL.name())
          .build();

  public static final PropertyDescriptor<Boolean> TEMPLATE =
      PropertyFactory.booleanProperty("template")
          .desc("Whether the rule is a template")
          .defaultValue(false)
          .build();

  public static final PropertyDescriptor<String> TYPE =
      PropertyFactory.stringProperty("type")
          .desc("Rule type. Options are: 'CODE_SMELL', 'BUG', 'VULNERABILITY', 'SECURITY_HOTSPOT'")
          .defaultValue(RuleType.CODE_SMELL.name())
          .build();

  public static final PropertyDescriptor<List<Integer>> SECURITY_STANDARD_CWE =
      PropertyFactory.intListProperty("cwe")
          .desc("A list of Common Weakness Enumeration (CWE) IDs")
          .emptyDefaultValue()
          .build();

  public static final PropertyDescriptor<List<String>> SECURITY_STANDARD_OWASP =
      PropertyFactory.stringListProperty("owasp")
          .desc("A list of OWASP Top 10 identifiers ('A1' - 'A10')")
          .emptyDefaultValue()
          .build();

  private DelphiPmdConstants() {
    // Constant class
  }
}
