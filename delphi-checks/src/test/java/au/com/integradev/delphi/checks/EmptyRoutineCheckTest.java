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

class EmptyRoutineCheckTest {
  @Test
  void testNonEmptyRoutinesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TEmptyProcs = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure One;")
                .appendDecl("    procedure Two;")
                .appendDecl("    procedure Three;")
                .appendDecl("  end;")
                .appendImpl("procedure TEmptyProcs.One;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('OK');")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.Two;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('OK');")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.Three;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('OK');")
                .appendImpl("end;")
                .appendImpl("procedure GlobalProcedureFour;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('OK');")
                .appendImpl("end;")
                .appendImpl("procedure TNonexistentType.ProcedureFive;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('OK');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyRoutinesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TEmptyProcs = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure One;")
                .appendDecl("    procedure Two;")
                .appendDecl("    procedure Three;")
                .appendDecl("    procedure Four;")
                .appendDecl("    procedure Five;")
                .appendDecl("  end;")
                .appendImpl("procedure TEmptyProcs.One; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.Two; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.Three; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure GlobalProcedureFour; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure TNonexistentType.ProcedureFive; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testEmptyExceptionalRoutinesWithoutCommentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TEmptyProcs = class(TObject)")
                .appendDecl("    type")
                .appendDecl("      TNestedType<T> = class(TNestedTypeBase)")
                .appendDecl("        public")
                .appendDecl("          procedure NestedOverride<T>; override;")
                .appendDecl("      end;")
                .appendDecl("  public")
                .appendDecl("    procedure OverrideProc; override;")
                .appendDecl("    procedure VirtualProc; virtual;")
                .appendDecl("  end;")
                .appendImpl("procedure TEmptyProcs.OverrideProc; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.VirtualProc; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("end;")
                .appendImpl(
                    "procedure TEmptyProcs.TTestedType<T>.NestedOverride<T>; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testEmptyExceptionalRoutinesWithCommentsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TEmptyProcs = class(TObject)")
                .appendDecl("    type")
                .appendDecl("      TNestedType<T> = class(TNestedTypeBase)")
                .appendDecl("        public")
                .appendDecl("          procedure NestedOverride<T>; override;")
                .appendDecl("      end;")
                .appendDecl("  public")
                .appendDecl("    procedure OverrideProc; override;")
                .appendDecl("    procedure VirtualProc; virtual;")
                .appendDecl("  end;")
                .appendImpl("procedure TEmptyProcs.OverrideProc;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.VirtualProc;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.TNestedType<T>.NestedOverride<T>;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testForwardTypeDeclarationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TEmptyProcs = class; // forward declaration")
                .appendDecl("  TEmptyProcs = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure VirtualProc; virtual;")
                .appendDecl("  end;")
                .appendImpl("procedure TEmptyProcs.VirtualProc;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOverloadedMethodShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TEmptyProcs = class(TObject)")
                .appendDecl("  public")
                .appendDecl(
                    "    procedure VirtualProc(MyArg: String; MyOtherArg: Boolean); overload;")
                .appendDecl(
                    "    procedure VirtualProc(Arg1: String; Arg2: String); overload; virtual;")
                .appendDecl("  end;")
                .appendImpl(
                    "procedure TEmptyProcs.VirtualProc(FirstName: String; LastName: String);")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testForwardDeclarationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure ForwardProc(FirstName: String; LastName: String); forward;"))
        .verifyNoIssues();
  }

  @Test
  void testExternalImplementationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl(
                    "procedure ExternalProc(FirstName: String; LastName: String); external;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceImplementationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFoo = interface")
                .appendDecl("    procedure Bar;")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TObject, IFoo)")
                .appendDecl("    procedure Bar;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Bar;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
