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

class UnusedFieldCheckTest {
  @Test
  void testUnusedPublicFieldShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedFieldCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("public")
                .appendDecl("  Bar: Integer; // Noncompliant")
                .appendDecl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedPublicFieldWithAttributeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedFieldCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("public")
                .appendDecl("  [Baz]")
                .appendDecl("  Bar: Integer;")
                .appendDecl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedGroupedPublicFieldsWithAttributeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedFieldCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("public")
                .appendDecl("  [Flarp]")
                .appendDecl("  Bar, Baz: Integer;")
                .appendDecl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedInRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedFieldCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("public")
                .appendDecl("  Bar: Integer;")
                .appendDecl("end;")
                .appendImpl("procedure Baz(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  Foo.Bar := 0;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedPublishedFieldShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedFieldCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("  Bar: Integer;")
                .appendDecl("end;")
                .appendImpl("procedure Baz(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  Foo.Bar := 0;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
