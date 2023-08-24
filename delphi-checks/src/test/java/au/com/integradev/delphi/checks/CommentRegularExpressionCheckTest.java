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

class CommentRegularExpressionCheckTest {
  private static DelphiCheck createCheck(String regularExpression) {
    CommentRegularExpressionCheck check = new CommentRegularExpressionCheck();
    check.regex = regularExpression;
    return check;
  }

  @Test
  void testValidCommentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("(?i).*todo.*"))
        .onFile(new DelphiTestUnitBuilder().appendImpl("// Wow, a comment!"))
        .verifyNoIssues();
  }

  @Test
  void testMatchingCommentShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck("(?i).*todo.*"))
        .onFile(new DelphiTestUnitBuilder().appendImpl("// TODO: Add comment"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testInvalidRegexShouldThrow() {
    assertThatThrownBy(
            () ->
                CheckVerifier.newVerifier()
                    .withCheck(createCheck("*"))
                    .onFile(new DelphiTestUnitBuilder().appendImpl("// TODO: Add comment"))
                    .verifyNoIssues())
        .isInstanceOf(FatalAnalysisError.class);
  }
}
