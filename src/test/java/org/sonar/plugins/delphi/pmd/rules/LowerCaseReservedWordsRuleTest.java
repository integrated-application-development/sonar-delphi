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

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class LowerCaseReservedWordsRuleTest extends BasePmdRuleTest {

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
}
