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

import static java.lang.String.format;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RedundantJumpCheckTest {

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
        CheckVerifier.newVerifier().withCheck(new RedundantJumpCheck()).onFile(unitBuilder);
    if (issues == Issues.ExpectingIssues) {
      verifier.verifyIssues();
    } else {
      verifier.verifyNoIssues();
    }
  }

  private void doExitTest(List<String> functionContents, Issues issues) {
    var unitBuilder =
        new DelphiTestUnitBuilder()
            .appendImpl("function A: Boolean; begin end;")
            .appendImpl("function B: Boolean; begin end;")
            .appendImpl("function C: Boolean; begin end;")
            .appendImpl("function Test: Integer;")
            .appendImpl("begin");
    for (String loopLine : functionContents) {
      unitBuilder.appendImpl("  " + loopLine);
    }
    unitBuilder.appendImpl("end;");

    var verifier =
        CheckVerifier.newVerifier().withCheck(new RedundantJumpCheck()).onFile(unitBuilder);
    if (issues == Issues.ExpectingIssues) {
      verifier.verifyIssues();
    } else {
      verifier.verifyNoIssues();
    }
  }

  // Continue
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBareContinueShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Continue; // Noncompliant"), Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopIfElseContinuesShouldAddIssues(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of("if Foo then Continue // Noncompliant", "else Continue; // Noncompliant"),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalContinueShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of("if Foo then Bar else Continue; // Noncompliant"),
        Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopContinueBeforeEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Continue; // Compliant", "Bar;"), Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalContinueBeforeEndShouldNotAddIssues(LoopType loopType) {
    doLoopTest(
        loopType, List.of("if Foo then Continue; // Compliant", "Bar;"), Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopContinueAtEndShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Bar;", "Continue; // Noncompliant"), Issues.ExpectingIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalContinueAtEndShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType, List.of("Bar;", "if Foo then Continue; // Noncompliant"), Issues.ExpectingIssues);
  }

  // Break
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBareBreakShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Break; // Compliant"), Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalBreakShouldNotAddIssue(LoopType loopType) {
    doLoopTest(
        loopType, List.of("if Foo then Bar else Break; // Compliant"), Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBreakBeforeEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Break; // Compliant", "Bar;"), Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalBreakBeforeEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(
        loopType, List.of("if Foo then Break; // Compliant", "Bar;"), Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBreakAtEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Bar;", "Break; // Compliant"), Issues.ExpectingNoIssues);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalBreakAtEndShouldAddIssue(LoopType loopType) {
    doLoopTest(
        loopType, List.of("Bar;", "if Foo then Continue; // Noncompliant"), Issues.ExpectingIssues);
  }

  // Exit
  @Test
  void testExitWithValueAtEndShouldNotAddIssue() {
    doExitTest(List.of("Foo;", "Exit(42); // Compliant"), Issues.ExpectingNoIssues);
  }

  @Test
  void testExitWithValueBeforeEndShouldNotAddIssue() {
    doExitTest(List.of("Exit(42); // Compliant", "Foo;"), Issues.ExpectingNoIssues);
  }

  @Test
  void testExitShouldAddIssue() {
    doExitTest(List.of("Exit; // Noncompliant"), Issues.ExpectingIssues);
  }

  @Test
  void testExitAtEndShouldAddIssue() {
    doExitTest(List.of("Foo;", "Exit; // Noncompliant"), Issues.ExpectingIssues);
  }

  @Test
  void testExitBeforeEndShouldNotAddIssue() {
    doExitTest(List.of("Exit; // Noncompliant", "Foo;"), Issues.ExpectingNoIssues);
  }

  @Test
  void testConditionalExitBeforeEndShouldNotAddIssue() {
    doExitTest(List.of("if Bar then Exit; // Compliant", "Foo;"), Issues.ExpectingNoIssues);
  }

  @Test
  void testExitInTryFinallyBeforeEndShouldNotAddIssue() {
    doExitTest(
        List.of("try", "  if True then Exit; // Compliant", "finally", "  Foo1;", "end;", "Foo2;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testExitInNestedTryFinallyBeforeEndShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    if True then Exit; // Compliant",
            "  finally",
            "    Foo1;",
            "  end;",
            "finally",
            "  Foo2;",
            "end;",
            "Foo3;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testExitInNestedTryFinallyBeforeInnerEndShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    if True then Exit; // Compliant",
            "  finally",
            "    Foo1;",
            "  end;",
            "  Foo2;",
            "finally",
            "  Foo3;",
            "end;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testExitInTryFinallyAtEndShouldAddIssue() {
    doExitTest(
        List.of("try", "  if True then Exit; // Noncompliant", "finally", "  Foo1;", "end;"),
        Issues.ExpectingIssues);
  }

  @Test
  void testExitInNestedTryFinallyAtEndShouldAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    if True then Exit; // Noncompliant",
            "  finally",
            "    Foo1;",
            "  end;",
            "finally",
            "  Foo2;",
            "end;"),
        Issues.ExpectingIssues);
  }

  @Test
  void testExitInNestedTryExceptFinallyExceptBeforeEndShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    Exit; // Compliant",
            "  except",
            "    Foo1;",
            "  end;",
            "finally",
            "  Foo2;",
            "end;",
            "Foo;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testConditionalExitThenRaiseRaisesNoIssues() {
    doExitTest(
        List.of(
            "try",
            "  A;",
            "except",
            "  on E: Exception do begin",
            "    if B then Exit; // Compliant",
            "  raise;",
            "  end;",
            "end;",
            "C;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testExceptExitShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    A;",
            "  except",
            "    on E: Exception do begin",
            "      Exit;",
            "    end;",
            "  end;",
            "  B;",
            "finally;",
            "  C;",
            "end;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testForLoopAfterConditionalTryFinallyExitShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  A;",
            "  if B then begin Exit end; // Compliant",
            "finally",
            "  C;",
            "end;",
            "for var I in [1..2] do A;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testConditionalExitInForLoopTryFinallyShouldNotAddIssue() {
    doExitTest(
        List.of(
            "for var I in [1..2] do begin",
            "  A;",
            "  try",
            "    B;",
            "    if C then begin",
            "      Exit",
            "    end;",
            "  finally",
            "    A;",
            "  end;",
            "end;",
            "B;"),
        Issues.ExpectingNoIssues);
  }

  @Test
  void testAnonymousMethod() {
    doExitTest(
        List.of("var A := procedure", "  begin", "    Exit; // Noncompliant", "  end;"),
        Issues.ExpectingIssues);
  }
}
