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

class ForbiddenMethodCheckTest extends CheckTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String FORBIDDEN_METHOD = "TestUnit.TFoo.Bar";

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(ForbiddenMethodCheck.BLACKLISTED_METHODS.name(), FORBIDDEN_METHOD);

    rule.setName("ForbiddenMethodRuleTest");
    rule.setTemplateName("ForbiddenMethodRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.setClazz("au.com.integradev.delphi.pmd.rules.ForbiddenMethodRule");

    addRule(rule);
  }

  @Test
  void testForbiddenMethodUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo.Create;")
            .appendImpl("  Foo.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ForbiddenMethodRuleTest", builder.getOffset() + 6));
  }

  @Test
  void testNotUsingForbiddenMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("    procedure Baz;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo.Create;")
            .appendImpl("  Foo.Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenMethodRuleTest"));
  }

  @Test
  void testMethodImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar; virtual;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Bar;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenMethodRuleTest"));
  }
}
