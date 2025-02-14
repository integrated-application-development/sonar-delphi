/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

class DestructorWithoutInheritedCheckTest {
  @Test
  void testDestructorWithInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new DestructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestDestructor = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    destructor Destroy; override;")
                .appendDecl("  end;")
                .appendImpl("destructor TTestDestructor.Destroy;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testDestructorWithoutInheritedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new DestructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestDestructor = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    destructor Destroy; override;")
                .appendDecl("  end;")
                .appendImpl("destructor TTestDestructor.Destroy; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testClassDestructorShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new DestructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestDestructor = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    class destructor Destroy; override;")
                .appendDecl("  end;")
                .appendImpl("class destructor TTestDestructor.Destroy;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testDestructorLikeWithInheritedShouldNotAddIssue() {
    var check = new DestructorWithoutInheritedCheck();
    check.destructorLikes = "Foo,Bar";

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestDestructorLike = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure Foo; override;")
                .appendDecl("    procedure Bar; override;")
                .appendDecl("  end;")
                .appendImpl("procedure TTestDestructorLike.Foo;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;")
                .appendImpl("procedure TTestDestructorLike.Bar;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testDestructorLikeWithoutInheritedShouldAddIssue() {
    var check = new DestructorWithoutInheritedCheck();
    check.destructorLikes = "Foo,Bar";

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestDestructorLike = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure Foo; override;")
                .appendDecl("    procedure Bar; override;")
                .appendDecl("  end;")
                .appendImpl("procedure TTestDestructorLike.Foo; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;")
                .appendImpl("procedure TTestDestructorLike.Bar; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testDestructorLikeWithoutInheritedOrOverrideShouldNotAddIssue() {
    var check = new DestructorWithoutInheritedCheck();
    check.destructorLikes = "Foo";

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestDestructorLike = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendImpl("procedure TTestDestructorLike.Foo;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRandomMethodWithoutInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new DestructorWithoutInheritedCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure Bar; override;")
                .appendDecl("  end;")
                .appendImpl("procedure TTestDestructorLike.Bar;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('do something');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
