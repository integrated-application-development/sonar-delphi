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

class TrailingWhitespaceCheckTest {
  @Test
  void testTrailingSpaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("var Foo: TObject; "))
        .verifyIssueOnLine(7);
  }

  @Test
  void testTrailingTabShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;\t"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testTrailingMixedWhitespaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;\t   \t\t \t  "))
        .verifyIssueOnLine(7);
  }

  @Test
  void testNoTrailingWhitespaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;"))
        .verifyNoIssues();
  }

  @Test
  void testLeadingWhitespaceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("\t   \t \t var Foo: TObject;"))
        .verifyNoIssues();
  }
}
