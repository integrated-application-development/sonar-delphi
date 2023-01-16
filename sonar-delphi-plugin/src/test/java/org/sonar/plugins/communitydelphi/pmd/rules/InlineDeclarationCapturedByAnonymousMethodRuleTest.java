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

import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class InlineDeclarationCapturedByAnonymousMethodRuleTest extends BasePmdRuleTest {
  @Test
  void testSimpleInlineVarReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("  Foo := 123;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }

  @Test
  void testRegularVarCaptureShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    begin")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }

  @Test
  void testInlineVarCaptureShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    begin")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1,
            ruleKeyAtLine(
                "InlineDeclarationCapturedByAnonymousMethodRule", builder.getOffset() + 6));
  }

  @Test
  void testRegularVarWithinAnonymousMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    var")
            .appendImpl("      Foo: Integer;")
            .appendImpl("    begin")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }

  @Test
  void testInlineVarWithinAnonymousMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Proc: TProc := procedure")
            .appendImpl("    begin")
            .appendImpl("      var Foo: Integer;")
            .appendImpl("      Foo := 123;")
            .appendImpl("    end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineDeclarationCapturedByAnonymousMethodRule"));
  }
}
