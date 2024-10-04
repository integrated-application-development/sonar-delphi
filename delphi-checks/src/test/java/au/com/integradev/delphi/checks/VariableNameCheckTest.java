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
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class VariableNameCheckTest {
  private static DelphiCheck createCheck() {
    VariableNameCheck check = new VariableNameCheck();
    check.globalPrefixes = "G";
    return check;
  }

  @Test
  void testValidGlobalNamesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  GMyChar: Char;")
                .appendDecl("  GAnotherChar: Char;")
                .appendDecl("  GThirdChar: Char;"))
        .verifyNoIssues();
  }

  @Test
  void testInvalidGlobalNamesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  G_My_Char: Char; // Noncompliant")
                .appendDecl("  gAnotherChar: Char; // Noncompliant")
                .appendDecl("  GlobalChar: Char; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testValidNameInRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("var")
                .appendImpl("  SomeVar: Integer;")
                .appendImpl("begin")
                .appendImpl("  SomeVar := 0;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBadPascalCaseInRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("var")
                .appendImpl("  someVar: Integer; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  someVar := 0;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testAutoCreateFormVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFooForm = class(TForm)")
                .appendDecl("  end;")
                .appendDecl("var")
                .appendDecl("  omForm: TFooForm;"))
        .verifyNoIssues();
  }

  @Test
  void testValidArgumentNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure(Arg: Integer);")
                .appendImpl("begin")
                .appendImpl("  Arg := 0;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBadPascalCaseInArgumentNameShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure(arg: Integer); // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  arg := 0;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testValidInlineVariableNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var SomeVar: Integer;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBadPascalCaseInlineVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var someVar: Integer; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testValidLoopVariableNameShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  for var SomeVar := 1 to 100 do;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBadPascalCaseInLoopVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  for var someVar := 1 to 100 do; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testBadPascalCaseInRoutineImplementingGoodPascalCaseInterfaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFoo = interface")
                .appendDecl("    procedure Proc(MyStr: string; MyInt: Integer);")
                .appendDecl("  end;")
                .appendDecl("  TBar = class(TObject, IFoo)")
                .appendDecl("    procedure Proc(")
                .appendDecl("      myStr: string; // Noncompliant")
                .appendDecl("      myInt: Integer // Noncompliant")
                .appendDecl("    );")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testBadPascalCaseInRoutineImplementingBadPascalCaseInterfaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFoo = interface")
                .appendDecl("    // Noncompliant@+2")
                .appendDecl("    // Noncompliant@+1")
                .appendDecl("    procedure Proc(myStr: string; myInt: Integer);")
                .appendDecl("  end;")
                .appendDecl("  TBar = class(TObject, IFoo)")
                .appendDecl("    procedure Proc(myStr: string; myInt: Integer);")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  private static DelphiTestUnitBuilder createVclForms() {
    return new DelphiTestUnitBuilder()
        .unitName("Vcl.Forms")
        .appendDecl("uses")
        .appendDecl("  System.Classes;")
        .appendDecl("type")
        .appendDecl("TForm = class(TComponent)")
        .appendDecl("end;");
  }
}
