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

class UnusedConstantsRuleTest extends BasePmdRuleTest {
  @Test
  void testUsedInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("const")
            .appendImpl("  C_Foo = 0;")
            .appendImpl("begin")
            .appendImpl("  var Foo := C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedConstantsRule"));
  }

  @Test
  void testUnusedInMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("const")
            .appendImpl("  C_Foo = 0;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedConstantsRule", builder.getOffset() + 3));
  }

  @Test
  void testUsedInlineConstantShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  const C_Foo = 0;")
            .appendImpl("  var Foo := C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedConstantsRule"));
  }

  @Test
  void testUnusedInlineConstantShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  const C_Foo = 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedConstantsRule", builder.getOffset() + 3));
  }

  @Test
  void testUsedGlobalConstantShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_Foo = 0;")
            .appendDecl("  C_Bar = C_Foo;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("UnusedConstantsRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUnusedGlobalConstantShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("const").appendDecl("  C_Foo = 0;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedConstantsRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUsedInArrayIndexTypesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_Foo = 0;")
            .appendDecl("var")
            .appendDecl("  FooArray: array[0..C_Foo] of Integer;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("UnusedConstantsRule", builder.getOffsetDecl() + 2));
  }
}
