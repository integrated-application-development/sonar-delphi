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
import org.junit.jupiter.api.Test;

class TooManyVariablesCheckTest extends CheckTest {

  @Test
  void testOneVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TooManyVariablesRule"));
  }

  @Test
  void testSingleVariableDeclarationsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  MyVar1: Boolean;")
            .appendImpl("  MyVar2: Boolean;")
            .appendImpl("  MyVar3: Boolean;")
            .appendImpl("  MyVar4: Boolean;")
            .appendImpl("  MyVar5: Boolean;")
            .appendImpl("  MyVar6: Boolean;")
            .appendImpl("  MyVar7: Boolean;")
            .appendImpl("  MyVar8: Boolean;")
            .appendImpl("  MyVar9: Boolean;")
            .appendImpl("  MyVar10: Boolean;")
            .appendImpl("  MyVar11: Boolean;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TooManyVariablesRule", builder.getOffset() + 1));
  }

  @Test
  void testMultiVariableDeclarationsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  MyVar1,")
            .appendImpl("  MyVar2,")
            .appendImpl("  MyVar3,")
            .appendImpl("  MyVar4,")
            .appendImpl("  MyVar5,")
            .appendImpl("  MyVar6,")
            .appendImpl("  MyVar7,")
            .appendImpl("  MyVar8,")
            .appendImpl("  MyVar9,")
            .appendImpl("  MyVar10,")
            .appendImpl("  MyVar11: Boolean;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TooManyVariablesRule", builder.getOffset() + 1));
  }
}
