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

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class LowerCaseReservedWordsRuleTest extends BasePmdRuleTest {

  @BeforeEach
  void setup() {
    DelphiRuleProperty property =
        Objects.requireNonNull(
            getRule(LowerCaseReservedWordsRule.class)
                .getProperty(LowerCaseReservedWordsRule.EXCLUDED_KEYWORDS.name()));
    property.setValue("string");
  }

  @Test
  void testUppercaseKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("Begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("END;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffset() + 2))
        .areExactly(1, ruleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffset() + 4));
  }

  @Test
  void testAsmBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Bar;")
            .appendImpl("asm")
            .appendImpl("  SHR   eax, 16")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("LowerCaseReservedWordsRule"));
  }

  @Test
  void testUppercaseExcludedKeywordShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo(")
            .appendImpl("  VarA: String;")
            .appendImpl("  VarB: string")
            .appendImpl("): STRING;")
            .appendImpl("begin")
            .appendImpl("  Result := VarA + VarB;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("LowerCaseReservedWordsRule"));
  }
}
