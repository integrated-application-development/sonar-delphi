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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class ConsecutiveConstSectionCheckTest {
  @Test
  void testConsecutiveInterfaceConstSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveConstSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  CFoo = 5;")
                .appendDecl("  CBar = 10;")
                .appendDecl("const // Noncompliant")
                .appendDecl("  CBaz = 15;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveImplementationConstSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveConstSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("const")
                .appendImpl("  CFoo = 5;")
                .appendImpl("  CBar = 5;")
                .appendImpl("const // Noncompliant")
                .appendImpl("  CBaz = 5;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveLocalConstSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveConstSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("const")
                .appendImpl("  CFoo = 5;")
                .appendImpl("  CBar = 5;")
                .appendImpl("const // Noncompliant")
                .appendImpl("  CBaz = 5;")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveConstAndResourceStringSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveConstSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  CFoo = 5;")
                .appendDecl("  CBar = 5;")
                .appendDecl("resourcestring")
                .appendDecl("  CBaz = 'abc';"))
        .verifyNoIssues();
  }

  @Test
  void testNonConsecutiveConstSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveConstSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  CFoo = 5;")
                .appendDecl("  CBar = 5;")
                .appendDecl("var")
                .appendDecl("  Baz: Integer;")
                .appendDecl("const")
                .appendDecl("  CFlarp = 5;"))
        .verifyNoIssues();
  }
}
