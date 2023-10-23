/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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

class MissingSemicolonCheckTest {
  @Test
  void testMissingSemicolonShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("begin")
                .appendImpl("  SomeVar := 5 // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonWithinWhileShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("var")
                .appendImpl("  SomeNumber: Integer;")
                .appendImpl("begin")
                .appendImpl("  while SomeNumber <> 0 do")
                .appendImpl("    WriteLn('test') // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonWithinForShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("var")
                .appendImpl("  SomeNumber: Integer;")
                .appendImpl("begin")
                .appendImpl("  for SomeNumber := 0 to 3 do")
                .appendImpl("    WriteLn('test') // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonWithinRepeatShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  repeat")
                .appendImpl("    WriteLn('test') // Noncompliant")
                .appendImpl("  until Int <> 0;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonWithinTryExceptShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("var")
                .appendImpl("  SomeNumber: Integer;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    WriteLn('test') // Noncompliant")
                .appendImpl("  except")
                .appendImpl("    WriteLn('test') // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonWithinExceptionHandlerShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("var")
                .appendImpl("  SomeNumber: Integer;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    WriteLn('test');")
                .appendImpl("  except")
                .appendImpl("    on E: Exception do")
                .appendImpl("      WriteLn('test') // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonWithinTryFinallyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("var")
                .appendImpl("  SomeNumber: Integer;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    WriteLn('test') // Noncompliant")
                .appendImpl("  finally")
                .appendImpl("    WriteLn('test') // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonAfterWhileCompoundStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  while Int <> 0 do")
                .appendImpl("  begin")
                .appendImpl("    WriteLn('test');")
                .appendImpl("  end // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testMissingSemicolonOnCaseItemShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("var")
                .appendImpl("  SomeVar: Integer;")
                .appendImpl("begin")
                .appendImpl("  case SomeVar of")
                .appendImpl("    1: WriteLn('test') // Noncompliant")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testEndFollowedByElseShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest(Val: Boolean);")
                .appendImpl("begin")
                .appendImpl("  if Val then")
                .appendImpl("  begin")
                .appendImpl("    WriteLn('test');")
                .appendImpl("  end")
                .appendImpl("  else")
                .appendImpl("  begin")
                .appendImpl("    WriteLn('test');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testShouldRecordDeclarationInImplementationSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TDummyRec = record")
                .appendImpl("    FData : Integer;")
                .appendImpl("    constructor Create(Data: Integer);")
                .appendImpl("  end;")
                .appendImpl("constructor TDummyRec.Create(Data: Integer);")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  FData := Data;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testClassDeclarationInImplementationSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  TDummyClass = class(TObject)")
                .appendImpl("    FData : Integer;")
                .appendImpl("    constructor Create(Data : Integer);")
                .appendImpl("  end;")
                .appendImpl("constructor TDummyClass.Create(Data : Integer);")
                .appendImpl("begin")
                .appendImpl("  inherited;")
                .appendImpl("  FData := Data;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInterfaceDeclarationInImplementationSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("type")
                .appendImpl("  IDummyInterface = interface")
                .appendImpl("  ['{FBDFC204-9986-48D5-BBBC-ED5A99834A9F}']")
                .appendImpl("    procedure Dummy;")
                .appendImpl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testSAsmProcedureShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test; assembler; register;")
                .appendImpl("asm")
                .appendImpl("   MOV EAX, 1")
                .appendImpl("   ADD EAX, 2")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testShouldSkipInlineAsm() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test; assembler; register;")
                .appendImpl("var")
                .appendImpl("  MyVar: Boolean;")
                .appendImpl("begin")
                .appendImpl("  MyVar := True;")
                .appendImpl("  asm")
                .appendImpl("    MOV EAX, 1")
                .appendImpl("    ADD EAX, 2")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInlineAsmWithoutSemicolonAfterEndShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test; assembler; register;")
                .appendImpl("var")
                .appendImpl("  MyVar: Boolean;")
                .appendImpl("begin")
                .appendImpl("  MyVar := True;")
                .appendImpl("  asm")
                .appendImpl("    MOV EAX, 1")
                .appendImpl("    ADD EAX, 2")
                .appendImpl("  end // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testFieldDeclarationsWithSemicolonsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("    Foo: TObject;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testFieldDeclarationsWithoutSemicolonsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("    Foo: TObject // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testFieldDeclarationsWithoutSemicolonsInRecordVariantSectionsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = record")
                .appendDecl("    case Tag: Integer of")
                .appendDecl("      0: (ByteField: Byte);")
                .appendDecl("      1: (ShortIntField: ShortInt);")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodDeclarationsWithSemicolonsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    constructor Create; override;")
                .appendDecl("    destructor Destroy; override;")
                .appendDecl("    procedure MyProcedure; overload;")
                .appendDecl("    function MyFunction: String; overload;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testMethodDeclarationsWithoutSemicolonsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MissingSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    constructor Create; override // Noncompliant")
                .appendDecl("    destructor Destroy; override // Noncompliant")
                .appendDecl("    procedure MyProcedure; overload // Noncompliant")
                .appendDecl("    function MyFunction: String; overload // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }
}
