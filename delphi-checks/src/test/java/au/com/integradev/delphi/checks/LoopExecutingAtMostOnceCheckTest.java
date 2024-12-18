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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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

  private void doSimpleCompliantLoopTest(LoopType loopType, String loopContents) {
    doLoopTest(loopType, Collections.emptyList(), List.of(loopContents));
  }

  private void doSimpleNoncompliantLoopTest(LoopType loopType, String loopContents) {
    doLoopTest(loopType, List.of(1), List.of(loopContents));
  }

  private void doLoopTest(LoopType loopType, List<String> loopContents) {
    doLoopTest(loopType, Collections.emptyList(), loopContents);
  }

  private void doLoopTest(
      LoopType loopType, List<Integer> secondaryLocations, List<String> loopContents) {
    String annotation;
    if (!secondaryLocations.isEmpty()) {
      annotation =
          "// Noncompliant "
              + secondaryLocations.stream()
                  .map(location -> "(" + location + ")")
                  .collect(Collectors.joining(" "));
    } else {
      annotation = "// Compliant";
    }
    var unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl(format("  %s %s", loopType.loopHeader, annotation));
    for (String loopLine : loopContents) {
      unitBuilder.appendImpl("    " + loopLine);
    }
    unitBuilder.appendImpl(format("  %s", loopType.loopFooter)).appendImpl("end;");

    var verifier =
        CheckVerifier.newVerifier()
            .withCheck(new LoopExecutingAtMostOnceCheck())
            .onFile(unitBuilder);
    if (!secondaryLocations.isEmpty()) {
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
    doSimpleCompliantLoopTest(loopType, "Continue;");
  }

  // Break
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalBreakShouldAddIssue(LoopType loopType) {
    doSimpleNoncompliantLoopTest(loopType, "Break;");
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalBreakShouldNotAddIssue(LoopType loopType) {
    doSimpleCompliantLoopTest(loopType, "if A then Break;");
  }

  // Exit
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalExitShouldAddIssue(LoopType loopType) {
    doSimpleNoncompliantLoopTest(loopType, "Exit;");
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalExitShouldNotAddIssue(LoopType loopType) {
    doSimpleCompliantLoopTest(loopType, "if A then Exit;");
  }

  // Halt
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalHaltShouldAddIssue(LoopType loopType) {
    doSimpleNoncompliantLoopTest(loopType, "Halt;");
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalHaltShouldNotAddIssue(LoopType loopType) {
    doSimpleCompliantLoopTest(loopType, "if A then Halt;");
  }

  // Raise
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalRaiseShouldAddIssue(LoopType loopType) {
    doSimpleNoncompliantLoopTest(loopType, "raise A;");
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalRaiseShouldNotAddIssue(LoopType loopType) {
    doSimpleCompliantLoopTest(loopType, "if A then raise B;");
  }

  // Goto
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalGotoBeforeShouldNotAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:",
            loopType.loopHeader + " // Compliant",
            "  goto before;",
            loopType.loopFooter),
        Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testUnconditionalGotoAfterShouldAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            loopType.loopHeader + " // Noncompliant (1)", //
            "  goto after;", // Secondary
            loopType.loopFooter,
            "after:"),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalGotoShouldNotAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:", //
            loopType.loopHeader + " // Compliant",
            "  if A then goto before; // Compliant",
            loopType.loopFooter),
        Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoBeforeExitShouldAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:", //
            "Exit;",
            loopType.loopHeader + " // Noncompliant (1)",
            "  goto before; // Secondary",
            loopType.loopFooter),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoMultiBlockInfiniteLoopShouldAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:", //
            "Writeln('A');",
            "middle:",
            "goto before;",
            loopType.loopHeader + " // Noncompliant (1)",
            "  goto middle; // Secondary",
            loopType.loopFooter),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testGotoSameBlockInfiniteLoopShouldAddIssue(LoopType loopType) {
    doRoutineTest(
        List.of(
            "before:", //
            "goto before;",
            loopType.loopHeader + " // Noncompliant (1)",
            "  goto before; // Secondary",
            loopType.loopFooter),
        Issues.ExpectingIssues);
  }

  // Mixed
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfBreakElseExitShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of(2, 4),
        List.of(
            "if A then", //
            "  Break // Secondary",
            "else",
            "  Exit; // Secondary"));
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfBreakElseIfExitShouldNotAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of(
            "if A then", //
            "  Break // Compliant",
            "else if B then",
            "  Exit; // Compliant"));
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfExitElseIfBreakThenExitShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of(5),
        List.of(
            "if A then", //
            "  Exit // Compliant",
            "else if B then",
            "  Break; // Compliant",
            "Exit // Secondary"));
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testIfContinueElseExitShouldNotAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of(
            "if A then", //
            "  Continue // Compliant",
            "else",
            "  Exit; // Compliant"));
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testConditionalBreakAndUnconditionalExitShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of(3),
        List.of(
            "if B then", //
            "  Break;",
            "Exit; // Secondary"));
  }
}
