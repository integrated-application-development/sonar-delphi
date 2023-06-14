/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;

class MemoryManagementCheckTest {
  private static DelphiCheck createCheck() {
    MemoryManagementCheck check = new MemoryManagementCheck();
    check.memoryFunctions = "Test.TMemory.Manage<T>";
    check.whitelist = "CreateNew";
    return check;
  }

  @Test
  void testRequiresMemoryManagementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = class(TObject)")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TBar)")
                .appendDecl("    constructor Create;")
                .appendDecl("    function Clone(X: Integer): TFoo;")
                .appendDecl("    function Clone(X: String): TBar;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo.Create;")
                .appendImpl("  Foo := Foo.Clone(12345);")
                .appendImpl("  Bar := Foo.Clone('12345');")
                .appendImpl("end;"))
        .verifyIssueOnLine(21, 22, 23);
  }

  @Test
  void testNestedUnmanagedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(IBar)")
                .appendDecl("    constructor Create;")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendImpl("function SomeFunction(Foo: TFoo): TFoo;")
                .appendImpl("begin")
                .appendImpl("  Result := Foo;")
                .appendImpl("end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := SomeFunction(TFoo.Create);")
                .appendImpl("end;"))
        .verifyIssueOnLine(21);
  }

  @Test
  void testInheritedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("constructor TFoo.Create;")
                .appendImpl("begin")
                .appendImpl("  inherited Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCreateOnSelfShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("    constructor Create; override;")
                .appendDecl("    constructor Create(Arg: String); override;")
                .appendDecl("  end;")
                .appendImpl("constructor TFoo.Create;")
                .appendImpl("begin")
                .appendImpl("  Create('String');")
                .appendImpl("  Self.Create('String');")
                .appendImpl("end;")
                .appendImpl("constructor TFoo.Create(Arg: String);")
                .appendImpl("begin")
                .appendImpl("  // Do nothing")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testWhitelistedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    constructor CreateNew;")
                .appendDecl("  end;")
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  TFoo.CreateNew;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMemoryManagedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(IBar)")
                .appendDecl("    constructor Create;")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TMemory = class")
                .appendDecl("    class function Manage<T>(Obj: T): T;")
                .appendDecl("  end;")
                .appendImpl("function TMemory.Manage<T>(Obj: T): T;")
                .appendImpl("begin")
                .appendImpl("  // Memory management voodoo")
                .appendImpl("  Result := Obj;")
                .appendImpl("end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := TMemory.Manage(TFoo.Create);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testMemoryManagedWithCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(IBar)")
                .appendDecl("    constructor Create;")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TMemory = class")
                .appendDecl("    class function Manage<T>(Obj: T): T;")
                .appendDecl("  end;")
                .appendImpl("function TMemory.Manage<T>(Obj: T): T;")
                .appendImpl("begin")
                .appendImpl("  // Memory management voodoo")
                .appendImpl("  Result := Obj;")
                .appendImpl("end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  Foo := TMemory.Manage(TFoo.Create as TObject);")
                .appendImpl("  Foo := TMemory.Manage(TObject(TFoo.Create));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRaisingExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  ESpookyError = class(Exception)")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  raise ESpookyError.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceAssignmentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IBar = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TObject, IBar)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: IBar;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceAssignmentWithCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IBar = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TObject, IBar)")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: IBar;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo.Create as IBar;")
                .appendImpl("  Foo := IBar(TFoo.Create);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceArgumentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IBar = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure Foo;")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TObject, IBar)")
                .appendDecl("    procedure Baz(Bar: IBar);")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  Foo.Baz(TFoo.Create);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceArgumentWithCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  IBar = interface")
                .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
                .appendDecl("    procedure Baz(Bar: IBar);")
                .appendDecl("  end;")
                .appendDecl("  TFoo = class(TObject, IBar)")
                .appendDecl("    procedure Baz(Bar: IBar);")
                .appendDecl("  end;")
                .appendImpl("procedure Test(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  Foo.Baz(IBar(TFoo.Create));")
                .appendImpl("  Foo.Baz(TFoo.Create as IBar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRecordConstructorsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = record")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonConstructorsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(IBar)")
                .appendDecl("    procedure SomeProcedure;")
                .appendDecl("    function SomeFunction: TFoo;")
                .appendDecl("    function Clone: Integer;")
                .appendDecl("  end;")
                .appendImpl("function Clone: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := 42;")
                .appendImpl("end;")
                .appendImpl("procedure Test(Foo: TFoo);")
                .appendImpl("begin")
                .appendImpl("  Foo.SomeProcedure;")
                .appendImpl("  Foo.SomeFunction;")
                .appendImpl("  Foo.Clone;")
                .appendImpl("  Clone;")
                .appendImpl("  Foo.ProcedureThatDoesNotExist;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNestedInObscureProceduralUsageShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(createCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(IBar)")
                .appendDecl("    constructor Create;")
                .appendDecl("  end;")
                .appendDecl("  TProcType = procedure(Argument: TFoo);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  ProcArray: array of TProcType;")
                .appendImpl("begin")
                .appendImpl("  ProcArray[0](TFoo.Create);")
                .appendImpl("end;"))
        .verifyIssueOnLine(17);
  }
}
