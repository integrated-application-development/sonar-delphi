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

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class MethodResultAssignedRuleTest extends BasePmdRuleTest {
  @Test
  void testNotAssignedResultShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodResultAssignedRule", builder.getOffset() + 1));
  }

  @Test
  void testNotAssignedOutParameterShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(")
            .appendImpl(" out Bar: TObject);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodResultAssignedRule", builder.getOffset() + 2));
  }

  @Test
  void testNotAssignedOtherParametersShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(")
            .appendImpl("  Bar: TObject;")
            .appendImpl("  var Baz: TObject;")
            .appendImpl("  const Flarp: TObject);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testExplicitlyAssignedResultShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Result := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testResultExplicitlyAssignedInNestedFunctionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("  procedure Bar;")
            .appendImpl("  begin")
            .appendImpl("    Result := TObject.Create;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testResultAssignedViaFunctionNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Foo := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testResultReturnedViaExitShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Exit(TObject.Create);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testResultReturnedViaExitInNestedFunctionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("  function Bar: TObject;")
            .appendImpl("  begin")
            .appendImpl("    Exit(TObject.Create);")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testResultAssignedViaFunctionNameInNestedFunctionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("  procedure Bar;")
            .appendImpl("  begin")
            .appendImpl("    Foo := TObject.Create;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testForLoopVariableAssignedResultOutParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  for Result := 0 to 100 do;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testPassedAsArgumentResultShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Baz(out Bar: TObject);")
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Baz(Result);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testPassedAsPointerArgumentResultShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Baz(out Bar: TObject);")
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Baz((@(Result)));")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testAsmMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo(out Bar: TObject): TObject;")
            .appendImpl("asm")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testExplicitlyAssignedOutParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(out Bar: TObject);")
            .appendImpl("begin")
            .appendImpl("  Bar := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testForLoopVariableAssignedOutParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(out Bar: Integer);")
            .appendImpl("begin")
            .appendImpl("  for Bar := 0 to 100 do;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testPassedAsArgumentOutParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Baz(out Bar: TObject);")
            .appendImpl("procedure Foo(")
            .appendImpl(" out Bar: TObject);")
            .appendImpl("begin")
            .appendImpl("  Baz(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testMethodStubWithExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo(out Bar: TObject): TObject;")
            .appendImpl("begin")
            .appendImpl("  raise Exception.Create('Foo not supported');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testMethodStubWithAssertFalseShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo(out Bar: TObject): TObject;")
            .appendImpl("begin")
            .appendImpl("  Assert(False, 'Foo not supported');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testMethodStubWithExceptionAndExtraStatementsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo(out Bar: TObject): TObject;")
            .appendImpl("begin")
            .appendImpl("  Result := nil;")
            .appendImpl("  raise Exception.Create('Foo not supported');")
            .appendImpl("  Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testMethodStubWithAssertFalseAndExtraStatementsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo(out Bar: TObject): TObject;")
            .appendImpl("begin")
            .appendImpl("  Result := nil;")
            .appendImpl("  Assert(False, 'Foo not supported');")
            .appendImpl("  Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }

  @Test
  void testMethodStubWithVariableAssignmentsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo(out Bar: TObject): TObject;")
            .appendImpl("var")
            .appendImpl("  Baz: Integer;")
            .appendImpl("begin")
            .appendImpl("  Baz := 5;")
            .appendImpl("  Bar := nil;")
            .appendImpl("  raise Exception.Create('Foo not supported');")
            .appendImpl("  Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodResultAssignedRule"));
  }
}
