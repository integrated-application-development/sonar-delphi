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

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedGlobalVariablesRuleTest extends BasePmdRuleTest {
  @Test
  void testUsedGlobalConstantShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  Foo: Integer;")
            .appendImpl("procedure SetFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := 123;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("UnusedGlobalVariablesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUnusedGlobalConstantShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  Foo: Integer;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedGlobalVariablesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUnusedAutoCreateFormVarShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  Vcl.Forms;")
            .appendDecl("type")
            .appendDecl("  TFooForm = class(TForm)")
            .appendDecl("  end;")
            .appendDecl("var")
            .appendDecl("  Foo: TFooForm;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("UnusedGlobalVariablesRule", builder.getOffsetDecl() + 2));
  }
}
