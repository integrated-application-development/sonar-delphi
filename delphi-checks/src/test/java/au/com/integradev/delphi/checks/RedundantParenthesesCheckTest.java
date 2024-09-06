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
  void testPrimaryExpressionShouldNotAddIssue() {
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
  void testBinaryExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := 1 + 2;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnaryExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := -123;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testParenthesesOnBinaryExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := (1 + 2);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testParenthesesOnUnaryExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := (-123);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testParenthesesOnNonTrivialInheritedExpressionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  (inherited Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testParenthesesOnParenthesizedExpressionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+2:12 to +2:13] <<>>")
                .appendImpl("  // Fix@[+1:20 to +1:21] <<>>")
                .appendImpl("  Result := ((1 + 2)); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testParenthesesOnPrimaryExpressionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetInteger: Integer;")
                .appendImpl("begin")
                .appendImpl("  // Fix qf1@[+6:12 to +6:13] <<>>")
                .appendImpl("  // Fix qf2@[+5:13 to +5:14] <<>>")
                .appendImpl("  // Fix qf2@[+4:17 to +4:18] <<>>")
                .appendImpl("  // Fix qf1@[+3:18 to +3:19] <<>>")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  Result := ((123));")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testParenthesesOnBareInheritedExpressionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantParenthesesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+2:2 to +2:3] <<>>")
                .appendImpl("  // Fix@[+1:12 to +1:13] <<>>")
                .appendImpl("  (inherited); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
