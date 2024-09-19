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

class VariableInitializationCheckTest {
  @Test
  void testRecordCreateShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  FormatSettings: TFormatSettings;")
                .appendImpl("begin")
                .appendImpl("  FormatSettings := TFormatSettings.Create;")
                .appendImpl("  Foo(FormatSettings);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testVariableAssignedFromExternalVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("var")
                .appendDecl("  MyFormatSettings: TFormatSettings;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  FormatSettings: TFormatSettings;")
                .appendImpl("begin")
                .appendImpl("  FormatSettings := MyFormatSettings;")
                .appendImpl("  Foo(FormatSettings);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testVariableAssignedFromLocalInitializedVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  FormatSettings1: TFormatSettings;")
                .appendImpl("  FormatSettings2: TFormatSettings;")
                .appendImpl("begin")
                .appendImpl("  FormatSettings1 := TFormatSettings.Create;")
                .appendImpl("  FormatSettings2 := FormatSettings1;")
                .appendImpl("  Foo(FormatSettings2);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testVariableAssignedFromLocalUninitializedVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Int1: Integer;")
                .appendImpl("  Int2: Integer;")
                .appendImpl("begin")
                .appendImpl("  Int2 := Int1; // Noncompliant")
                .appendImpl("  Foo(Int2); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInlineVariableAssignedFromLocalInitializedVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var FormatSettings1 := TFormatSettings.Create;")
                .appendImpl("  var FormatSettings2 := FormatSettings1;")
                .appendImpl("  Foo(FormatSettings2);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInlineVariableAssignedFromLocalUninitializedVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var FormatSettings1: TFormatSettings;")
                .appendImpl("  var FormatSettings2 := FormatSettings1; // Noncompliant")
                .appendImpl("  Foo(FormatSettings2); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInlineConstAssignedFromLocalInitializedVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var FormatSettings1 := TFormatSettings.Create;")
                .appendImpl("  const FormatSettings2 = FormatSettings1;")
                .appendImpl("  Foo(FormatSettings2);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInlineConstAssignedFromLocalUninitializedVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var FormatSettings1: TFormatSettings;")
                .appendImpl("  const FormatSettings2 = FormatSettings1; // Noncompliant")
                .appendImpl("  Foo(FormatSettings2); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInlineRecordCreateShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var FormatSettings: TFormatSettings;")
                .appendImpl("  FormatSettings := TFormatSettings.Create;")
                .appendImpl("  Foo(FormatSettings);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInlineRecordCreateWithImmediateInitializationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var FormatSettings := TFormatSettings.Create;")
                .appendImpl("  const FormatSettings2 = TFormatSettings.Create;")
                .appendImpl("  Foo(FormatSettings);")
                .appendImpl("  Foo(FormatSettings2);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNestedRecordCreateShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  FormatSettings: TFormatSettings;")
                .appendImpl("begin")
                .appendImpl("  if True then begin")
                .appendImpl("    if True then begin")
                .appendImpl("      FormatSettings := TFormatSettings.Create;")
                .appendImpl("    end;")
                .appendImpl("    Foo(FormatSettings);")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("    System.SysUtils")
                .appendImpl("  ;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  FormatSettings: TFormatSettings;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('Foo');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testVariableWithoutInitializationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  FormatSettings: TFormatSettings;")
                .appendImpl("begin")
                .appendImpl("  Foo(FormatSettings); // Noncompliant")
                .appendImpl("  Foo(FormatSettings);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInlineVariableWithoutInitializationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  var FormatSettings: TFormatSettings;")
                .appendImpl("  Foo(FormatSettings); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMultipleInlineVariablesWithoutInitializationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("    System.SysUtils")
                .appendDecl("  ;")
                .appendDecl("procedure Foo(FormatSettings: TFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl(
                    "  var FormatSettings, FormatSettings2, FormatSettings3: TFormatSettings;")
                .appendImpl("  Foo(FormatSettings); // Noncompliant")
                .appendImpl("  Foo(FormatSettings2); // Noncompliant")
                .appendImpl("  Foo(FormatSettings3); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRecordWithPartialInitializationShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("    I64: Int64;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Int := 123;")
                .appendImpl("  Foo(Bar); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRecordWithInitializationOfAllUnmanagedFieldsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("    Str: String;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Int := 123;")
                .appendImpl("  Foo(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUninitializedVariantRecordShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("    case Boolean of")
                .appendDecl("      True: (Int2: Integer);")
                .appendDecl("      False: (Int3: Integer);")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Foo(Bar); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testPartiallyInitializedVariantRecordShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("    case Boolean of")
                .appendDecl("      True: (Int2: Integer);")
                .appendDecl("      False: (Int3: Integer);")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Int := 123;")
                .appendImpl("  Foo(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUninitializedVariantRecordWithOnlyVariantFieldsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    case Boolean of")
                .appendDecl("      True: (Int2: Integer);")
                .appendDecl("      False: (Int3: Integer);")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Foo(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRecordFieldAssignedLocalVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("    Str: String;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Int := 123;")
                .appendImpl("  Bar.Int := Int;")
                .appendImpl("  Foo(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRecordFieldAssignedUninitializedLocalVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("    Str: String;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Int: Integer;")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Int := Int; // Noncompliant")
                .appendImpl("  Foo(Bar.Int); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRecordWithManagedFieldsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    I: IInterface;")
                .appendDecl("    S: String;")
                .appendDecl("    A: array of String;")
                .appendDecl("    V: Variant;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Foo(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUninitializedRecordFieldAssignedToVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("  Int: Integer;")
                .appendImpl("begin")
                .appendImpl("  Int := Bar.Int; // Noncompliant")
                .appendImpl("  Foo(Int); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testPointerToUninitializedRecordShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  PFormatSettings = ^TFormatSettings;")
                .appendDecl("procedure Foo(FormatSettings: PFormatSettings);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  FormatSettings: TFormatSettings;")
                .appendImpl("begin")
                .appendImpl("  Foo(@(FormatSettings));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testForLoopVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to 100 do begin")
                .appendImpl("    Foo(I);")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInitializedInRepeatLoopShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  repeat")
                .appendImpl("    I := 0;")
                .appendImpl("  until I = 0;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSelfReferencingAssignmentShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  I := I + 1; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testAssigneeNestedReferenceShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(Arr: array of Integer);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  Arr[I] := 1; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testPassedAsVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure GetInt(var Int: Integer);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  GetInt(I);")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testTextFilePassedAsVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  T: TextFile;")
                .appendImpl("begin")
                .appendImpl("  AssignFile(T, 'Foo');")
                .appendImpl("  WriteLn(T, 'Bar');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPassedAsOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure GetInt(out Int: Integer);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  GetInt(I);")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRecordFieldPassedAsVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Baz: Integer;")
                .appendDecl("  end;")
                .appendDecl("procedure GetInt(var Int: Integer);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  GetInt(Bar.Baz);")
                .appendImpl("  Foo(Bar.Baz);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPassedToProcVarShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestProc = reference to procedure(Int: Integer);")
                .appendImpl("procedure Test(Proc: TTestProc);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  Proc(I); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testPassedToProcVarOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestProc = reference to procedure(out Int: Integer);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(Proc: TTestProc);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  Proc(I);")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testFailOnUpgradeVarPassedToArrayProcVarOutParameterShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestProc = reference to procedure(out Int: Integer);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(ProcArray: array of TTestProc);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  ProcArray[0](I); // Noncompliant")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testPassedToProcVarVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TTestProc = reference to procedure(var Int: Integer);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(Proc: TTestProc);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  Proc(I);")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPartiallyInitializedRecordInQualifiedReferenceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Baz: Integer;")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendDecl("function Oof(Int: Integer): Integer;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("  Blam: Integer;")
                .appendImpl("begin")
                .appendImpl("  Bar.Baz := 123;")
                .appendImpl("  Foo(Bar.Baz);")
                .appendImpl("  Blam := Oof(Bar.Baz);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testPassedAsPointerShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  PInteger = ^Integer;")
                .appendDecl("procedure GetInt(I: PInteger);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  GetInt(@I);")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUninitializedRecordFieldPassedAsUntypedVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Int: Integer;")
                .appendDecl("  end;")
                .appendDecl("procedure GetInt(var Int);")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  GetInt(Bar.Int);")
                .appendImpl("  Foo(Bar.Int);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testHardCastedPointerPassedAsVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  PInteger = ^Integer;")
                .appendDecl("procedure GetInt(var P: PInteger);")
                .appendDecl("procedure Foo(P: Pointer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  P: Pointer;")
                .appendImpl("begin")
                .appendImpl("  GetInt(PInteger(P));")
                .appendImpl("  Foo(P);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testHardCastedAssignmentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(P: Pointer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  P: Pointer;")
                .appendImpl("begin")
                .appendImpl("  Integer(P) := 123;")
                .appendImpl("  Foo(P);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSelfReferencingAssignmentPassedAsVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure GetInt(var I: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  I := GetInt(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSelfReferencingAssignmentPassedAsOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure GetInt(out I: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  I := GetInt(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUninitializedObjectWithAssignedCheckShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Bar;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  if Assigned(Foo) then begin // Noncompliant")
                .appendImpl("    Foo.Bar;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUninitializedVariableUseAfterSizeOfShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(I: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  Size: Integer;")
                .appendImpl("begin")
                .appendImpl("  Size := SizeOf(I);")
                .appendImpl("  Foo(I); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnknownRoutineArgumentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  UnknownRoutine(Foo(I));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUninitializedObjectWithFreeAndNilShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .withSearchPathUnit(createSysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  System.SysUtils;")
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Bar;")
                .appendDecl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TFoo;")
                .appendImpl("begin")
                .appendImpl("  FreeAndNil(Foo); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testUnionVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(I: Integer);")
                .appendImpl("procedure Test(P: Pointer);")
                .appendImpl("var")
                .appendImpl("  I: Integer absolute P;")
                .appendImpl("begin")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInitializedInSubroutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(I: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("  var")
                .appendImpl("    I: Integer;")
                .appendImpl("  procedure Sub;")
                .appendImpl("  begin")
                .appendImpl("    I := 123;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Sub;")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUsedBeforeInitializedInSubroutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(I: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("  var")
                .appendImpl("    I: Integer;")
                .appendImpl("  procedure Sub;")
                .appendImpl("  begin")
                .appendImpl("    I := 123;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Foo(I); // Noncompliant")
                .appendImpl("  Sub;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRecordAfterAnyRoutineCalledShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Baz: Integer;")
                .appendDecl("    procedure Reset;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Reset;")
                .appendImpl("  Foo(Bar.Baz);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRecordAfterAnyPropertyReferenceShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("    Baz: Integer;")
                .appendDecl("    FBlam: Integer;")
                .appendDecl("    property Blam: Integer write FBlam;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Blam := 123;")
                .appendImpl("  Foo(Bar.Baz);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAssigningToImaginaryFieldOnUninitializedVariableShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: Integer;")
                .appendImpl("  Bar: Integer;")
                .appendImpl("begin")
                .appendImpl("  Foo := 123;")
                .appendImpl("  Bar.Baz := Foo; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testRecordClassVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("  private class var")
                .appendDecl("      Baz: TBar;")
                .appendDecl("  public")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Flarp := 123;")
                .appendImpl("  Foo(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRecordSelfReferentialClassVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TBar = record")
                .appendDecl("  private class var")
                .appendDecl("      Baz: TBar;")
                .appendDecl("  public")
                .appendDecl("    Flarp: Integer;")
                .appendDecl("  end;")
                .appendDecl("procedure Foo(Bar: TBar);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Bar: TBar;")
                .appendImpl("begin")
                .appendImpl("  Bar.Flarp := 123;")
                .appendImpl("  Foo(Bar);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testVarParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(var I: Integer);")
                .appendImpl("begin")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOutParameterShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(out I: Integer);")
                .appendImpl("begin")
                .appendImpl("  Foo(I); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInitializedOutParameterShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(out I: Integer);")
                .appendImpl("begin")
                .appendImpl("  I := 123;")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOutParameterDeclaredInNestedRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test;")
                .appendImpl("  procedure Sub(out I: Integer);")
                .appendImpl("  begin")
                .appendImpl("    Foo(I); // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  Sub(I);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testOutParameterAccessedInNestedRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(out I: Integer);")
                .appendImpl("  procedure Sub;")
                .appendImpl("  begin")
                .appendImpl("    Foo(I); // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Sub;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testOutParameterInitializedInNestedRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VariableInitializationCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Int: Integer);")
                .appendImpl("procedure Test(out I: Integer);")
                .appendImpl("  procedure Sub;")
                .appendImpl("  begin")
                .appendImpl("    I := 123;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  Sub;")
                .appendImpl("  Foo(I);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  private static DelphiTestUnitBuilder createSysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("procedure FreeAndNil(var Obj); inline;");
  }
}
