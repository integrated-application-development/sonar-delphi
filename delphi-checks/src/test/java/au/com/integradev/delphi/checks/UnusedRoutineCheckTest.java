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

class UnusedRoutineCheckTest {
  @Test
  void testUnusedRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedRecursiveRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  Foo;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedRoutineWithAttributeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("[Bar]")
                .appendDecl("procedure Foo; // Noncompliant")
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUsedRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("initialization")
                .appendImpl("  Foo;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedDeclaredRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo; // Noncompliant")
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedRecursiveDeclaredRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo; // Noncompliant")
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  Foo;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedMemberRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    procedure Foo; // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Foo;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedMemberRoutineWithAttributeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    [MyAttr]")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Foo;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedRecursiveMemberRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    procedure Foo; // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Foo;")
                .appendImpl("begin")
                .appendImpl("  Foo;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedConstructorShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    constructor Create; // Noncompliant")
                .appendDecl("  end;")
                .appendImpl("constructor TFoo.Create;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedConstructorWithMissingImplementationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    constructor Create; // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedForbiddenConstructorShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("constructor TFoo.Create;")
                .appendImpl("begin")
                .appendImpl("  raise Exception.Create('Do not use this constructor.');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedMemberRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendImpl("procedure TFoo.Foo;")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;")
                .appendImpl("var")
                .appendImpl("  F: TFoo;")
                .appendImpl("initialization")
                .appendImpl("  F.Foo;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedOverrideRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    procedure Foo; override;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testMessageRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    procedure Foo(var Message: TMessage); message 123;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonCallableRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  public")
                .appendDecl("    class constructor Create;")
                .appendDecl("    class destructor Destroy;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testRegisterRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Register;")
                .appendImpl("procedure Register;")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedInterfaceImplementationRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IFoo = interface")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TInterfacedObject, IFoo)")
                .appendDecl("  public")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedAttributeConstructorsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Foo = class(TCustomAttribute)")
                .appendDecl("  public")
                .appendDecl("    constructor Create; overload;")
                .appendDecl("    constructor Create(Bar: Integer); overload;")
                .appendDecl("    constructor Create(Bar: string); overload;")
                .appendDecl("    constructor Create(Bar: Integer; Baz: Integer); overload;")
                .appendDecl("  end;")
                .appendDecl("  [Foo]")
                .appendDecl("  [Foo(5)]")
                .appendDecl("  [Foo('hello')]")
                .appendDecl("  [Foo(10, 15)]")
                .appendDecl("  TBar = class(TObject)")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedAttributeConstructorsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  Foo = class(TCustomAttribute)")
                .appendDecl("  public")
                .appendDecl("    constructor Create; overload;")
                .appendDecl("    constructor Create(Bar: Integer); overload;")
                .appendDecl("    constructor Create(Bar: string); overload; // Noncompliant")
                .appendDecl("    constructor Create(Bar: Integer; Baz: Integer); overload;")
                .appendDecl("  end;")
                .appendDecl("  [Foo]")
                .appendDecl("  [Foo(5)]")
                .appendDecl("  [Foo(10, 15)]")
                .appendDecl("  TBar = class(TObject)")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedApiPropertyWithExcludeApiShouldNotAddIssue() {
    var check = new UnusedRoutineCheck();
    check.excludeApi = true;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("public")
                .appendDecl("  procedure Bar;")
                .appendDecl("end;")
                .appendDecl("")
                .appendDecl("procedure Baz;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedNonPublicRoutineWithExcludeApiShouldAddIssue() {
    var check = new UnusedRoutineCheck();
    check.excludeApi = true;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type TFoo = class")
                .appendDecl("private")
                .appendDecl("  procedure Bar; // Noncompliant")
                .appendDecl("protected")
                .appendDecl("  procedure Baz; // Noncompliant")
                .appendDecl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnusedImplementationRoutineWithExcludeApiShouldAddIssue() {
    var check = new UnusedRoutineCheck();
    check.excludeApi = true;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type TFoo = class")
                .appendImpl("public")
                .appendImpl("  procedure Bar; // Noncompliant")
                .appendImpl("end;")
                .appendImpl("")
                .appendImpl("procedure Baz; // Noncompliant")
                .appendImpl("begin")
                .appendImpl("  // do nothing")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
