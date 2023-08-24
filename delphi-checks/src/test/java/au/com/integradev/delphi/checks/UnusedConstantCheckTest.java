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

class UnusedConstantCheckTest {
  @Test
  void testUsedInMethodShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("const")
                .appendImpl("  C_Foo = 0;")
                .appendImpl("begin")
                .appendImpl("  var Foo := C_Foo;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedInMethodShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("const")
                .appendImpl("  C_Foo = 0;")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testUsedInlineConstantShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  const C_Foo = 0;")
                .appendImpl("  var Foo := C_Foo;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedInlineConstantShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  const C_Foo = 0;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testUsedGlobalConstantShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  C_Foo = 0;")
                .appendImpl("function Foo: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := C_Foo;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedGlobalConstantShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("const")
                .appendDecl("  C_Foo = 0;"))
        .verifyIssueOnLine(6);
  }

  @Test
  void testUsedInArrayIndexTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("const")
                .appendDecl("  C_Foo = 0;")
                .appendDecl("var")
                .appendDecl("  FooArray: array[0..C_Foo] of Integer;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedConstSetsWithSubrangeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type TAlias = set of AnsiChar;")
                .appendImpl("procedure Bar(const CharSet: TAlias);")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure Foo(const Str: String);")
                .appendImpl("const")
                .appendImpl("  C_Foo = ['A'..'Z'];")
                .appendImpl("begin")
                .appendImpl("  Bar(C_Foo);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedConstSetsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedConstantCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type TAlias = set of AnsiChar;")
                .appendImpl("procedure Bar(const CharSet: TAlias);")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("procedure Foo(const Str: String);")
                .appendImpl("const")
                .appendImpl("  C_Foo = ['A','Z'];")
                .appendImpl("begin")
                .appendImpl("  Bar(C_Foo);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
