/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.checks.verifier;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext.Location;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;

class CheckVerifierImplTest {
  private static final String MESSAGE = "Test message";

  @Test
  void testImpliedIssueOnExactMatchingLines() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("  Foo2 = Foo; // Noncompliant")
                    .appendImpl("const")
                    .appendImpl("  Bar = Foo; // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testMustSetCheckBeforeVerify() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("  Bar = Foo; // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set check before calling any verification method!");
    assertThatThrownBy(verifier::verifyNoIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set check before calling any verification method!");
    assertThatThrownBy(verifier::verifyIssueOnFile)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set check before calling any verification method!");
    assertThatThrownBy(verifier::verifyIssueOnProject)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set check before calling any verification method!");
  }

  @Test
  void testMustSetFileBeforeVerify() {
    CheckVerifier verifier = CheckVerifier.newVerifier().withCheck(new WillRaiseIssueOnFooCheck());

    assertThatThrownBy(verifier::verifyIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set file before calling any verification method!");
    assertThatThrownBy(verifier::verifyNoIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set file before calling any verification method!");
    assertThatThrownBy(verifier::verifyIssueOnFile)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set file before calling any verification method!");
    assertThatThrownBy(verifier::verifyIssueOnProject)
        .isInstanceOf(AssertionError.class)
        .hasMessage("Set file before calling any verification method!");
  }

  @Test
  void testImpliedQuickFixWithPositiveOffset() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("// Fix@[+1:8 to +1:11] <<BarBaz>>")
                    .appendDecl("  Bar = Foo; // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testQuickFixCommentWithNoReplacementText() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("// Fix@[+1:8 to +1:11]")
                    .appendDecl("  Bar = Foo; // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testMismatchingQuickFix() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("// Fix@[+1:2 to +1:5] <<BarBaz>>")
                    .appendDecl("  Bar = Foo; // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessageContainingAll("Expected:", "Found 1 non-matching quick fixes:");
  }

  @Test
  void testTwoMismatchingQuickFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("// Fix qf1@[+1:2 to +1:5] <<BarBaz>>")
                    .appendDecl("  Bar = Foo; // Noncompliant")
                    .appendDecl("// Fix qf2@[+1:2 to +1:5] <<BarBaz>>")
                    .appendDecl("  Baz = Foo; // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessageContainingAll("Expected:", "Found 2 non-matching quick fixes:");
  }

  @Test
  void testWrongNumberOfQuickFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("// Fix qf1@[+2:8 to +2:11] <<BarBaz>>")
                    .appendDecl("// Fix qf2@[+1:2 to +1:5] <<BarBaz>>")
                    .appendDecl("  Bar = Foo; // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessage("2 quick fixes expected, found 1");
  }

  @Test
  void testImpliedQuickFixWithNegativeOffset() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("  Bar = Foo; // Noncompliant")
                    .appendDecl("// Fix@[-1:8 to -1:11] <<BarBaz>>"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testImpliedQuickFixWithMultipleEdits() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix qf1@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix qf1@[-2:21 to -2:25] <<(Flarp)>>"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMultipleQuickFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix qf1@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix qf1@[-2:21 to -2:25] <<(Flarp)>>")
                    .appendImpl("initialization")
                    .appendImpl("  // Fix qf2@[+2:2 to +2:5] <<BarBaz>>")
                    .appendImpl("  // Fix qf2@[+1:5 to +1:9] <<(Flarp)>>")
                    .appendImpl("  Foo(84); // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMultipleQuickFixesWithUnnamedFix() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix@[-2:21 to -2:25] <<(Flarp)>>")
                    .appendImpl("initialization")
                    .appendImpl("  // Fix qf2@[+2:2 to +2:5] <<BarBaz>>")
                    .appendImpl("  // Fix qf2@[+1:5 to +1:9] <<(Flarp)>>")
                    .appendImpl("  Foo(84); // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMultipleUnnamedQuickFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix@[-2:21 to -2:25] <<(Flarp)>>")
                    .appendImpl("initialization")
                    .appendImpl("  // Fix@[+2:2 to +2:5] <<BarBaz>>")
                    .appendImpl("  // Fix@[+1:5 to +1:9] <<(Flarp)>>")
                    .appendImpl("  Foo(84); // Noncompliant"));

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testQuickFixesWhenNoQuickFixesAsserted() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testMatchingEditsAcrossMismatchingFixes() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseQuickFixOnFooAndArgsCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("function Foo(const Blomp: Integer): Integer;")
                    .appendDecl("var")
                    .appendDecl("  GBar: Integer = Foo(42); // Noncompliant")
                    .appendDecl("// Fix qf1@[-1:18 to -1:21] <<BarBaz>>")
                    .appendDecl("// Fix qf2@[-2:21 to -2:25] <<(Flarp)>>"));

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testImpliedIssuesWithOffset() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 0;")
                    .appendDecl("  // Noncompliant@+1")
                    .appendDecl("  Foo2 = Foo;")
                    .appendImpl("const")
                    .appendImpl("  Bar = Foo;")
                    .appendImpl("  Bar2 = Foo;")
                    .appendImpl("  // Noncompliant@-1")
                    .appendImpl("  // Noncompliant@-3"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  @Test
  void testImpliedIssueOnNonMatchingLines() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Baz = 0; // Noncompliant")
                    .appendImpl("const")
                    .appendImpl("  // Noncompliant@+1")
                    .appendImpl("  Bar = Baz;"));

    assertThatCode(verifier::verifyNoIssues).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  @Test
  void testImpliedAndNotImpliedIssue() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnFooCheck())
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendDecl("const")
                    .appendDecl("  Foo = 42;")
                    .appendDecl("  Baz = 0; // Noncompliant")
                    .appendDecl("  Bar = Foo;"));

    assertThatThrownBy(verifier::verifyIssues)
        .isInstanceOf(AssertionError.class)
        .hasMessageContainingAll("Issues were expected at", "unexpected at");
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  private static Stream<Arguments> validSecondaryLocationsCases() {
    return Stream.of(
        Arguments.of(Named.of("InOrder", "(1) (2) (3)"), "(-2) (-1)"),
        Arguments.of(Named.of("OutOfOrder", "(3) (1) (2)"), "(-1) (-2)"),
        Arguments.of(Named.of("ExtraSpaces", "   ( 1  )   (2 ) ( 3)"), "(-2   ) (-1  ) "));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validSecondaryLocationsCases")
  void testValidSecondaryLocationsOnIssue(String annotation1, String annotation2) {
    Supplier<CheckVerifier> verifier =
        () ->
            CheckVerifier.newVerifier()
                .withCheck(new WillRaiseDirectionalSecondariesOnIntegersCheck())
                .onFile(
                    new DelphiTestUnitBuilder()
                        .appendDecl("const")
                        .appendDecl("  Foo0 = 2; // Noncompliant " + annotation1)
                        .appendDecl("  Foo1 = 2;")
                        .appendDecl("  Foo2 = 2;")
                        .appendDecl("  Foo3 = 2;")
                        .appendDecl("  Bar0 = 1;")
                        .appendDecl("  Bar1 = 1;")
                        .appendDecl("  Bar2 = 1; // Noncompliant " + annotation2));

    assertThatCode(verifier.get()::verifyIssues).doesNotThrowAnyException();
    assertThatThrownBy(verifier.get()::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  private static Stream<Arguments> invalidSecondaryLocationsCases() {
    return Stream.of(
        Arguments.of(Named.of("MissingLocation", "(1) (2)"), "(-2)"),
        Arguments.of(Named.of("ExtraLocation", "(1) (2) (3) (0)"), "(-2) (0) (-1)"),
        Arguments.of(Named.of("ErroneousFlow", "(1, 0) (2) (3)"), "(-2, 1) (-1)"),
        Arguments.of(Named.of("MissingLocation", "(1) (3)"), "(-2)"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidSecondaryLocationsCases")
  void testInvalidSecondaryLocationsOnIssue(String annotation1, String annotation2) {
    Supplier<CheckVerifier> verifier =
        () ->
            CheckVerifier.newVerifier()
                .withCheck(new WillRaiseDirectionalSecondariesOnIntegersCheck())
                .onFile(
                    new DelphiTestUnitBuilder()
                        .appendDecl("const")
                        .appendDecl("  Foo0 = 2; // Noncompliant " + annotation1)
                        .appendDecl("  Foo1 = 2;")
                        .appendDecl("  Foo2 = 2;")
                        .appendDecl("  Foo3 = 2;")
                        .appendDecl("  Bar0 = 1;")
                        .appendDecl("  Bar1 = 1;")
                        .appendDecl("  Bar2 = 1; // Noncompliant " + annotation2));

    assertThatThrownBy(verifier.get()::verifyIssues)
        .hasMessageContainingAll("Issues were expected at", "unexpected at")
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidSecondaryLocationsCases")
  void testInvalidSecondaryLocationsOnIssueWithOffset(String annotation1, String annotation2) {
    Supplier<CheckVerifier> verifier =
        () ->
            CheckVerifier.newVerifier()
                .withCheck(new WillRaiseDirectionalSecondariesOnIntegersCheck())
                .onFile(
                    new DelphiTestUnitBuilder()
                        .appendDecl("const")
                        .appendDecl("  Foo0 = 2;")
                        .appendDecl("  Foo1 = 2; // Noncompliant@-1 " + annotation1)
                        .appendDecl("  Foo2 = 2;")
                        .appendDecl("  Foo3 = 2;")
                        .appendDecl("  Bar0 = 1;")
                        .appendDecl("  Bar1 = 1; // Noncompliant@+1 " + annotation2)
                        .appendDecl("  Bar2 = 1;"));

    assertThatThrownBy(verifier.get()::verifyIssues)
        .hasMessageContainingAll("Issues were expected at", "unexpected at")
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  private static Stream<Arguments> validFlowsLocationsCases() {
    return Stream.of(
        Arguments.of(Named.of("InOrder", "(-3, -1, 2, 3) (-2, 1)")),
        Arguments.of(Named.of("OutOfOrder", "(-2, 1) (-3, -1, 2, 3)")),
        Arguments.of(Named.of("ExtraSpaces", "  (   -3,  -1    ,2, 3   )    (-2 , 1)")));
  }

  @ParameterizedTest
  @MethodSource("validFlowsLocationsCases")
  void testValidFlowsLocations(String flows) {
    Supplier<CheckVerifier> verifier =
        () ->
            CheckVerifier.newVerifier()
                .withCheck(new WillRaiseFlowOnEachIntegerLiteralForBinaryExpressionCheck())
                .onFile(
                    new DelphiTestUnitBuilder()
                        .appendDecl("const")
                        .appendDecl("  Foo0 = 0;")
                        .appendDecl("  Bar0 = 1;")
                        .appendDecl("  Foo1 = 0;")
                        .appendDecl("  Bar1 = 0 + 1; // Noncompliant " + flows)
                        .appendDecl("  Bar2 = 1;")
                        .appendDecl("  Foo2 = 0;")
                        .appendDecl("  Foo3 = 0;"));

    assertThatCode(verifier.get()::verifyIssues).doesNotThrowAnyException();
    assertThatThrownBy(verifier.get()::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  @ParameterizedTest
  @MethodSource("validFlowsLocationsCases")
  void testValidFlowsLocationsWithOffset(String flows) {
    Supplier<CheckVerifier> verifier =
        () ->
            CheckVerifier.newVerifier()
                .withCheck(new WillRaiseFlowOnEachIntegerLiteralForBinaryExpressionCheck())
                .onFile(
                    new DelphiTestUnitBuilder()
                        .appendDecl("const")
                        .appendDecl("  Foo0 = 0;")
                        .appendDecl("  Bar0 = 1;")
                        .appendDecl("  Foo1 = 0;")
                        .appendDecl("  Bar1 = 0 + 1;")
                        .appendDecl("  Bar2 = 1; // Noncompliant@-1 " + flows)
                        .appendDecl("  Foo2 = 0;")
                        .appendDecl("  Foo3 = 0;"));

    assertThatCode(verifier.get()::verifyIssues).doesNotThrowAnyException();
    assertThatThrownBy(verifier.get()::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  private static Stream<Arguments> invalidFlowsLocationsCases() {
    return Stream.of(
        Arguments.of(Named.of("LocationsOutOfOrder", "(-1, 2, 3, -3) (1, -2)")),
        Arguments.of(Named.of("MissingFlow", "(-3, -1, 2, 3)")),
        Arguments.of(Named.of("ExtraFlow", "(-1, 2, 3, -3) (1, -2) ()")),
        Arguments.of(Named.of("MissingLocation", "(-3, -1, 2) (-2)")),
        Arguments.of(Named.of("EmptyFlow", "()")),
        Arguments.of(Named.of("NoFlows", "")));
  }

  @ParameterizedTest
  @MethodSource("invalidFlowsLocationsCases")
  void testInvalidFlowsLocationsOnIssue(String annotation) {
    Supplier<CheckVerifier> verifier =
        () ->
            CheckVerifier.newVerifier()
                .withCheck(new WillRaiseFlowOnEachIntegerLiteralForBinaryExpressionCheck())
                .onFile(
                    new DelphiTestUnitBuilder()
                        .appendDecl("const")
                        .appendDecl("  Foo0 = 0;")
                        .appendDecl("  Bar0 = 1;")
                        .appendDecl("  Foo1 = 0;")
                        .appendDecl("  Bar1 = 0 + 1; // Noncompliant " + annotation)
                        .appendDecl("  Bar2 = 1;")
                        .appendDecl("  Foo2 = 0;")
                        .appendDecl("  Foo3 = 0;"));

    assertThatThrownBy(verifier.get()::verifyIssues)
        .hasMessageContainingAll("Issues were expected at", "unexpected at")
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  @ParameterizedTest
  @MethodSource("invalidFlowsLocationsCases")
  void testInvalidFlowsLocationsOnIssueWithOffset(String annotation) {
    Supplier<CheckVerifier> verifier =
        () ->
            CheckVerifier.newVerifier()
                .withCheck(new WillRaiseFlowOnEachIntegerLiteralForBinaryExpressionCheck())
                .onFile(
                    new DelphiTestUnitBuilder()
                        .appendDecl("const")
                        .appendDecl("  Foo0 = 0;")
                        .appendDecl("  Bar0 = 1;")
                        .appendDecl("  Foo1 = 0; // Noncompliant@+1 " + annotation)
                        .appendDecl("  Bar1 = 0 + 1;")
                        .appendDecl("  Bar2 = 1;")
                        .appendDecl("  Foo2 = 0;")
                        .appendDecl("  Foo3 = 0;"));

    assertThatThrownBy(verifier.get()::verifyIssues)
        .hasMessageContainingAll("Issues were expected at", "unexpected at")
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyNoIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnFile).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier.get()::verifyIssueOnProject).isInstanceOf(AssertionError.class);
  }

  @Test
  void testFileIssue() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseFileIssueCheck())
            .onFile(new DelphiTestUnitBuilder());

    assertThatCode(verifier::verifyIssueOnFile).doesNotThrowAnyException();

    assertThatThrownBy(verifier::verifyIssues).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyIssueOnProject).isInstanceOf(AssertionError.class);
    assertThatThrownBy(verifier::verifyNoIssues).isInstanceOf(AssertionError.class);
  }

  DelphiTestUnitBuilder unitWithTFlorp(String name) {
    return new DelphiTestUnitBuilder()
        .unitName(name)
        .appendDecl("type")
        .appendDecl("  TFlorp = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    procedure Bonk;")
        .appendDecl("  end;");
  }

  @Test
  void testAddStandardLibraryUnit() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnTFlorpUsagesCheck("System.SysUtils"))
            .withStandardLibraryUnit(unitWithTFlorp("System.SysUtils"))
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendImpl("uses System.SysUtils;")
                    .appendImpl("var Boop: TFlorp; // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Test
  void testAddSearchPathUnit() {
    CheckVerifier verifier =
        CheckVerifier.newVerifier()
            .withCheck(new WillRaiseIssueOnTFlorpUsagesCheck("BlimpBlomp"))
            .withStandardLibraryUnit(unitWithTFlorp("BlimpBlomp"))
            .onFile(
                new DelphiTestUnitBuilder()
                    .appendImpl("uses BlimpBlomp;")
                    .appendImpl("var Boop: TFlorp; // Noncompliant"));

    assertThatCode(verifier::verifyIssues).doesNotThrowAnyException();
  }

  @Rule(key = "WillRaiseIssueOnFoo")
  public static class WillRaiseIssueOnFooCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(
        NameReferenceNode nameReferenceNode, DelphiCheckContext context) {
      if (nameReferenceNode.getImage().equalsIgnoreCase("Foo")) {
        reportIssue(context, nameReferenceNode, MESSAGE);
      }
      return super.visit(nameReferenceNode, context);
    }
  }

  @Rule(key = "WillRaiseQuickFixOnFoo")
  public static class WillRaiseQuickFixOnFooCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(
        NameReferenceNode nameReferenceNode, DelphiCheckContext context) {
      if (nameReferenceNode.getImage().equalsIgnoreCase("Foo")) {
        context
            .newIssue()
            .onNode(nameReferenceNode)
            .withMessage(MESSAGE)
            .withQuickFixes(
                QuickFix.newFix(MESSAGE)
                    .withEdit(QuickFixEdit.replace(nameReferenceNode, "BarBaz")))
            .report();
      }
      return super.visit(nameReferenceNode, context);
    }
  }

  @Rule(key = "WillRaiseQuickFixOnFooAndArgs")
  public static class WillRaiseQuickFixOnFooAndArgsCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(
        NameReferenceNode nameReferenceNode, DelphiCheckContext context) {
      DelphiNode argList =
          nameReferenceNode.getParent().getChild(nameReferenceNode.getChildIndex() + 1);

      if (argList instanceof ArgumentListNode
          && nameReferenceNode.getImage().equalsIgnoreCase("Foo")) {
        context
            .newIssue()
            .onNode(nameReferenceNode)
            .withMessage(MESSAGE)
            .withQuickFixes(
                QuickFix.newFix(MESSAGE)
                    .withEdits(
                        QuickFixEdit.replace(nameReferenceNode, "BarBaz"),
                        QuickFixEdit.replace(argList, "(Flarp)")))
            .report();
      }
      return super.visit(nameReferenceNode, context);
    }
  }

  @Rule(key = "WillRaiseIssueOnTFlorpUsages")
  public static class WillRaiseIssueOnTFlorpUsagesCheck extends DelphiCheck {

    private final String unitName;

    public WillRaiseIssueOnTFlorpUsagesCheck(String unitName) {
      this.unitName = unitName;
    }

    @Override
    public DelphiCheckContext visit(NameReferenceNode nameReference, DelphiCheckContext context) {
      var declaration = nameReference.getNameDeclaration();
      if (declaration instanceof TypeNameDeclaration
          && ((TypeNameDeclaration) declaration).getType().is(unitName + ".TFlorp")) {
        reportIssue(context, nameReference, MESSAGE);
      }
      return super.visit(nameReference, context);
    }
  }

  /**
   * This check will add secondary locations on: the same even integer after the first instance; and
   * the same odd integer before the last instance.
   */
  @Rule(key = "WillRaiseDirectionalSecondariesOnIntegersCheck")
  public static class WillRaiseDirectionalSecondariesOnIntegersCheck extends DelphiCheck {

    private final Map<Integer, List<IntegerLiteralNode>> locations = new HashMap<>();

    @Override
    public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
      context = super.visit(ast, context);
      for (Entry<Integer, List<IntegerLiteralNode>> entry : locations.entrySet()) {
        Integer key = entry.getKey();
        List<IntegerLiteralNode> literals = entry.getValue();
        IntegerLiteralNode node;
        if (key % 2 == 0) {
          node = literals.remove(0);
        } else {
          node = literals.remove(literals.size() - 1);
        }

        context
            .newIssue()
            .onNode(node)
            .withMessage(MESSAGE)
            .withSecondaries(
                literals.stream()
                    .map(literal -> new Location(MESSAGE, literal))
                    .collect(Collectors.toList()))
            .report();
      }
      return context;
    }

    @Override
    public DelphiCheckContext visit(
        IntegerLiteralNode integerLiteralNode, DelphiCheckContext context) {
      Integer number = integerLiteralNode.getValue().intValue();
      if (this.locations.containsKey(number)) {
        locations.get(number).add(integerLiteralNode);
      } else {
        List<IntegerLiteralNode> numberLocations = new ArrayList<>();
        numberLocations.add(integerLiteralNode);
        locations.put(number, numberLocations);
      }

      return context;
    }
  }

  @Rule(key = "WillRaiseFlowOnEachIntegerLiteralForBinaryExpression")
  public static class WillRaiseFlowOnEachIntegerLiteralForBinaryExpressionCheck
      extends DelphiCheck {

    private final List<BinaryExpressionNode> binaryExpressions = new ArrayList<>();
    private final Map<Integer, List<Location>> locations = new HashMap<>();

    @Override
    public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
      context = super.visit(ast, context);
      for (BinaryExpressionNode node : binaryExpressions) {
        IntegerLiteralNode left = node.getLeft().getFirstDescendantOfType(IntegerLiteralNode.class);
        IntegerLiteralNode right =
            node.getRight().getFirstDescendantOfType(IntegerLiteralNode.class);
        if (left == null || right == null) {
          continue;
        }

        List<Location> leftLocations = this.locations.get(left.getValue().intValue());
        List<Location> rightLocations = this.locations.get(right.getValue().intValue());
        context
            .newIssue()
            .onNode(node)
            .withMessage(MESSAGE)
            .withFlows(List.of(leftLocations, rightLocations))
            .report();
      }
      return context;
    }

    @Override
    public DelphiCheckContext visit(
        IntegerLiteralNode integerLiteralNode, DelphiCheckContext context) {
      Integer number = integerLiteralNode.getValue().intValue();
      if (!locations.containsKey(number)) {
        this.locations.put(number, new ArrayList<>());
      }
      this.locations.get(number).add(new Location(MESSAGE, integerLiteralNode));
      return context;
    }

    @Override
    public DelphiCheckContext visit(
        BinaryExpressionNode binaryExpressionNode, DelphiCheckContext context) {
      this.binaryExpressions.add(binaryExpressionNode);
      return context;
    }
  }

  @Rule(key = "WillRaiseFileIssue")
  public static class WillRaiseFileIssueCheck extends DelphiCheck {
    @Override
    public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
      context.newIssue().withMessage(MESSAGE).report();
      return super.visit(ast, context);
    }
  }
}
