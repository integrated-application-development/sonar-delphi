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

class ConsecutiveVarSectionCheckTest {
  @Test
  void testConsecutiveInterfaceVarSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVarSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  Foo: Integer;")
                .appendDecl("  Bar: Integer;")
                .appendDecl("var // Noncompliant")
                .appendDecl("  Baz: Integer;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveImplementationVarSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVarSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("var")
                .appendImpl("  Foo: Integer;")
                .appendImpl("  Bar: Integer;")
                .appendImpl("var // Noncompliant")
                .appendImpl("  Baz: Integer;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveLocalVarSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVarSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer;")
                .appendImpl("  Bar: Integer;")
                .appendImpl("var // Noncompliant")
                .appendImpl("  Baz: Integer;")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveVarAndThreadVarSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVarSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  Foo: Integer;")
                .appendDecl("  Bar: Integer;")
                .appendDecl("threadvar")
                .appendDecl("  Baz: Integer;"))
        .verifyNoIssues();
  }

  @Test
  void testNonConsecutiveVarSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVarSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  Foo: Integer;")
                .appendDecl("  Bar: Integer;")
                .appendDecl("const")
                .appendDecl("  CFlorp = 5;")
                .appendDecl("var")
                .appendDecl("  Baz: Integer;"))
        .verifyNoIssues();
  }

  @Test
  void testConsecutiveFieldSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveVarSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyType = class(TObject)")
                .appendDecl("  var")
                .appendDecl("    Foo: Integer;")
                .appendDecl("    Bar: Integer;")
                .appendDecl("  var")
                .appendDecl("    Baz: Integer;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }
}
