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

class TrailingWhitespaceCheckTest {
  @Test
  void testTrailingSpaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+2:17 to +2:18] <<>>")
                .appendImpl("// Noncompliant@+1")
                .appendImpl("var Foo: TObject; "))
        .verifyIssues();
  }

  @Test
  void testTrailingTabShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+2:17 to +2:18] <<>>")
                .appendImpl("// Noncompliant@+1")
                .appendImpl("var Foo: TObject;\t"))
        .verifyIssues();
  }

  @Test
  void testTrailingMixedWhitespaceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+2:17 to +2:26] <<>>")
                .appendImpl("// Noncompliant@+1")
                .appendImpl("var Foo: TObject;\t   \t\t \t "))
        .verifyIssues();
  }

  @Test
  void testTrailingSpaceOnFollowingLineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+3:0 to +3:3] <<>>")
                .appendImpl("// Noncompliant@+2")
                .appendImpl("var Foo: TObject;")
                .appendImpl("   "))
        .verifyIssues();
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

  @Test
  void testNoTrailingSpaceInLineCommentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder().appendImpl("// there is no trailing whitespace in here"))
        .verifyNoIssues();
  }

  @Test
  void testTrailingSpaceInLineCommentShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+2:42 to +2:43] <<>>")
                .appendImpl("// Noncompliant@+1")
                .appendImpl("// hey, there's a trailing spaces in here! "))
        .verifyIssues();
  }

  @Test
  void testTrailingTabsInLineCommentShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+2:39 to +2:40] <<>>")
                .appendImpl("// Noncompliant@+1")
                .appendImpl("// hey, there's a trailing tab in here!\t"))
        .verifyIssues();
  }

  @Test
  void testNoTrailingSpaceInMultilineCommentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("{")
                .appendImpl("  there is no trailing whitespace in here")
                .appendImpl("}"))
        .verifyNoIssues();
  }

  @Test
  void testTrailingSpaceInMultilineCommentShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+3:40 to +3:41] <<>>")
                .appendImpl("// Noncompliant@+2")
                .appendImpl("{")
                .appendImpl("  hey, there's a trailing space in here! ")
                .appendImpl("}"))
        .verifyIssues();
  }

  @Test
  void testTrailingTabInMultilineCommentShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+3:38 to +3:39] <<>>")
                .appendImpl("// Noncompliant@+2")
                .appendImpl("{")
                .appendImpl("  hey, there's a trailing tab in here!\t")
                .appendImpl("}"))
        .verifyIssues();
  }

  @Test
  void testTrailingSpaceOnFirstLineOfMultilineCommentWithOffsetShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TrailingWhitespaceCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Fix qf@[+2:6 to +2:7] <<>>")
                .appendImpl("// Noncompliant@+1")
                .appendImpl("     {\t")
                .appendImpl("  hey, there's a trailing tab up there!")
                .appendImpl("}"))
        .verifyIssues();
  }
}
