/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

class EmptyBlockCheckTest {
  @Test
  void testNonEmptyBlocksShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
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
                .appendImpl("procedure GlobalProcedureFive;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('OK');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyBeginStatementsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
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
                .appendImpl("  if Foo then begin")
                .appendImpl("    Bar;")
                .appendImpl("  end;")
                .appendImpl("end;")
                .appendImpl("procedure TEmptyProcs.Three;")
                .appendImpl("begin")
                .appendImpl("  if Foo then begin")
                .appendImpl("    // This exists for X reason")
                .appendImpl("  end;")
                .appendImpl("end;")
                .appendImpl("procedure GlobalProcedureFour;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('OK');")
                .appendImpl("end;")
                .appendImpl("procedure GlobalProcedureFive;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  if Foo then begin")
                .appendImpl("  end")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  else begin")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testEmptyBlocksInCaseStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(Arg: Integer);")
                .appendImpl("begin")
                .appendImpl("  case Arg of")
                .appendImpl("    // Noncompliant@+1")
                .appendImpl("    0: begin")
                .appendImpl("    end;")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  else begin")
                .appendImpl("  end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNonEmptyBlocksInCaseStatementShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(Arg: Integer);")
                .appendImpl("begin")
                .appendImpl("  case Arg of")
                .appendImpl("    0: begin")
                .appendImpl("      // Do nothing")
                .appendImpl("    end;")
                .appendImpl("  else begin")
                .appendImpl("    // Do nothing")
                .appendImpl("  end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TEmptyProcs = class")
                .appendDecl("  public")
                .appendDecl("    procedure One;")
                .appendDecl("  end;")
                .appendImpl("procedure TEmptyProcs.One;")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyAnonymousMethodShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TProc = reference to procedure;")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Bar: TProc;")
                .appendImpl("begin")
                .appendImpl("  Bar :=")
                .appendImpl("    procedure")
                .appendImpl("    begin")
                .appendImpl("    end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyExceptShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    // Do risky stuff")
                .appendImpl("  except")
                .appendImpl("  end;")
                .appendImpl("  try")
                .appendImpl("    // Do risky stuff")
                .appendImpl("  except")
                .appendImpl("    on E: Exception do begin")
                .appendImpl("   end;")
                .appendImpl("    else begin")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNestedEmptyElsesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyBlockCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  if Bar then begin")
                .appendImpl("  end")
                .appendImpl("  else begin")
                .appendImpl("    // Noncompliant@+1")
                .appendImpl("    if Baz then begin")
                .appendImpl("    end")
                .appendImpl("    // Noncompliant@+1")
                .appendImpl("    else begin")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
