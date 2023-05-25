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

class ForbiddenFieldCheckTest extends CheckTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String DECLARING_TYPE = "TestUnit.TFoo";
  private static final String FORBIDDEN_FIELD = "Bar";

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(ForbiddenFieldCheck.BLACKLISTED_FIELDS.name(), FORBIDDEN_FIELD);

    DelphiRuleProperty declaringType =
        new DelphiRuleProperty(ForbiddenFieldCheck.DECLARING_TYPE.name(), DECLARING_TYPE);

    rule.setName("ForbiddenFieldRuleTest");
    rule.setTemplateName("ForbiddenFieldRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.addProperty(declaringType);
    rule.setClazz("au.com.integradev.delphi.pmd.rules.ForbiddenFieldRule");

    addRule(rule);
  }

  @Test
  void testForbiddenFieldUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = record")
            .appendDecl("    Bar: String;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Baz: String;")
            .appendImpl("begin")
            .appendImpl("  Baz := Foo.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ForbiddenFieldRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testForbiddenFieldNameDeclaredByDifferentTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBoop = record")
            .appendDecl("    Bar: String;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Boop: TBoop);")
            .appendImpl("var")
            .appendImpl("  Baz: String;")
            .appendImpl("begin")
            .appendImpl("  Baz := Boop.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenFieldRuleTest"));
  }
}
