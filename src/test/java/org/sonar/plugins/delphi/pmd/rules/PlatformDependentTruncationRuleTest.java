package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class PlatformDependentTruncationRuleTest extends BasePmdRuleTest {
  @Test
  void testIntegerToNativeIntAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Nat := Int;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testInt64ToNativeIntAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  I64: Int64;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Nat := I64;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentTruncationRule", builder.getOffset() + 6));
  }

  @Test
  void testNativeIntToIntegerAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Int := Nat;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentTruncationRule", builder.getOffset() + 6));
  }

  @Test
  void testNativeIntToI64AssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  I64: Int64;")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  I64 := Nat;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testNativeIntToNativeIntAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Nat1: NativeInt;")
            .appendImpl("  Nat2: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Nat1 := Nat2;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testIntegerArgumentToNativeIntParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Bar(Nat: NativeInt);")
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Int: Integer;")
            .appendImpl("begin")
            .appendImpl("  Bar(Int);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testInt64ArgumentToNativeIntParameterShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Bar(Nat: NativeInt);")
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  I64: Int64;")
            .appendImpl("begin")
            .appendImpl("  Bar(I64);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentTruncationRule", builder.getOffset() + 5));
  }

  @Test
  void testNativeIntArgumentToIntegerParameterShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Bar(Nat: NativeInt);")
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  I64: Int64;")
            .appendImpl("begin")
            .appendImpl("  Bar(I64);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("PlatformDependentTruncationRule", builder.getOffset() + 5));
  }

  @Test
  void testNativeIntArgumentToI64ParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Bar(I64: Int64);")
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Bar(Nat);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testNativeIntArgumentToNativeIntParameterShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Bar(Nat: NativeInt);")
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Nat: NativeInt;")
            .appendImpl("begin")
            .appendImpl("  Bar(Nat);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
