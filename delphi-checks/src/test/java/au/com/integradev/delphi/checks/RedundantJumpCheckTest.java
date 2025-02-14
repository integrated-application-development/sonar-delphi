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

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RedundantJumpCheckTest {

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

  private void doLoopTest(LoopType loopType, List<String> loopContents, boolean expectIssues) {
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
    if (expectIssues) {
      verifier.verifyIssues();
    } else {
      verifier.verifyNoIssues();
    }
  }

  private void doExitTest(List<String> functionContents, boolean expectIssues) {
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
    if (expectIssues) {
      verifier.verifyIssues();
    } else {
      verifier.verifyNoIssues();
    }
  }

  // Continue
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBareContinueShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Continue; // Noncompliant"), true);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopIfElseContinuesShouldAddIssues(LoopType loopType) {
    doLoopTest(
        loopType,
        List.of("if Foo then Continue // Noncompliant", "else Continue; // Noncompliant"),
        true);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalContinueShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("if Foo then Bar else Continue; // Noncompliant"), true);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopContinueBeforeEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Continue;", "Bar;"), false);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalContinueBeforeEndShouldNotAddIssues(LoopType loopType) {
    doLoopTest(loopType, List.of("if Foo then Continue;", "Bar;"), false);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopContinueAtEndShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Bar;", "Continue; // Noncompliant"), true);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalContinueAtEndShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Bar;", "if Foo then Continue; // Noncompliant"), true);
  }

  // Break
  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBareBreakShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Break;"), false);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalBreakShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("if Foo then Bar else Break;"), false);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBreakBeforeEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Break;", "Bar;"), false);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalBreakBeforeEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("if Foo then Break;", "Bar;"), false);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopBreakAtEndShouldNotAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Bar;", "Break;"), false);
  }

  @ParameterizedTest
  @EnumSource(value = LoopType.class)
  void testLoopConditionalBreakAtEndShouldAddIssue(LoopType loopType) {
    doLoopTest(loopType, List.of("Bar;", "if Foo then Continue; // Noncompliant"), true);
  }

  // Exit
  @Test
  void testExitWithValueAtEndShouldNotAddIssue() {
    doExitTest(List.of("Foo;", "Exit(42);"), false);
  }

  @Test
  void testExitWithValueBeforeEndShouldNotAddIssue() {
    doExitTest(List.of("Exit(42);", "Foo;"), false);
  }

  @Test
  void testExitWithEmptyArgumentListShouldAddIssue() {
    doExitTest(List.of("Exit(); // Noncompliant"), true);
  }

  @Test
  void testExitWithEmptyArgumentListAtEndShouldAddIssue() {
    doExitTest(List.of("Foo;", "Exit(); // Noncompliant"), true);
  }

  @Test
  void testExitWithEmptyArgumentListBeforeEndShouldNotAddIssue() {
    doExitTest(List.of("Exit();", "Foo;"), false);
  }

  @Test
  void testExitShouldAddIssue() {
    doExitTest(List.of("Exit; // Noncompliant"), true);
  }

  @Test
  void testExitAtEndShouldAddIssue() {
    doExitTest(List.of("Foo;", "Exit; // Noncompliant"), true);
  }

  @Test
  void testExitBeforeEndShouldNotAddIssue() {
    doExitTest(List.of("Exit;", "Foo;"), false);
  }

  @Test
  void testConditionalExitBeforeEndShouldNotAddIssue() {
    doExitTest(List.of("if Bar then Exit;", "Foo;"), false);
  }

  @Test
  void testExitInTryFinallyBeforeEndShouldNotAddIssue() {
    doExitTest(
        List.of("try", "  if True then Exit;", "finally", "  Foo1;", "end;", "Foo2;"), false);
  }

  @Test
  void testExitInNestedTryFinallyBeforeEndShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    if True then Exit;",
            "  finally",
            "    Foo1;",
            "  end;",
            "finally",
            "  Foo2;",
            "end;",
            "Foo3;"),
        false);
  }

  @Test
  void testExitInNestedTryFinallyBeforeInnerEndShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    if True then Exit;",
            "  finally",
            "    Foo1;",
            "  end;",
            "  Foo2;",
            "finally",
            "  Foo3;",
            "end;"),
        false);
  }

  @Test
  void testExitInTryFinallyAtEndShouldAddIssue() {
    doExitTest(
        List.of("try", "  if True then Exit; // Noncompliant", "finally", "  Foo1;", "end;"), true);
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
        true);
  }

  @Test
  void testExitInNestedTryExceptFinallyExceptBeforeEndShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  try",
            "    Exit;",
            "  except",
            "    Foo1;",
            "  end;",
            "finally",
            "  Foo2;",
            "end;",
            "Foo;"),
        false);
  }

  @Test
  void testConditionalExitThenRaiseShouldNotAddIssues() {
    doExitTest(
        List.of(
            "try",
            "  A;",
            "except",
            "  on E: Exception do begin",
            "    if B then Exit;",
            "  raise;",
            "  end;",
            "end;",
            "C;"),
        false);
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
        false);
  }

  @Test
  void testForLoopAfterConditionalTryFinallyExitShouldNotAddIssue() {
    doExitTest(
        List.of(
            "try",
            "  A;",
            "  if B then begin Exit end;",
            "finally",
            "  C;",
            "end;",
            "for var I in [1..2] do A;"),
        false);
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
        false);
  }

  @Test
  void testAnonymousMethodExitShouldAddIssue() {
    doExitTest(
        List.of("var A := procedure", "  begin", "    Exit; // Noncompliant", "  end;"), true);
  }

  @Test
  void testGotoShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantJumpCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("label NextLine;")
                .appendImpl("begin")
                .appendImpl("  goto NextLine;")
                .appendImpl("  NextLine:")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAsmRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantJumpCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure AsmRoutine;")
                .appendImpl("asm")
                .appendImpl("  JMP @NextLine")
                .appendImpl("  @NextLine:")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
