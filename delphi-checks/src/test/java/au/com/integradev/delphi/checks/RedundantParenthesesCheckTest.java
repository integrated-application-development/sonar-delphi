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

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class RedundantParenthesesCheckTest {
  @Test
  void testNoParenthesesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := 123;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testParenthesesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := (123);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRedundantParenthesesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+2:13 to +2:14] <<>>")
                .appendImpl("  // Fix@[+1:17 to +1:18] <<>>")
                .appendImpl("  Result := ((123)); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
