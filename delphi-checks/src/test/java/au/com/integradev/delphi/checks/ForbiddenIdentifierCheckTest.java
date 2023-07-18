/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class ForbiddenIdentifierCheckTest {
  private static String FORBIDDEN_IDENTIFIER = "BadName";

  private static DelphiCheck createCheck() {
    ForbiddenIdentifierCheck check = new ForbiddenIdentifierCheck();
    check.blacklist = FORBIDDEN_IDENTIFIER;
    return check;
  }

  @Test
  void testAllowedIdentifierShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("var")
                .appendDecl("  GoodName: TObject;"))
        .verifyNoIssues();
  }

  @Test
  void testForbiddenIdentifierShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("var")
                .appendDecl("  BadName: TObject;"))
        .verifyIssueOnLine(6);
  }
}
