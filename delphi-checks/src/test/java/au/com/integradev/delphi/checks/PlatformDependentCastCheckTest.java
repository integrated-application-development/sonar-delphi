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

class PlatformDependentCastCheckTest {
  @Test
  void testPointerIntegerCastsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Ptr: Pointer;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Ptr);")
                .appendImpl("  Ptr := Pointer(Int);")
                .appendImpl("end;"))
        .verifyIssueOnLine(12, 13);
  }

  @Test
  void testObjectIntegerCastsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Obj: TObject;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Obj);")
                .appendImpl("  Obj := TObject(Int);")
                .appendImpl("end;"))
        .verifyIssueOnLine(12, 13);
  }

  @Test
  void testInterfaceIntegerCastsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Intf: IInterface;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Intf);")
                .appendImpl("  Intf := IInterface(Int);")
                .appendImpl("end;"))
        .verifyIssueOnLine(12, 13);
  }

  @Test
  void testNativeIntIntegerCastsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Nat);")
                .appendImpl("  Nat := NativeInt(Int);")
                .appendImpl("end;"))
        .verifyIssueOnLine(12, 13);
  }

  @Test
  void testNativeIntPointerCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testNativeIntObjectCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testNativeIntInterfaceCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testIntegerLiteralCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testHexadecimalLiteralCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testBinaryLiteralCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testTObjectStringCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testTObjectRecordCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testRecordIntegerCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testIntegerStringCastsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Str: String;")
                .appendImpl("begin")
                .appendImpl("  Str := String(Int);")
                .appendImpl("  Int := Integer(Str);")
                .appendImpl("end;"))
        .verifyIssueOnLine(12, 13);
  }

  @Test
  void testNativeIntStringCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testIntegerArrayCastsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Arr: TArray<Byte>;")
                .appendImpl("begin")
                .appendImpl("  Arr := TArray<Byte>(Int);")
                .appendImpl("  Int := Integer(Arr);")
                .appendImpl("end;"))
        .verifyIssueOnLine(12, 13);
  }

  @Test
  void testNativeIntArrayCastsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
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

  @Test
  void testTypeTypeCastsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentCastCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("type")
                .appendImpl("  MyNativeInt = type NativeInt;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Nat: MyNativeInt;")
                .appendImpl("begin")
                .appendImpl("  Int := Integer(Nat);")
                .appendImpl("  Nat := MyNativeInt(Int);")
                .appendImpl("end;"))
        .verifyIssueOnLine(14, 15);
  }
}
