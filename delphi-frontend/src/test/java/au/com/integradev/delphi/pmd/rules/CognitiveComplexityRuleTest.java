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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class CognitiveComplexityRuleTest extends BasePmdRuleTest {

  @Test
  void testSimpleMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  if Foo then Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffset() + 1));
  }

  @Test
  void testTooComplexMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("function Foo: Integer;").appendImpl("begin");

    for (int i = 1; i <= 16; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 16
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffset() + 1));
  }

  @Test
  void testTooComplexSubProcedureShouldOnlyAddIssueForSubProcedure() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("  function Bar: Integer;")
            .appendImpl("  begin");

    for (int i = 1; i <= 16; ++i) {
      builder.appendImpl("    if Foo then Bar;"); // 16
    }

    builder
        .appendImpl("  end;")
        .appendImpl("begin")
        .appendImpl("Result := Bar;")
        .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffset() + 1))
        .areExactly(1, ruleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffset() + 2));
  }
}
