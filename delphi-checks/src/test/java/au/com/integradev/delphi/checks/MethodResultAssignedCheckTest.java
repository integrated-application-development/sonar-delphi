/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class MethodResultAssignedCheckTest {
  @Test
  void testNotAssignedResultShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testNotAssignedOutParameterShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(")
                .appendImpl(" out Bar: TObject);")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssueOnLine(8);
  }

  @Test
  void testNotAssignedOtherParametersShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(")
                .appendImpl("  Bar: TObject;")
                .appendImpl("  var Baz: TObject;")
                .appendImpl("  const Flarp: TObject);")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExplicitlyAssignedResultShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  Result := TObject.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testResultExplicitlyAssignedInNestedFunctionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: TObject;")
                .appendImpl("  procedure Bar;")
                .appendImpl("  begin")
                .appendImpl("    Result := TObject.Create;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testResultAssignedViaFunctionNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  Foo := TObject.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testResultReturnedViaExitShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  Exit(TObject.Create);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testResultReturnedViaExitInNestedFunctionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("  function Bar: TObject;")
                .appendImpl("  begin")
                .appendImpl("    Exit(TObject.Create);")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testResultAssignedViaFunctionNameInNestedFunctionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: TObject;")
                .appendImpl("  procedure Bar;")
                .appendImpl("  begin")
                .appendImpl("    Foo := TObject.Create;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testForLoopVariableAssignedResultOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: Integer;")
                .appendImpl("begin")
                .appendImpl("  for Result := 0 to 100 do;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPassedAsArgumentResultShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Baz(out Bar: TObject);")
                .appendImpl("function Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  Baz(Result);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPassedAsPointerArgumentResultShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Baz(out Bar: TObject);")
                .appendImpl("function Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  Baz((@(Result)));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAsmMethodShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(out Bar: TObject): TObject;")
                .appendImpl("asm")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExplicitlyAssignedOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(out Bar: TObject);")
                .appendImpl("begin")
                .appendImpl("  Bar := TObject.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testForLoopVariableAssignedOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(out Bar: Integer);")
                .appendImpl("begin")
                .appendImpl("  for Bar := 0 to 100 do;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPassedAsArgumentOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Baz(out Bar: TObject);")
                .appendImpl("procedure Foo(")
                .appendImpl(" out Bar: TObject);")
                .appendImpl("begin")
                .appendImpl("  Baz(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodStubWithExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(out Bar: TObject): TObject;")
                .appendImpl("begin")
                .appendImpl("  raise Exception.Create('Foo not supported');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodStubWithAssertFalseShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(out Bar: TObject): TObject;")
                .appendImpl("begin")
                .appendImpl("  Assert(False, 'Foo not supported');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodStubWithExceptionAndExtraStatementsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(out Bar: TObject): TObject;")
                .appendImpl("begin")
                .appendImpl("  Result := nil;")
                .appendImpl("  raise Exception.Create('Foo not supported');")
                .appendImpl("  Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodStubWithAssertFalseAndExtraStatementsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(out Bar: TObject): TObject;")
                .appendImpl("begin")
                .appendImpl("  Result := nil;")
                .appendImpl("  Assert(False, 'Foo not supported');")
                .appendImpl("  Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodStubWithVariableAssignmentsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MethodResultAssignedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo(out Bar: TObject): TObject;")
                .appendImpl("var")
                .appendImpl("  Baz: Integer;")
                .appendImpl("begin")
                .appendImpl("  Baz := 5;")
                .appendImpl("  Bar := nil;")
                .appendImpl("  raise Exception.Create('Foo not supported');")
                .appendImpl("  Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
