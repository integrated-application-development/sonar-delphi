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
package org.sonar.plugins.communitydelphi.pmd.rules;

import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.pmd.xml.DelphiRule;
import org.sonar.plugins.communitydelphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class ForbiddenFieldRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String DECLARING_TYPE = "TestUnit.TFoo";
  private static final String FORBIDDEN_FIELD = "Bar";

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(ForbiddenFieldRule.BLACKLISTED_FIELDS.name(), FORBIDDEN_FIELD);

    DelphiRuleProperty declaringType =
        new DelphiRuleProperty(ForbiddenFieldRule.DECLARING_TYPE.name(), DECLARING_TYPE);

    rule.setName("ForbiddenFieldRuleTest");
    rule.setTemplateName("ForbiddenFieldRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.addProperty(declaringType);
    rule.setClazz("org.sonar.plugins.communitydelphi.pmd.rules.ForbiddenFieldRule");

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
