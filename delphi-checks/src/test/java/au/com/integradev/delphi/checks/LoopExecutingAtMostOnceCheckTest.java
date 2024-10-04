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

import static java.lang.String.format;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LoopExecutingAtMostOnceCheckTest {

  enum LoopType {
    While("while A do begin", "end;"),
    ForIn("for var A in B do begin", "end;"),
    ForTo("for var A := B to C do begin", "end;"),
    ForDownTo("for var A := B downto C do begin", "end;"),
    Repeat("repeat", "until A = B;");

    final String loopHeader;
    final String loopFooter;

    LoopType(String loopHeader, String loopFooter) {
      this.loopHeader = loopHeader;
      this.loopFooter = loopFooter;
    }
  }

  enum Issues {
    ExpectingIssues,
    ExpectingNoIssues
  }

  private void doLoopTest(LoopType loopType, List<String> loopContents, Issues issues) {
    var unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl(format("  %s", loopType.loopHeader));
    for (String loopLine : loopContents) {
      unitBuilder.appendImpl("    " + loopLine);
    }
    unitBuilder.appendImpl(format("  %s", loopType.loopFooter)).appendImpl("end;");

    var verifier =
        CheckVerifier.newVerifier()
            .withCheck(new LoopExecutingAtMostOnceCheck())
            .onFile(unitBuilder);
    if (issues == Issues.ExpectingIssues) {
      verifier.verifyIssues();
    } else {
      verifier.verifyNoIssues();
    }
  }

  private void doRoutineTest(List<String> functionContents, Issues issues) {
    var unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("function A: Boolean; begin end;")
            .appendImpl("function B: Boolean; begin end;")
            .appendImpl("function C: Boolean; begin end;")
            .appendImpl("function Test: Integer;")
            .appendImpl("label before, middle, after;")
            .appendImpl("begin");
    for (String loopLine : functionContents) {
      unitBuilder.appendImpl("  " + loopLine);
    }
    unitBuilder.appendImpl("end;");

    var verifier =
        CheckVerifier.newVerifier()
            .withCheck(new LoopExecutingAtMostOnceCheck())
            .onFile(unitBuilder);
    if (issues == Issues.ExpectingIssues) {
      verifier.verifyIssues();
    } else {
      verifier.verifyNoIssues();
    }
  }

  // Continue
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalContinueShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Continue; // Compliant"), Issues.ExpectingNoIssues);
  }

  // Break
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalBreakShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Break; // Noncompliant"), Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalBreakShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("if A then Break; // Compliant"), Issues.ExpectingNoIssues);
  }

  // Exit
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalExitShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Exit; // Noncompliant"), Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalExitShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("if A then Exit; // Compliant"), Issues.ExpectingNoIssues);
  }

  // Halt
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalHaltShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Halt; // Noncompliant"), Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalHaltShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("if A then Halt; // Compliant"), Issues.ExpectingNoIssues);
  }

  // Raise
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalRaiseShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("raise A; // Noncompliant"), Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalRaiseShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("if A then raise B; // Compliant"), Issues.ExpectingNoIssues);
  }

  // Goto
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalGotoBeforeShouldNotAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of("before:", loopType.loopHeader, "  goto before; // Compliant", loopType.loopFooter),
        Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalGotoAfterShouldNotAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            loopType.loopHeader, "  goto after; // Noncompliant", loopType.loopFooter, "after:"),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalGotoShouldNotAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:",
            loopType.loopHeader,
            "  if A then goto before; // Compliant",
            loopType.loopFooter),
        Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoBeforeExitShouldAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:",
            "Exit;",
            loopType.loopHeader,
            "  goto before; // Noncompliant",
            loopType.loopFooter),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoMultiBlockInfiniteLoopShouldAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:",
            "Writeln('A');",
            "middle:",
            "goto before;",
            loopType.loopHeader,
            "  goto middle; // Noncompliant",
            loopType.loopFooter),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoSameBlockInfiniteLoopShouldAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:",
            "goto before;",
            loopType.loopHeader,
            "  goto before; // Noncompliant",
            loopType.loopFooter),
        Issues.ExpectingIssues);
  }

  // Mixed
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfBreakElseExitShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of("if A then", "  Break // Noncompliant", "else", "  Exit; // Noncompliant"),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfBreakElseIfExitShouldNotAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of("if A then", "  Break // Compliant", "else if B then", "  Exit; // Compliant"),
        Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfExitElseIfBreakThenExitShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of(
            "if A then",
            "  Exit // Compliant",
            "else if B then",
            "  Break; // Compliant",
            "Exit // Noncompliant"),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfContinueElseExitShouldNotAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of("if A then", "  Continue", "else", "  Exit; // Compliant"),
        Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalBreakAndUnconditionalExitShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of("if B then", "  Break; // Compliant", "Exit; // Noncompliant"),
        Issues.ExpectingIssues);
  }
}
