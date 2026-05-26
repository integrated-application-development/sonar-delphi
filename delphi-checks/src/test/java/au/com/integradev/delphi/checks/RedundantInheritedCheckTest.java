/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import au.com.integradev.delphi.builders.DelphiTestFile;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class RedundantInheritedCheckTest {
  @Test
  void testOverridingParentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  begin")
                .appendImpl("    inherited;")
                .appendImpl("  end;")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOverridingParentInCompoundStatementShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  begin")
                .appendImpl("    inherited;")
                .appendImpl("  end;")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOverridingParentAndGrandparentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TGrandChild = class(TChild)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TGrandChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  begin")
                .appendImpl("    inherited;")
                .appendImpl("  end;")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOverridingGrandparentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase);")
                .appendDecl("  TGrandChild = class(TChild)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TGrandChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  begin")
                .appendImpl("    inherited;")
                .appendImpl("  end;")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOverridingMethodWithDifferentParameterNamesShouldNotAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure(A: TObject);")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(B: TObject);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOverridingMethodWithDifferentArgsShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure(A: TObject); overload;")
                .appendDecl("    procedure MyProcedure(A: TObject; B: String); overload;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMessageHandlerShouldNotAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Handler(A: TObject); message 1;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Handler;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAsmRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("asm")
                .appendImpl("  MOV EAX, 1")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonMethodShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNotOverridingMethodShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  // Fix qf1@[+1:2 to +2:2] <<>>")
                .appendImpl("  inherited;")
                .appendImpl("  // Noncompliant@-1")
                .appendImpl("  // Fix qf2@[+1:13 to +1:23] <<>>")
                .appendImpl("  {$ifndef A}inherited;{$endif} // Noncompliant")
                .appendImpl("  begin")
                .appendImpl("    // Fix qf3@[+1:4 to +1:14] <<>>")
                .appendImpl("    inherited; // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("  // Fix qf4@[+0:34 to +1:12] <<>>")
                .appendImpl("  inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testThenBranchShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(Sender: TObject);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure(Sender: TObject);")
                .appendImpl("begin")
                .appendImpl("  if Sender = nil then")
                .appendImpl("    // Fix qf1@[+1:4 to +1:13] <<>>")
                .appendImpl("    inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testElseBranchShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(Sender: TObject);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure(Sender: TObject);")
                .appendImpl("begin")
                .appendImpl("  if Sender = nil then")
                .appendImpl("  begin")
                .appendImpl("    Exit;")
                .appendImpl("  end")
                .appendImpl("  // Fix qf1@[+1:2 to +2:13] <<>>")
                .appendImpl("  else")
                .appendImpl("    inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInheritedSurroundedByStatementsShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  A := 1;")
                .appendImpl("  // Fix qf1@[+1:2 to +1:13] <<>>")
                .appendImpl("  inherited; // Noncompliant")
                .appendImpl("  A := 2;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInheritedWithTrailingExtraSemicolonShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  // Fix qf1@[+1:2 to +1:14] <<>>")
                .appendImpl("  inherited; ; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInheritedWithTrailingExtraSemicolonBeforeNextStatementShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  // Fix qf1@[+1:2 to +1:15] <<>>")
                .appendImpl("  inherited; ; // Noncompliant")
                .appendImpl("  A := 1;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testLoopBodyShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(Sender: TObject);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure(Sender: TObject);")
                .appendImpl("begin")
                .appendImpl("  while Sender = nil do")
                .appendImpl("    // Fix qf1@[+1:4 to +1:13] <<>>")
                .appendImpl("    inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCaseItemDirectStatementShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(Sender: TObject);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure(Sender: TObject);")
                .appendImpl("begin")
                .appendImpl("  case Integer(Sender) of")
                .appendImpl("    // Fix qf1@[+1:7 to +1:16] <<>>")
                .appendImpl("    0: inherited; // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testOnHandlerDirectStatementShouldAddQuickFix() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject);")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    DoThing;")
                .appendImpl("  except")
                .appendImpl("    // Fix qf1@[+1:23 to +1:32] <<>>")
                .appendImpl("    on E: Exception do inherited; // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIssuesFollowingIncludeDirectiveShouldAddQuickFixes() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantInheritedCheck())
        .onFile(
            DelphiTestFile.fromResource(
                "/au/com/integradev/delphi/checks/RedundantInherited/QuickFixesFollowingIncludeDirectives.pas"))
        .verifyIssues();
  }
}
