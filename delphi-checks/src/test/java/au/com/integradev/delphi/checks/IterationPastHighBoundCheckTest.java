/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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

class IterationPastHighBoundCheckTest {
  @Test
  void testIterationToLengthMinusOneShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyArr: TArray<string>);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to Length(MyArr) - 1 do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationToHighShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyArr: TArray<string>);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to High(MyArr) do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationFromNonZeroShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyArr: TArray<string>);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 1 to Length(MyArr) do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationFromCustomBoundArrayStartingAtZeroShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type TCustomArray = array[1..6] of string;")
                .appendImpl("procedure Test(MyArr: TCustomArray);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to Length(MyArr) do begin // Noncompliant")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIterationFromCustomBoundArrayStartingAtNonZeroShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type TCustomArray = array[1..6] of string;")
                .appendImpl("procedure Test(MyArr: TCustomArray);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 1 to Length(MyArr) do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationFromNonZeroInlineVarShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyArr: TArray<string>);")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+1:33 to +1:33] << - 1>>")
                .appendImpl("  for var I := 1 to Length(MyArr) do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationToLengthShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyArr: TArray<string>);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to Length(MyArr) do begin // Noncompliant")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIterationToCountShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TMyCollection = class(TObject)")
                .appendImpl("    FCount: Integer;")
                .appendImpl("    property Count: Integer read FCount write FCount;")
                .appendImpl("  end;")
                .appendImpl("procedure Test(MyArr: TMyCollection);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to MyArr.Count do begin // Noncompliant")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIterationToInt64CountShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TMyCollection = class(TObject)")
                .appendImpl("    FCount: Int64;")
                .appendImpl("    property Count: Int64 read FCount write FCount;")
                .appendImpl("  end;")
                .appendImpl("procedure Test(MyArr: TMyCollection);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to MyArr.Count do begin // Noncompliant")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIterationToCountMinusOneShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TMyCollection = class(TObject)")
                .appendImpl("    FCount: Integer;")
                .appendImpl("    property Count: Integer read FCount write FCount;")
                .appendImpl("  end;")
                .appendImpl("procedure Test(MyArr: TMyCollection);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to MyArr.Count - 1 do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationToPredCountShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TMyCollection = class(TObject)")
                .appendImpl("    FCount: Integer;")
                .appendImpl("    property Count: Integer read FCount write FCount;")
                .appendImpl("  end;")
                .appendImpl("procedure Test(MyArr: TMyCollection);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to Pred(MyArr.Count) do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationToNestedCountShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TMyCollection = class(TObject)")
                .appendImpl("    FCount: Integer;")
                .appendImpl("    property Count: Integer read FCount write FCount;")
                .appendImpl("  end;")
                .appendImpl("  TMyWrapper = class(TObject)")
                .appendImpl("    FCollection: TMyCollection;")
                .appendImpl(
                    "    property Collection: TMyCollection read FCollection write FCollection;")
                .appendImpl("  end;")
                .appendImpl("procedure Test(Wrapper: TMyWrapper);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to Wrapper.Collection.Count do begin // Noncompliant")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testIterationToOtherNestedPropertyShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TMyCollection = class(TObject)")
                .appendImpl("    FLevel: Integer;")
                .appendImpl("    property Level: Integer read FLevel write FLevel;")
                .appendImpl("  end;")
                .appendImpl("  TMyWrapper = class(TObject)")
                .appendImpl("    FCollection: TMyCollection;")
                .appendImpl(
                    "    property Collection: TMyCollection read FCollection write FCollection;")
                .appendImpl("  end;")
                .appendImpl("procedure Test(Wrapper: TMyWrapper);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to Wrapper.Collection.Level do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testNonIntegerCountShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TMyCollection = class(TObject)")
                .appendImpl("    FCount: Double;")
                .appendImpl("    property Count: Double read FCount write FCount;")
                .appendImpl("  end;")
                .appendImpl("procedure Test(MyCollection: TMyCollection);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to MyCollection.Count do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testStringIterationShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyArr: string);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 1 to Length(MyArr) do begin")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIterationToParenthesizedLengthShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new IterationPastHighBoundCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test(MyArr: TArray<string>);")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+1:30 to +1:30] << - 1>>")
                .appendImpl("  for I := 0 to (Length(MyArr)) do begin // Noncompliant")
                .appendImpl("    // ...")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
