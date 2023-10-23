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

class UnusedPropertyCheckTest {
  @Test
  void testUnusedPropertyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  FBar: Integer;")
                .appendDecl("public")
                .appendDecl("  property Bar: Integer read FBar; // Noncompliant")
                .appendDecl("end;"))
        .verifyIssues();
  }

  @Test
  void testUsedInMethodShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  FBar: Integer;")
                .appendDecl("public")
                .appendDecl("  property Bar: Integer read FBar;")
                .appendDecl("end;")
                .appendImpl("procedure Baz(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  Foo.Bar := 0;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedDefaultPropertyShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("public")
                .appendDecl("   property Items[Index: Integer]: TObject; default;")
                .appendDecl("end;")
                .appendImpl("function Baz(Foo: TFoo): TObject;")
                .appendImpl("begin")
                .appendImpl("  Result := Foo[0];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedPublishedPropertyShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  FBar: Integer;")
                .appendDecl("published")
                .appendDecl("  property Bar: Integer read FBar;")
                .appendDecl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedRedeclaredPropertyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  FBar: Integer;")
                .appendDecl("private")
                .appendDecl("  property Baz: Integer read FBar; // Noncompliant")
                .appendDecl("end;")
                .appendDecl("type TBar = class(TFoo)")
                .appendDecl("public")
                .appendDecl("  property Baz; // Noncompliant")
                .appendDecl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedRedeclaredPublishedPropertyShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  FBar: Integer;")
                .appendDecl("private")
                .appendDecl("  property Baz: Integer read FBar;")
                .appendDecl("end;")
                .appendDecl("type TBar = class(TFoo)")
                .appendDecl("published")
                .appendDecl("  property Baz;")
                .appendDecl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedRedeclaredPropertyShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  FBar: Integer;")
                .appendDecl("private")
                .appendDecl("  property Baz: Integer read FBar;")
                .appendDecl("end;")
                .appendDecl("type TBar = class(TFoo)")
                .appendDecl("public")
                .appendDecl("  property Baz;")
                .appendDecl("end;")
                .appendImpl("function Flarp(Bar: TBar): Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := Bar.Baz;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedRedeclaredPropertyWithUsedConcretePropertyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedPropertyCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  FBar: Integer;")
                .appendDecl("private")
                .appendDecl("  property Baz: Integer read FBar;")
                .appendDecl("end;")
                .appendDecl("type TBar = class(TFoo)")
                .appendDecl("public")
                .appendDecl("  property Baz; // Noncompliant")
                .appendDecl("end;")
                .appendImpl("function Flarp(Foo: TFoo): Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := Foo.Baz;")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
