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

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class PlatformDependentTruncationCheckTest extends CheckTest {
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

    assertIssues().areNot(ruleKey("PlatformDependentTruncationRule"));
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

    assertIssues().areNot(ruleKey("PlatformDependentTruncationRule"));
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

    assertIssues().areNot(ruleKey("PlatformDependentTruncationRule"));
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

    assertIssues().areNot(ruleKey("PlatformDependentTruncationRule"));
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

    assertIssues().areNot(ruleKey("PlatformDependentTruncationRule"));
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

    assertIssues().areNot(ruleKey("PlatformDependentTruncationRule"));
  }
}
