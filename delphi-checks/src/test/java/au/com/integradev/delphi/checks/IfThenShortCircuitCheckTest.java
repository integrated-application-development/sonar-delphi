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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IfThenShortCircuitCheckTest {
  private DelphiTestUnitBuilder builder;

  @BeforeEach
  void setup() {
    builder =
        new DelphiTestUnitBuilder()
            .appendDecl("function IfThen(")
            .appendDecl("  Condition: Boolean;")
            .appendDecl("  IfTrue: String;")
            .appendDecl("  IfFalse: String")
            .appendDecl("): String;");
  }

  @Test
  void testNilNotEqualComparisonWithAccessShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IfThenShortCircuitCheck())
        .onFile(
            builder
                .appendImpl("function Foo(Bar: TObject): String;")
                .appendImpl("begin")
                .appendImpl("  Result := IfThen(Bar <> nil, Bar.ToString, 'Baz'); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNilEqualComparisonWithAccessShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IfThenShortCircuitCheck())
        .onFile(
            builder
                .appendImpl("function Foo(Bar: TObject): String;")
                .appendImpl("begin")
                .appendImpl("  Result := IfThen(nil = Bar, 'Baz', Bar.ToString); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testAssignedCheckWithAccessShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IfThenShortCircuitCheck())
        .onFile(
            builder
                .appendImpl("function Foo(Bar: TObject): String;")
                .appendImpl("begin")
                .appendImpl(
                    "  Result := IfThen(Assigned(Bar), Bar.ToString, 'Baz'); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNilNotEqualComparisonWithoutAccessShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IfThenShortCircuitCheck())
        .onFile(
            builder
                .appendImpl("function Foo(Bar: TObject): String;")
                .appendImpl("begin")
                .appendImpl("  Result := IfThen(Bar <> nil, 'Flarp', 'Baz');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNilEqualComparisonWithoutAccessShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IfThenShortCircuitCheck())
        .onFile(
            builder
                .appendImpl("function Foo(Bar: TObject): String;")
                .appendImpl("begin")
                .appendImpl("  Result := IfThen(nil = Bar, 'Baz', 'Flarp');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAssignedCheckWithoutAccessShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IfThenShortCircuitCheck())
        .onFile(
            builder
                .appendImpl("function Foo(Bar: TObject): String;")
                .appendImpl("begin")
                .appendImpl("  Result := IfThen(Assigned(Bar), 'Flarp', 'Baz');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIfThenWithWrongNumberOfArgumentsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IfThenShortCircuitCheck())
        .onFile(
            builder
                .appendImpl("function Foo(Bar: TObject): String;")
                .appendImpl("begin")
                .appendImpl("  Result := IfThen(Assigned(Bar), Bar.ToString);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
