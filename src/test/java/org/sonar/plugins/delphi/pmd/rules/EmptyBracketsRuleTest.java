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

class EmptyBracketsRuleTest extends BasePmdRuleTest {

  @Test
  void testMethodParametersEmptyBracketsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TObject)");
    builder.appendDecl("    procedure MyProcedure();");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EmptyBracketsRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testInvocationOfUnknownMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EmptyBracketsRule", builder.getOffset() + 3));
  }

  @Test
  void testInvocationOfKnownMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure MyProcedure;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EmptyBracketsRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitArrayConstructorShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TIntArray = array of Integer;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TIntArray;")
            .appendImpl("begin")
            .appendImpl("  Foo := TIntArray.Create();")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyBracketsRule"));
  }

  @Test
  void testInvocationOfProcVarShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TProc = procedure;")
            .appendImpl("procedure Test(ProcVar: TProc);")
            .appendImpl("begin")
            .appendImpl("  ProcVar();")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyBracketsRule"));
  }

  @Test
  void testInvocationOfProcVarArrayShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TProc = procedure;")
            .appendDecl("  TProcArray = array of TProc;")
            .appendImpl("procedure Test(ProcArray: TProcArray);")
            .appendImpl("begin")
            .appendImpl("  ProcArray[0]();")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyBracketsRule"));
  }

  @Test
  void testAssignedArgumentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Result := TObject.Create;")
            .appendImpl("end;")
            .appendImpl("")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  Result := Assigned(Foo());")
            .appendImpl("  Result := System.Assigned(Foo());")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyBracketsRule"));
  }
}
