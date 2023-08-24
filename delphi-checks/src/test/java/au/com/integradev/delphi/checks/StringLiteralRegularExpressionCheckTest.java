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

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.FatalAnalysisError;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class StringLiteralRegularExpressionCheckTest {
  private static final String PATTERN = ".*FOO(\\d|[A-Z]){8}BAR.*";

  private static DelphiCheck createCheck(String pattern) {
    StringLiteralRegularExpressionCheck check = new StringLiteralRegularExpressionCheck();
    check.regex = pattern;
    return check;
  }

  @Test
  void tesNonMatchingStringShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(PATTERN))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  C_MyConstant = 'Wow, a constant!';"))
        .verifyNoIssues();
  }

  @Test
  void testMatchingStringShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck(PATTERN))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  C_HardcodedIDRef = 'FOO1234X6U8BAR';"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testInvalidRegexShouldThrow() {
    assertThatThrownBy(
            () ->
                CheckVerifier.newVerifier()
                    .withCheck(createCheck("*"))
                    .onFile(
                        new DelphiTestUnitBuilder()
                            .appendDecl("const")
                            .appendDecl("  C_HardcodedIDRef = 'FOO1234X6U8BAR';"))
                    .verifyIssueOnLine(6))
        .isInstanceOf(FatalAnalysisError.class);
  }
}
