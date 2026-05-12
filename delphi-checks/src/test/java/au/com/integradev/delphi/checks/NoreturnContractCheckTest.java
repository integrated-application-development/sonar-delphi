/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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

class NoreturnContractCheckTest {
  @Test
  void testNoreturnRoutineThatRaisesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure AlwaysRaises; noreturn;")
                .appendImpl("begin")
                .appendImpl("  raise Exception.Create('Error');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoreturnRoutineThatCanReturnShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MayReturn; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  WriteLn('Hello');")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineWithConditionalRaiseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure ConditionalRaise; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  if True then")
                .appendImpl("    raise Exception.Create('Error');")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRoutineWithoutNoreturnShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure NormalRoutine;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('Hello');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoreturnRoutineCallingHaltShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure AlwaysHalts; noreturn;")
                .appendImpl("begin")
                .appendImpl("  Halt;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoreturnRoutineWithTryFinallyAndPossibleRaiseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MaybeRaise;")
                .appendImpl("begin")
                .appendImpl("  // ...")
                .appendImpl("end;")
                .appendImpl("")
                .appendImpl("procedure MightRaisesWithCleanup; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    MaybeRaise;")
                .appendImpl("  finally")
                .appendImpl("    WriteLn('Cleanup');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineCallingAnotherNoreturnRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendDecl("procedure Abort; noreturn;")
                .appendImpl("procedure Abort;")
                .appendImpl("begin")
                .appendImpl("  raise Exception.Create('Error');")
                .appendImpl("end;")
                .appendImpl("procedure Outer; noreturn;")
                .appendImpl("begin")
                .appendImpl("  Abort;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoreturnRoutineWithTryFinallyAndRaiseShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure AlwaysRaisesWithCleanup; noreturn;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    raise Exception.Create('Error');")
                .appendImpl("  finally")
                .appendImpl("    WriteLn('Cleanup');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoreturnRoutineWithTryFinallyAndHaltShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure AlwaysHaltsWithCleanup; noreturn;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    Halt;")
                .appendImpl("  finally")
                .appendImpl("    WriteLn('Cleanup');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyNoreturnRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure EmptyNoreturn; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testAsmRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure AlwaysHalts; noreturn;")
                .appendImpl("asm")
                .appendImpl("")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoreturnRoutineCallingHaltAndExitShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure AlwaysHalts; noreturn;")
                .appendImpl("begin")
                .appendImpl("  Halt;")
                .appendImpl("  Exit;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNoreturnRoutineWithGotoJumpingAfterHaltShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure GotoAfterHalt; noreturn; // Noncompliant")
                .appendImpl("label")
                .appendImpl("  AfterHalt;")
                .appendImpl("begin")
                .appendImpl("  goto AfterHalt;")
                .appendImpl("  Halt;")
                .appendImpl("  AfterHalt:")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineWithGotoJumpingAfterRaiseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure GotoAfterRaise; noreturn; // Noncompliant")
                .appendImpl("label")
                .appendImpl("  AfterRaise;")
                .appendImpl("begin")
                .appendImpl("  goto AfterRaise;")
                .appendImpl("  raise Exception.Create('Error');")
                .appendImpl("  AfterRaise:")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineWithRaiseToSpecificExceptShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure RaiseToSpecificExcept; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    raise Exception.Create('Error');")
                .appendImpl("  except")
                .appendImpl("    on Exception do begin")
                .appendImpl("      WriteLn('...');")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineWithRaiseToBareExceptShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure RaiseToBareExcept; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    raise Exception.Create('Error');")
                .appendImpl("  except")
                .appendImpl("    WriteLn('...');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineWithRaiseToExceptElseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure RaiseToExceptElse; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    raise Exception.Create('Error');")
                .appendImpl("  except")
                .appendImpl("    on EArgumentException do begin")
                .appendImpl("      WriteLn('...');")
                .appendImpl("    end")
                .appendImpl("    else begin")
                .appendImpl("      WriteLn('...');")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineWithRaiseToIrrelevantExceptShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure RaiseToIrrelevantExcept; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    raise Exception.Create('Error');")
                .appendImpl("  except")
                .appendImpl("    on EArgumentException do begin")
                .appendImpl("      WriteLn('...');")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoreturnRoutineWithRaiseToSpecificEmptyExceptShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new NoreturnContractCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.SysUtils;")
                .appendImpl("")
                .appendImpl("procedure RaiseToSpecificExcept; noreturn; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    raise Exception.Create('Error');")
                .appendImpl("  except")
                .appendImpl("    on Exception do begin")
                .appendImpl("      // ...")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
