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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class VisibilitySectionOrderCheckTest {
  @ParameterizedTest(name = "[{index}] {0} before {1}")
  @CsvSource({
    "private,strict private",
    "protected,private",
    "protected,strict protected",
    "published,public",
    "published,private",
  })
  void testOutOfOrderSectionsShouldAddIssue(String firstVisibility, String secondVisibility) {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  " + firstVisibility)
                .appendDecl("    procedure Bar;")
                .appendDecl("    property Baz: Integer;")
                .appendDecl("  " + secondVisibility + " // Noncompliant")
                .appendDecl("    procedure Bar2;")
                .appendDecl("    property Baz2: Integer;")
                .appendDecl(" end;"))
        .verifyIssues();
  }

  @ParameterizedTest(name = "[{index}] {0} before {1}")
  @CsvSource({
    "strict private,private",
    "private,protected",
    "strict protected,protected",
    "public,published",
    "private,published",
  })
  void testOrderedSectionsShouldNotAddIssue(String firstVisibility, String secondVisibility) {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  " + firstVisibility)
                .appendDecl("    procedure Bar;")
                .appendDecl("    property Baz: Integer;")
                .appendDecl("  " + secondVisibility)
                .appendDecl("    procedure Bar2;")
                .appendDecl("    property Baz2: Integer;")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @Test
  void testMultipleOfTheSameSectionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure Bar;")
                .appendDecl("    property Baz: Integer;")
                .appendDecl("  public")
                .appendDecl("    procedure Bar2;")
                .appendDecl("    property Baz2: Integer;")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @Test
  void testMultipleOutOfOrderSectionsShouldAddIssues() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    procedure Baz;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    procedure Bee;")
                .appendDecl("  protected // Noncompliant")
                .appendDecl("    procedure Bee;")
                .appendDecl("  published")
                .appendDecl("    procedure Bee;")
                .appendDecl(" end;"))
        .verifyIssues();
  }

  @Test
  void testMultipleReferencedOutOfOrderSectionsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    type TBar = string;")
                .appendDecl("  public")
                .appendDecl("    const CBaz = 'abcd';")
                .appendDecl("  private")
                .appendDecl("    procedure Flarp(Arg: TBar = CBaz);")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @Test
  void testOutOfOrderSectionAfterMultipleReferencedOutOfOrderSectionsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  protected")
                .appendDecl("    FMyVar: Integer;")
                .appendDecl("  public")
                .appendDecl("    type TBar = string;")
                .appendDecl("  public")
                .appendDecl("    const CBaz = 'abcd';")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    procedure Flarp(Arg: TBar = CBaz);")
                .appendDecl(" end;"))
        .verifyIssues();
  }

  @Test
  void testImplicitPublishedFollowedByPrivateShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    procedure Bar;")
                .appendDecl("  private")
                .appendDecl("    procedure Baz;")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @Test
  void testOutOfOrderUnreferencedVisibilitySectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    const")
                .appendDecl("      CMyConst = 'abcd';")
                .appendDecl("    type")
                .appendDecl("      TBar = TFlarp;")
                .appendDecl("    FZorp: string;")
                .appendDecl("    function GetMyInt: Integer;")
                .appendDecl("  private // Noncompliant")
                .appendDecl("    procedure Baz;")
                .appendDecl(" end;"))
        .verifyIssues();
  }

  @Test
  void testOutOfOrderVisibilitySectionWithReferencedConstantShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    const")
                .appendDecl("      CMyConst = 'abcd';")
                .appendDecl("    type")
                .appendDecl("      TBar = TFlarp;")
                .appendDecl("    FZorp: string;")
                .appendDecl("    function GetMyInt: Integer;")
                .appendDecl("  private")
                .appendDecl("    procedure Baz(MyArg: string = CMyConst);")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @Test
  void testOutOfOrderVisibilitySectionWithReferencedTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    const")
                .appendDecl("      CMyConst = 'abcd';")
                .appendDecl("    type")
                .appendDecl("      TBar = TFlarp;")
                .appendDecl("    FZorp: string;")
                .appendDecl("    function GetMyInt: Integer;")
                .appendDecl("  private")
                .appendDecl("    procedure Baz(MyArg: TBar);")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @Test
  void testOutOfOrderVisibilitySectionWithReferencedVarShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    const")
                .appendDecl("      CMyConst = 'abcd';")
                .appendDecl("    type")
                .appendDecl("      TBar = TFlarp;")
                .appendDecl("    FZorp: string;")
                .appendDecl("    function GetMyInt: Integer;")
                .appendDecl("  private")
                .appendDecl("    property Baz: string read FZorp;")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @Test
  void testOutOfOrderVisibilitySectionWithReferencedFunctionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  public")
                .appendDecl("    const")
                .appendDecl("      CMyConst = 'abcd';")
                .appendDecl("    type")
                .appendDecl("      TBar = TFlarp;")
                .appendDecl("    FZorp: string;")
                .appendDecl("    function GetMyInt: Integer;")
                .appendDecl("  private")
                .appendDecl("    property Baz: Integer read GetMyInt;")
                .appendDecl(" end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "class",
        "record",
        "object",
        "class helper for TObject",
        "record helper for string"
      })
  void testAllStructTypesWithOrderedVisibilitySectionsShouldAddIssue(String structType) {
    CheckVerifier.newVerifier()
        .withCheck(new VisibilitySectionOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo  = " + structType)
                .appendDecl("  public")
                .appendDecl("    procedure Bar;")
                .appendDecl("  protected // Noncompliant")
                .appendDecl("    procedure Baz;")
                .appendDecl(" end;"))
        .verifyIssues();
  }
}
