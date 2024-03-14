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
import au.com.integradev.delphi.compiler.CompilerVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlatformDependentCastCheckTest {
  private static final String VERSION_ALEXANDRIA = "VER350";
  private static final String VERSION_ATHENS = "VER360";

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testPointerIntegerCastsShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Ptr: Pointer;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Ptr); // Noncompliant")
                .appendImpl("  Ptr := Pointer(Int); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testObjectIntegerCastsShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Obj); // Noncompliant")
                .appendImpl("  Obj := TObject(Int); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testInterfaceIntegerCastsShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Intf: IInterface;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Intf); // Noncompliant")
                .appendImpl("  Intf := IInterface(Int); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntIntegerCastsShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Nat); // Noncompliant")
                .appendImpl("  Nat := NativeInt(Int); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntPointerCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Ptr: Pointer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Ptr := Pointer(Nat);")
                .appendImpl("  Nat := NativeInt(Ptr);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntObjectCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Obj := TObject(Nat);")
                .appendImpl("  Nat := NativeInt(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntInterfaceCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Intf: IInterface;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Intf := IInterface(Nat);")
                .appendImpl("  Nat := NativeInt(Intf);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testIntegerLiteralCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Ptr: Pointer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Ptr := Pointer(0);")
                .appendImpl("  Nat := NativeInt(0);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testHexadecimalLiteralCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Ptr: Pointer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Ptr := Pointer($0);")
                .appendImpl("  Nat := NativeInt($0);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testBinaryLiteralCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Ptr: Pointer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Ptr := Pointer(%0);")
                .appendImpl("  Nat := Pointer(%0);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testTObjectStringCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("  Str: String;")
                .appendImpl("begin")
                .appendImpl("  Str := String(Obj);")
                .appendImpl("  Obj := TObject(Str);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testTObjectRecordCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TRecord = record")
                .appendDecl("  end;")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Obj: TObject;")
                .appendImpl("  Rec: TRecord;")
                .appendImpl("begin")
                .appendImpl("  Rec := TRecord(Obj);")
                .appendImpl("  Obj := TObject(Rec);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testRecordIntegerCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TRecord = record")
                .appendDecl("  end;")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Rec: TRecord;")
                .appendImpl("begin")
                .appendImpl("  Rec := TRecord(Obj);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testIntegerStringCastsShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Str: String;")
                .appendImpl("begin")
                .appendImpl("  Str := String(Int); // Noncompliant")
                .appendImpl("  Int := Integer(Str); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntStringCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Str: String;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Str := String(Nat);")
                .appendImpl("  Nat := NativeInt(Str);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testIntegerArrayCastsShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Arr: TArray<Byte>;")
                .appendImpl("begin")
                .appendImpl("  Arr := TArray<Byte>(Int); // Noncompliant")
                .appendImpl("  Int := Integer(Arr); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntArrayCastsShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Arr: TArray<Byte>;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Arr := TArray<Byte>(Nat);")
                .appendImpl("  Nat := NativeInt(Arr);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testStrongAliasCastsShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("type")
                .appendImpl("  MyNativeInt = type NativeInt;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Nat: MyNativeInt;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Nat); // Noncompliant")
                .appendImpl("  Nat := MyNativeInt(Int); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
