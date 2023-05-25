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
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForbiddenEnumValueCheckTest extends CheckTest {
  private static final String UNIT_NAME = "Foo";
  private static final String ENUM_NAME = "Foo.TBar";
  private static final String FORBIDDEN_VALUE = "Baz";

  @BeforeEach
  void setup() {
    au.com.integradev.delphi.pmd.xml.DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(
            ForbiddenEnumValueCheck.BLACKLISTED_ENUM_VALUES.name(), FORBIDDEN_VALUE);

    DelphiRuleProperty enumName =
        new DelphiRuleProperty(ForbiddenEnumValueCheck.ENUM_NAME.name(), ENUM_NAME);

    rule.setName("ForbiddenEnumValueRuleTest");
    rule.setTemplateName("ForbiddenEnumValueRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.addProperty(enumName);
    rule.setClazz("au.com.integradev.delphi.pmd.rules.ForbiddenEnumValueRule");

    addRule(rule);
  }

  @Test
  void testForbiddenEnumValueShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenEnumValueRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testQualifiedForbiddenEnumValueShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := TBar.Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenEnumValueRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testAllowedEnumValueShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := Beep;")
            .appendImpl("  Bar := Boop;")
            .appendImpl("  Bar := Blop;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenEnumValueRuleTest"));
  }
}
