/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class StringLiteralRegexRuleTest extends BasePmdRuleTest {
  private static final String IDREF_PATTERN = ".*ID(\\d|[A-Z]){8}.*";
  private DelphiRuleProperty regexProperty;

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    regexProperty = new DelphiRuleProperty(StringLiteralRegexRule.REGEX.name(), IDREF_PATTERN);

    rule.setName("IDRefStringLiteralRule");
    rule.setTemplateName("StringLiteralRegexRule");
    rule.setPriority(5);
    rule.addProperty(regexProperty);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.StringLiteralRegexRule");

    addRule(rule);
  }

  @Test
  void testValidStringShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_MyConstant = 'Wow, a constant!';");

    execute(builder);

    assertIssues().areNot(ruleKey("IDRefStringLiteralRule"));
  }

  @Test
  void testMatchingStringShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_HardcodedIDRef = 'ID1234X6U8';");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("IDRefStringLiteralRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testInvalidRegexShouldNotAddIssue() {
    regexProperty.setValue("*");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_HardcodedIDRef = 'ID12345678';");

    execute(builder);

    assertIssues().areNot(ruleKey("IDRefStringLiteralRule"));
  }

  @Test
  void testMatchingStringInTestMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(TEST_UNIT)
            .appendDecl("type")
            .appendDecl("  TTestSuite = class(TObject)")
            .appendDecl("    procedure TestWithHardcodedIDRef;")
            .appendDecl("  end;")
            .appendImpl("procedure TTestSuite.TestWithHardcodedIDRef;")
            .appendImpl("const")
            .appendImpl("  C_HardcodedIDRef = 'ID1234X6U8';")
            .appendImpl("begin")
            .appendImpl("  Assert(Assigned(C_HardcodedIDRef), 'The sky is falling!');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("IDRefStringLiteralRule"));
  }

  @Test
  void testMatchingStringInTestClassDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(TEST_UNIT)
            .appendImpl("type")
            .appendImpl("  TTestSuite = class(TObject)")
            .appendImpl("  private const")
            .appendImpl("    C_HardcodedIDRef = 'ID1234X6U8';")
            .appendImpl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("IDRefStringLiteralRule"));
  }
}
