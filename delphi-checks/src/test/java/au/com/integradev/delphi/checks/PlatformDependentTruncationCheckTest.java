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

class PlatformDependentTruncationCheckTest {
  private static final String VERSION_ALEXANDRIA = "VER350";
  private static final String VERSION_ATHENS = "VER360";

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testIntegerToNativeIntAssignmentShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Nat := Int;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testInt64ToNativeIntAssignmentShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  I64: Int64;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Nat := I64; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntToIntegerAssignmentShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Int := Nat; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntToI64AssignmentShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  I64: Int64;")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  I64 := Nat;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntToNativeIntAssignmentShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Nat1: NativeInt;")
                .appendImpl("  Nat2: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Nat1 := Nat2;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testIntegerArgumentToNativeIntParameterShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Bar(Nat: NativeInt);")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("begin")
                .appendImpl("  Bar(Int);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testInt64ArgumentToNativeIntParameterShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Bar(Nat: NativeInt);")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  I64: Int64;")
                .appendImpl("begin")
                .appendImpl("  Bar(I64); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntArgumentToIntegerParameterShouldAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Bar(Nat: NativeInt);")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  I64: Int64;")
                .appendImpl("begin")
                .appendImpl("  Bar(I64); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntArgumentToI64ParameterShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Bar(I64: Int64);")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Bar(Nat);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {VERSION_ALEXANDRIA, VERSION_ATHENS})
  void testNativeIntArgumentToNativeIntParameterShouldNotAddIssue(String versionSymbol) {
    CheckVerifier.newVerifier()
        .withCheck(new PlatformDependentTruncationCheck())
        .withCompilerVersion(CompilerVersion.fromVersionSymbol(versionSymbol))
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Bar(Nat: NativeInt);")
                .appendImpl("procedure Foo;")
                .appendImpl("var")
                .appendImpl("  Nat: NativeInt;")
                .appendImpl("begin")
                .appendImpl("  Bar(Nat);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
