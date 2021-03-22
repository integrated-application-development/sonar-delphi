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
}
