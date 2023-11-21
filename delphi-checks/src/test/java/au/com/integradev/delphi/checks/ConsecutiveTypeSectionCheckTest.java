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

class ConsecutiveTypeSectionCheckTest {
  @Test
  void testConsecutiveInterfaceTypeSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveTypeSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = Integer;")
                .appendDecl("  TBar = string;")
                .appendDecl("type // Noncompliant")
                .appendDecl("  TBaz = TObject;"))
        .verifyIssues();
  }

  @Test
  void testConsecutiveLocalTypeSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveTypeSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("type")
                .appendImpl("  TFlorp = TObject;")
                .appendImpl("type // Noncompliant")
                .appendImpl("  TFlarp = TObject;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer;")
                .appendImpl("  Bar: Integer;")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNonConsecutiveLocalTypeSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveTypeSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("type")
                .appendImpl("  TFlorp = TObject;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer;")
                .appendImpl("  Bar: Integer;")
                .appendImpl("type")
                .appendImpl("  TFlarp = TObject;")
                .appendImpl("begin")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testConsecutiveObjectTypeSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveTypeSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  type")
                .appendDecl("    TFlarp = Integer;")
                .appendDecl("  type // Noncompliant")
                .appendDecl("    TFlorp = Single;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testNonConsecutiveObjectTypeSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveTypeSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  type")
                .appendDecl("    TFlarp = Integer;")
                .appendDecl("  const")
                .appendDecl("    CFleep = 5;")
                .appendDecl("  type")
                .appendDecl("    TFlorp = Single;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testConsecutiveNestedInterfaceTypeSectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ConsecutiveTypeSectionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  type")
                .appendDecl("    TFlarp = Integer;")
                .appendDecl("  type // Noncompliant")
                .appendDecl("    TFlorp = Single;")
                .appendDecl("  end;")
                .appendDecl("type // Noncompliant")
                .appendDecl("  TBaz = TObject;"))
        .verifyIssues();
  }
}
