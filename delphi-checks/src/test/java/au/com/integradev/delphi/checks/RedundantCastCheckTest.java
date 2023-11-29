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

class RedundantCastCheckTest {
  @Test
  void testNoCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendDecl("  TBar = class (TFoo)")
                .appendDecl("  end;")
                .appendImpl("procedure Foo(Bar: TBar);")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRequiredCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendDecl("  TBar = class (TFoo)")
                .appendDecl("  end;")
                .appendImpl("procedure Foo(Foo: TFoo);")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar := TBar(Foo);")
                .appendImpl("  Bar := Foo as TBar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRedundantCastShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendImpl("procedure Foo(Foo: TFoo);")
                .appendImpl("var")
                .appendImpl("  Foo2: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo2 := TFoo(Foo); // Noncompliant")
                .appendImpl("  Foo2 := Foo as TFoo; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRedundantCastWithConstructorShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := TFoo(TFoo.Create); // Noncompliant")
                .appendImpl("  Foo := TFoo.Create as TFoo; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRedundantCastWithStringKeywordShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Arg: String);")
                .appendImpl("var")
                .appendImpl("  Str: String;")
                .appendImpl("begin")
                .appendImpl("  Str := String(Arg); // Noncompliant")
                .appendImpl("  Str := Arg as String; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRedundantCastWithFileKeywordShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Arg: file);")
                .appendImpl("var")
                .appendImpl("  FileVar: file;")
                .appendImpl("begin")
                .appendImpl("  FileVar := file(Arg); // Noncompliant")
                .appendImpl("  FileVar := Arg as file; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRedundantCastWithAmbiguousRoutineCallShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("function GetString: String;")
                .appendImpl("procedure Foo(Arg: String);")
                .appendImpl("var")
                .appendImpl("  Str: String;")
                .appendImpl("begin")
                .appendImpl("  Str := String(GetString); // Noncompliant")
                .appendImpl("  Str := GetString as String; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testTClassToTObjectShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Clazz: TClass;")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject(Clazz);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnknownTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class")
                .appendDecl("  end;")
                .appendImpl("function Foo(Foo: TFoo): TFoo;")
                .appendImpl("var")
                .appendImpl("  Unknown: TUnknown;")
                .appendImpl("begin")
                .appendImpl("  Result := TBar(Unknown);")
                .appendImpl("  Result := Foo as TUnknown;")
                .appendImpl("  Result := TUnknown(Foo);")
                .appendImpl("  Result := Unknown as TFoo;")
                .appendImpl("  Result := Unknown as TUnknown;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  // See: https://github.com/integrated-application-development/sonar-delphi/issues/105
  @Test
  void testIssue105ShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Test: Integer;")
                .appendImpl("var")
                .appendImpl("  C: Cardinal;")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := Integer(C xor C) and not I;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
