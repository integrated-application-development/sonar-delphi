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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.FatalAnalysisError;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class ForbiddenImportFilePatternCheckTest {
  private static final String FORBIDDEN_PATTERN = "**/Foo.pas";
  private static final String FORBIDDEN_SYNTAX = "GLOB";

  private static DelphiCheck createCheck(String whitelistPattern, String whitelistSyntax) {
    ForbiddenImportFilePatternCheck check = new ForbiddenImportFilePatternCheck();
    check.forbiddenImportPattern = FORBIDDEN_PATTERN;
    check.forbiddenImportSyntax = FORBIDDEN_SYNTAX;
    check.whitelistPattern = whitelistPattern;
    check.whitelistSyntax = whitelistSyntax;
    return check;
  }

  @Test
  void testForbiddenImportShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("", ""))
        .withSearchPathUnit(new DelphiTestUnitBuilder().unitName("Foo"))
        .withSearchPathUnit(new DelphiTestUnitBuilder().unitName("Bar"))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    Foo")
                .appendDecl("  , Bar")
                .appendDecl("  ;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testForbiddenImportInWhitelistedFileShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("**/Baz.pas", "GLOB"))
        .withSearchPathUnit(new DelphiTestUnitBuilder().unitName("Foo"))
        .onFile(
            new DelphiTestUnitBuilder()
                .unitName("Baz")
                .appendDecl("uses")
                .appendDecl("    Foo")
                .appendDecl("  ;"))
        .verifyNoIssues();
  }

  @Test
  void testInvalidPatternShouldThrow() {
    assertThatThrownBy(
            () ->
                CheckVerifier.newVerifier()
                    .withCheck(createCheck("[", "REGEX"))
                    .withSearchPathUnit(new DelphiTestUnitBuilder().unitName("Foo"))
                    .onFile(
                        new DelphiTestUnitBuilder()
                            .appendDecl("uses")
                            .appendDecl("    Foo")
                            .appendDecl("  ;"))
                    .verifyNoIssues())
        .isInstanceOf(FatalAnalysisError.class);
  }
}
