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

import static au.com.integradev.delphi.checks.ForbiddenIdentifierCheck.BLACKLISTED_NAMES;
import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.pmd.xml.DelphiRule;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForbiddenIdentifierCheckTest extends CheckTest {
  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist = new DelphiRuleProperty(BLACKLISTED_NAMES.name(), "BadName");

    rule.setName("ForbiddenNameRuleTest");
    rule.setTemplateName("ForbiddenIdentifierRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.setClazz("au.com.integradev.delphi.pmd.rules.ForbiddenIdentifierRule");

    addRule(rule);
  }

  @Test
  void testAllowedIdentifierShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  GoodName: TObject;");
    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenNameRuleTest"));
  }

  @Test
  void testForbiddenIdentifierShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  BadName: TObject;");
    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenNameRuleTest", builder.getOffsetDecl() + 2));
  }
}
