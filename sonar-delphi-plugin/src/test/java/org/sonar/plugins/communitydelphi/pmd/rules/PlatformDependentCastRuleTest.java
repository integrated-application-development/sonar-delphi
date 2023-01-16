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
package org.sonar.plugins.communitydelphi.pmd.rules;

import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class PlatformDependentCastRuleTest extends BasePmdRuleTest {
  @Test
  void testPointerIntegerCastsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Ptr: Pointer;")
            .appendImpl("begin")
            .appendImpl("  Int := Integer(Ptr);")
            .appendImpl("  Ptr := Pointer(Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 7));
  }

  @Test
  void testObjectIntegerCastsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Int := Integer(Obj);")
            .appendImpl("  Obj := TObject(Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 7));
  }

  @Test
  void testInterfaceIntegerCastsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Intf: IInterface;")
            .appendImpl("begin")
            .appendImpl("  Int := Integer(Intf);")
            .appendImpl("  Intf := IInterface(Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 7));
  }

  @Test
  void testNativeIntIntegerCastsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Int := Integer(Nat);")
            .appendImpl("  Nat := NativeInt(Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 7));
  }

  @Test
  void testNativeIntPointerCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Ptr: Pointer;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Ptr := Pointer(Nat);")
            .appendImpl("  Nat := NativeInt(Ptr);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testNativeIntObjectCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject(Nat);")
            .appendImpl("  Nat := NativeInt(Obj);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testNativeIntInterfaceCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Intf: IInterface;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Intf := IInterface(Nat);")
            .appendImpl("  Nat := NativeInt(Intf);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testIntegerLiteralCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Ptr: Pointer;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Ptr := Pointer(0);")
            .appendImpl("  Nat := NativeInt(0);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testHexadecimalLiteralCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Ptr: Pointer;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Ptr := Pointer($0);")
            .appendImpl("  Nat := NativeInt($0);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testBinaryLiteralCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Ptr: Pointer;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Ptr := Pointer(%0);")
            .appendImpl("  Nat := Pointer(%0);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testTObjectStringCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("  Str: String;")
            .appendImpl("begin")
            .appendImpl("  Str := String(Obj);")
            .appendImpl("  Obj := TObject(Str);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testTObjectRecordCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testRecordIntegerCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testIntegerStringCastsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Str: String;")
            .appendImpl("begin")
            .appendImpl("  Str := String(Int);")
            .appendImpl("  Int := Integer(Str);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 7));
  }

  @Test
  void testNativeIntStringCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Str: String;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Str := String(Nat);")
            .appendImpl("  Nat := NativeInt(Str);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testIntegerArrayCastsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Arr: TArray<Byte>;")
            .appendImpl("begin")
            .appendImpl("  Arr := TArray<Byte>(Int);")
            .appendImpl("  Int := Integer(Arr);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 7));
  }

  @Test
  void testNativeIntArrayCastsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Arr: TArray<Byte>;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Arr := TArray<Byte>(Nat);")
            .appendImpl("  Nat := NativeInt(Arr);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testTypeTypeCastsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 8))
        .areExactly(1, ruleKeyAtLine("PlatformDependentCastRule", builder.getOffset() + 9));
  }
}
