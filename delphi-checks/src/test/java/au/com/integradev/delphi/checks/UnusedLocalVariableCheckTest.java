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

class UnusedLocalVariableCheckTest {
  @Test
  void testUsedInRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure UseFoo(Foo: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer;")
                .appendImpl("begin")
                .appendImpl("  UseFoo(Foo)")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedAssignedInRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  Foo := 0;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUsedAssignedInRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure UseFoo(Foo: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer;")
                .appendImpl("begin")
                .appendImpl("  Foo := 0;")
                .appendImpl("  UseFoo(Foo);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedInRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUsedInlineVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure UseFoo(Foo: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var Foo: Integer;")
                .appendImpl("  UseFoo(Foo);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedInlineVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var Foo: Integer; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedAssignedInlineVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var Foo: Integer; // Noncompliant")
                .appendImpl("  Foo := 0;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUsedAssignedInlineVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure UseFoo(Foo: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var Foo: Integer;")
                .appendImpl("  Foo := 0;")
                .appendImpl("  UseFoo(Foo);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testVariableWithFieldAssignmentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    Bar: Boolean;")
                .appendDecl("  end;")
                .appendDecl("procedure UseFoo(Foo: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo.Create;")
                .appendImpl("  Foo.Bar := True;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testProcVariablePointerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Bar;")
                .appendDecl("    procedure Baz;")
                .appendDecl("  end;")
                .appendDecl("  TObjProc = procedure of object;")
                .appendDecl("procedure UsePtr(Ptr: Pointer);")
                .appendImpl("procedure TFoo.Bar;")
                .appendImpl("var")
                .appendImpl("  Proc: TObjProc;")
                .appendImpl("begin")
                .appendImpl("  Proc := Baz;")
                .appendImpl("  UsePtr(@Proc)")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedGlobalVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("var")
                .appendDecl("  G_Foo: Integer;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedArgumentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedLocalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(Foo: Integer);")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
