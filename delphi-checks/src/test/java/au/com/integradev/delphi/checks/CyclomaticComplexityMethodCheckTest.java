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

import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class CyclomaticComplexityMethodCheckTest extends CheckTest {

  @Test
  void testSimpleMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin")
            .appendImpl("  if Foo then Bar;") // 2
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffset() + 1));
  }

  @Test
  void testAlmostTooComplexMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin");

    for (int i = 1; i <= 19; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 20
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffset() + 1));
  }

  @Test
  void testTooComplexMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin");

    for (int i = 1; i <= 20; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 21
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffset() + 1));
  }

  @Test
  void testTooComplexSubProcedureShouldOnlyAddIssueForSubProcedure() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("  function Bar: Integer;") // 1
            .appendImpl("  begin");

    for (int i = 1; i <= 20; ++i) {
      builder.appendImpl("    if Foo then Bar;"); // 21
    }

    builder
        .appendImpl("  end;")
        .appendImpl("begin")
        .appendImpl("Result := Bar;")
        .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffset() + 1))
        .areExactly(1, ruleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffset() + 2));
  }
}
