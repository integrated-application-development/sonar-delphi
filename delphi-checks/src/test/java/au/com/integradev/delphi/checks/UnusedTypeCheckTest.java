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

class UnusedTypeCheckTest {
  @Test
  void testUnusedTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testUsedByFieldShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class // Noncompliant")
                .appendDecl("    Bar: TFoo;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testUsedInMemberMethodShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class // Noncompliant")
                .appendDecl("    procedure Baz;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Baz;")
                .appendImpl("begin")
                .appendImpl("  var Foo: TFoo;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUsedInMemberMethodParametersShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class // Noncompliant")
                .appendDecl("    procedure Baz(Foo: TFoo);")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Baz(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUsedInRoutineParametersShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendImpl("procedure Baz(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedInRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendImpl("procedure Baz;")
                .appendImpl("begin")
                .appendImpl("  var Foo: TFoo;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testHelperShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendDecl("  TFooHelper = class helper for TFoo")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedApiTypeWithExcludeApiShouldNotAddIssue() {
    var check = new UnusedTypeCheck();
    check.excludeApi = true;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("type TFoo = class")
                .appendDecl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedNestedApiTypeWithExcludeApiShouldNotAddIssue() {
    var check = new UnusedTypeCheck();
    check.excludeApi = true;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("  public type")
                .appendDecl("    TBar = class")
                .appendDecl("    end;")
                .appendDecl("  published type")
                .appendDecl("    TBaz = class")
                .appendDecl("    end;")
                .appendDecl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedNonPublicTypeWithExcludeApiShouldAddIssue() {
    var check = new UnusedTypeCheck();
    check.excludeApi = true;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private type")
                .appendDecl("  TBar = class end; // Noncompliant")
                .appendDecl("protected type")
                .appendDecl("  TBaz = class end; // Noncompliant")
                .appendDecl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedImplementationTypeWithExcludeApiShouldAddIssue() {
    var check = new UnusedTypeCheck();
    check.excludeApi = true;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type TFoo = class // Noncompliant")
                .appendImpl("  public type")
                .appendImpl("    TBar = class // Noncompliant")
                .appendImpl("    end;")
                .appendImpl("  published type")
                .appendImpl("    TBaz = class // Noncompliant")
                .appendImpl("    end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedTypeWithAttributeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedTypeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  [Bar]")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }
}
