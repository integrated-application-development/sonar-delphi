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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForbiddenConstantRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String FORBIDDEN_CONSTANT = "C_Foo";

  @BeforeEach
  void setup() {
    au.com.integradev.delphi.pmd.xml.DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(
            ForbiddenConstantRule.BLACKLISTED_CONSTANTS.name(), FORBIDDEN_CONSTANT);

    DelphiRuleProperty unitName =
        new DelphiRuleProperty(ForbiddenConstantRule.UNIT_NAME.name(), UNIT_NAME);

    rule.setName("ForbiddenConstantRuleTest");
    rule.setTemplateName("ForbiddenConstantRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.addProperty(unitName);
    rule.setClazz("au.com.integradev.delphi.pmd.rules.ForbiddenConstantRule");

    addRule(rule);
  }

  @Test
  void testForbiddenConstantUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("const")
            .appendDecl("  C_Foo = 'Foo';")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: String;")
            .appendImpl("begin")
            .appendImpl("  Foo := C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenConstantRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testQualifiedForbiddenConstantUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("const")
            .appendDecl("  C_Foo = 'Foo';")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: String;")
            .appendImpl("begin")
            .appendImpl("  Foo := " + UNIT_NAME + ".C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenConstantRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testLocalConstantUsageShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("const")
            .appendDecl("  C_Foo = 'Foo';")
            .appendImpl("procedure Test;")
            .appendImpl("const")
            .appendImpl("  C_Foo = 'Foo';")
            .appendImpl("var")
            .appendImpl("  Foo: String;")
            .appendImpl("begin")
            .appendImpl("  Foo := C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenConstantRuleTest"));
  }
}
