/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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

import static java.lang.String.format;

import au.com.integradev.delphi.builders.DelphiTestProgramBuilder;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LoopExecutingAtMostOnceCheckTest {

  enum LoopType {
    WHILE("while A do begin", "end;"),
    FOR_IN("for var A in B do begin", "end;"),
    FOR_TO("for var A := B to C do begin", "end;"),
    FOR_DOWNTO("for var A := B downto C do begin", "end;"),
    REPEAT("repeat", "until A = B;");

    final String loopHeader;
    final String loopFooter;

    LoopType(String loopHeader, String loopFooter) {
      this.loopHeader = loopHeader;
      this.loopFooter = loopFooter;
    }
  }

  // Continue
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalContinueShouldNotAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Compliant", loopType.loopHeader))
                .appendImpl("    Continue; // Compliant")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  // Break
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalBreakShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    Break; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalBreakShouldNotAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Compliant", loopType.loopHeader))
                .appendImpl("    if A then")
                .appendImpl("      Break; // Compliant")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  // Exit
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalExitShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    Exit; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalExitShouldNotAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Compliant", loopType.loopHeader))
                .appendImpl("    if A then")
                .appendImpl("      Exit; // Compliant")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  // Halt
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalHaltShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    Halt; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalHaltShouldNotAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Compliant", loopType.loopHeader))
                .appendImpl("    if A then")
                .appendImpl("      Halt; // Compliant")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  // Raise
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalRaiseShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    raise E; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalRaiseShouldNotAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Compliant", loopType.loopHeader))
                .appendImpl("    if A then")
                .appendImpl("      raise B; // Compliant")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  // Goto
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalGotoBeforeShouldNotAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("  label before;")
                .appendImpl("begin")
                .appendImpl("  before:")
                .appendImpl(format("  %s // Compliant", loopType.loopHeader))
                .appendImpl("    goto before; // Compliant")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalGotoAfterShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("  label after;")
                .appendImpl("begin")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    goto after; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("  after:")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalGotoShouldNotAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("  label before;")
                .appendImpl("begin")
                .appendImpl("  before:")
                .appendImpl(format("  %s // Compliant", loopType.loopHeader))
                .appendImpl("    if A then")
                .appendImpl("      goto before; // Compliant")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoBeforeExitShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("  label before;")
                .appendImpl("begin")
                .appendImpl("  before:")
                .appendImpl("  Exit;")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    goto before; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoMultiBlockInfiniteLoopShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("  label before, middle;")
                .appendImpl("begin")
                .appendImpl("  before:")
                .appendImpl("  Writeln('A');")
                .appendImpl("  middle:")
                .appendImpl("  goto before;")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    goto middle; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoSameBlockInfiniteLoopShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("  label before;")
                .appendImpl("begin")
                .appendImpl("  before:")
                .appendImpl("  goto before;")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    goto before; // Secondary")
                .appendImpl(format("  %s", loopType.loopFooter))
                .appendImpl("end;"))
        .verifyIssues();
  }

  // Mixed
  @Test
  void testIfBreakElseExitShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Noncompliant (4)")
                .appendImpl("    if A then")
                .appendImpl("      Break // Compliant")
                .appendImpl("    else")
                .appendImpl("      Exit; // Secondary")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIfBreakElseIfExitShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Compliant")
                .appendImpl("    if A then")
                .appendImpl("      Break // Compliant")
                .appendImpl("    else if B then")
                .appendImpl("      Exit; // Compliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIfExitElseIfBreakThenExitShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Noncompliant (5)")
                .appendImpl("    if A then")
                .appendImpl("      Exit // Compliant")
                .appendImpl("    else if B then")
                .appendImpl("      Break // Compliant")
                .appendImpl("    Exit; // Secondary")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIfContinueElseExitShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Compliant")
                .appendImpl("    if A then")
                .appendImpl("      Continue // Compliant")
                .appendImpl("    else")
                .appendImpl("      Exit; // Compliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCaseBreakShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Compliant")
                .appendImpl("    case True of")
                .appendImpl("      True: Break; // Compliant")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCaseBreakElseBreakShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Noncompliant (2) (4)")
                .appendImpl("    case True of")
                .appendImpl("      True: Break; // Secondary")
                .appendImpl("    else")
                .appendImpl("      Break; // Secondary")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIfNestedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Compliant")
                .appendImpl("    if A then begin")
                .appendImpl("      if B then")
                .appendImpl("        Break // Compliant")
                .appendImpl("      else")
                .appendImpl("        Exit; // Compliant")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testElseNestedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Noncompliant (7)")
                .appendImpl("    if A then begin")
                .appendImpl("      Break // Compliant")
                .appendImpl("    end else begin")
                .appendImpl("      if B then")
                .appendImpl("        Break // Compliant")
                .appendImpl("      else")
                .appendImpl("        Exit; // Secondary")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testConditionalBreakAndUnconditionalExitShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Noncompliant (3)")
                .appendImpl("    if B then")
                .appendImpl("      Break; // Compliant")
                .appendImpl("    Exit; // Secondary")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIfExitElseBreakAndUnconditionalBreakShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Noncompliant (4) (5)")
                .appendImpl("    if A then")
                .appendImpl("      Exit // Compliant")
                .appendImpl("    else")
                .appendImpl("      Break; // Secondary")
                .appendImpl("    Exit; // Secondary")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInnerNestedLoopViolationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Compliant")
                .appendImpl("    while A do begin // Noncompliant (4)")
                .appendImpl("      if A then")
                .appendImpl("        Exit // Compliant")
                .appendImpl("      else")
                .appendImpl("        Break; // Secondary")
                .appendImpl("    end;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testOuterNestedLoopViolationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  while A do begin // Noncompliant (7)")
                .appendImpl("    while A do begin // Noncompliant (4)")
                .appendImpl("      if A then")
                .appendImpl("        Exit // Compliant")
                .appendImpl("      else")
                .appendImpl("        Exit; // Inner secondary")
                .appendImpl("    end;")
                .appendImpl("  Break; // Outer secondary")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testCfgInProgramShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestProgramBuilder()
                .appendImpl("A := True;")
                .appendImpl(format("%s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("  Break;")
                .appendImpl(format("%s", loopType.loopFooter)))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testCfgInInitializationShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("initialization")
                .appendImpl("  A := True;")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    Break;")
                .appendImpl(format("  %s", loopType.loopFooter)))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testCfgInUnitBeginShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("begin")
                .appendImpl("  A := True;")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    Break;")
                .appendImpl(format("  %s", loopType.loopFooter)))
        .verifyIssues();
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testCfgInFinalizationShouldAddIssue(LoopType loopType) {
    CheckVerifier.newVerifier()
        .withCheck(new LoopExecutingAtMostOnceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("initialization")
                .appendImpl("finalization")
                .appendImpl("  A := True;")
                .appendImpl(format("  %s // Noncompliant (1)", loopType.loopHeader))
                .appendImpl("    Break;")
                .appendImpl(format("  %s", loopType.loopFooter)))
        .verifyIssues();
  }
}
