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

class InheritedTypeNameRuleTest extends BasePmdRuleTest {

  private DelphiRuleProperty nameRegex;
  private DelphiRuleProperty parentRegex;

  @BeforeEach
  void setup() {
    nameRegex = new DelphiRuleProperty(InheritedTypeNameRule.NAME_REGEX.name(), ".*_Child");
    parentRegex = new DelphiRuleProperty(InheritedTypeNameRule.PARENT_REGEX.name(), ".*_Parent");

    DelphiRule rule = new DelphiRule();
    rule.setName("TestInheritedNameRule");
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.InheritedTypeNameRule");
    rule.setPriority(5);
    rule.addProperty(nameRegex);
    rule.addProperty(parentRegex);

    addRule(rule);
  }

  @Test
  void testCompliesWithNamingConventionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType_Child = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TestInheritedNameRule"));
  }

  @Test
  void testFailsNamingConventionShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("TestInheritedNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testFailsNamingConventionWithMultipleParentsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(IType, TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("TestInheritedNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testDoesNotInheritFromExpectedTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TSomeOtherType)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TestInheritedNameRule"));
  }

  @Test
  void testDoesNotInheritFromAnyTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TObject)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TestInheritedNameRule"));
  }

  @Test
  void testBadNameRegexShouldNotAddIssue() {
    nameRegex.setValue("*");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TestInheritedNameRule"));
  }

  @Test
  void testBadParentRegexShouldNotAddIssue() {
    parentRegex.setValue("*");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TType = class(TType_Parent)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TestInheritedNameRule"));
  }
}
