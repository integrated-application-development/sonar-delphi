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

class InheritedMethodWithNoCodeCheckTest {
  @Test
  void testNoCodeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNoSemicolonShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testImplementationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  FMyField := 5;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testImplementationWithInheritedAtEndShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  FMyField := 5;")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testFalsePositiveImplementationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  FMyField := 5;")
                .appendImpl("  if MyBoolean then begin")
                .appendImpl("    inherited;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExplicitInheritedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited MyProcedure; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testExplicitInheritedWithArgumentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure(X: Integer; Y: String);")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(X: Integer; Y: String);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure(X: Integer; Y: String);")
                .appendImpl("begin")
                .appendImpl("  inherited MyProcedure(X, Y); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testExplicitInheritedWithMismatchedArgumentSizesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure(X: Integer; Y: String);")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(X: Integer; Y: String);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure(X: Integer; Y: String);")
                .appendImpl("begin")
                .appendImpl("  inherited MyProcedure(X);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExplicitInheritedWithMismatchedArgumentsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure(X: Integer; Y: String);")
                .appendDecl("    procedure MyProcedure(X: Boolean; Y: Boolean);")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure(X: Integer; Y: String);")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure(X: Integer; Y: String);")
                .appendImpl("begin")
                .appendImpl("  inherited MyProcedure(True, False);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testExplicitInheritedWithEmptyBracketsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited MyProcedure(); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testWrongExplicitInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("    procedure SomeOtherProcedure;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited SomeOtherProcedure;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testFunctionInheritedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    function MyFunction: Integer;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    function MyFunction: Integer;")
                .appendDecl("  end;")
                .appendImpl("function TChild.MyFunction: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := inherited MyFunction; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testFunctionQualifiedInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("    function MyFunction: TArray<Integer>;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("    function MyFunction: Integer;")
                .appendDecl("  end;")
                .appendImpl("function TChild.MyFunction: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := inherited MyFunction[0].GetValue;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIncreasedVisibilityShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("  protected")
                .appendDecl("    procedure IncreaseVisibilityProc;")
                .appendDecl("  public")
                .appendDecl("    procedure NotVirtualProc;")
                .appendDecl("    procedure VirtualProc; virtual;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("  public")
                .appendDecl("    procedure IncreaseVisibilityProc;")
                .appendDecl("    procedure NotVirtualProc; virtual;")
                .appendDecl("    procedure VirtualProc; reintroduce;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.IncreaseVisibilityProc;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("end;")
                .appendImpl("procedure TChild.NotVirtualProc;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("end;")
                .appendImpl("procedure TChild.VirtualProc;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInstanceRoutineOverridingVisibilityOfInheritedClassRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("  protected")
                .appendDecl("    class procedure MyProcedure; virtual;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("  public")
                .appendDecl("    procedure MyProcedure;")
                .appendDecl("  end;")
                .appendImpl("procedure TChild.MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testEmptyInstanceDestructorWithClassDestructorOfDifferentVisibilityShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("  protected")
                .appendDecl("    class destructor Destroy;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("  public")
                .appendDecl("    destructor Destroy; override;")
                .appendDecl("  end;")
                .appendImpl("destructor TChild.Destroy;")
                .appendImpl("begin")
                .appendImpl("  inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testEmptyInstanceConstructorWithClassConstructorOfDifferentVisibilityShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new InheritedMethodWithNoCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBase = class(TObject)")
                .appendDecl("  protected")
                .appendDecl("    class constructor Create;")
                .appendDecl("  end;")
                .appendDecl("  TChild = class(TBase)")
                .appendDecl("  public")
                .appendDecl("    constructor Create; override;")
                .appendDecl("  end;")
                .appendImpl("constructor TChild.Create;")
                .appendImpl("begin")
                .appendImpl("  inherited; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
