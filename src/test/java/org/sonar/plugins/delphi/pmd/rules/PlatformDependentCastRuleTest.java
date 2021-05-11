package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

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
            .appendImpl("  Ptr := Pointer(0);")
            .appendImpl("  Nat := NativeInt(0);")
            .appendImpl("  Ptr := Pointer($0);")
            .appendImpl("  Nat := NativeInt($0);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PlatformDependentCastRule"));
  }

  @Test
  void testStringCastsShouldNotAddIssue() {
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
}
