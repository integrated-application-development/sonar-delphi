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

class MemberDeclarationOrderCheckTest {
  @Test
  void testOrderedClassBodyShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FMyField: Integer;")
                .appendDecl("    FMyOtherField: Integer;")
                .appendDecl("    procedure MyProc;")
                .appendDecl("    procedure MyOtherProc;")
                .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest(name = "[{index}] {0} before {1}")
  @CsvSource({"function,field", "property,field", "property,function"})
  void testForbiddenItemsBeforeFieldsShouldAddIssue(String firstItem, String secondItem) {
    firstItem = firstItem.replace("field", "");
    secondItem = secondItem.replace("field", "");

    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    " + firstItem + " Bar: Integer;")
                .appendDecl("    " + secondItem + " Baz: Integer;")
                .appendDecl("  end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testPropertiesBeforeMultipleFieldsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
                .appendDecl("    FMyField: Integer;")
                .appendDecl("    FMyOtherField: Integer;")
                .appendDecl("  end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testMultipleOrderedVisibilitySectionsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FMyField: Integer;")
                .appendDecl("    procedure MyProc;")
                .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
                .appendDecl("  protected")
                .appendDecl("    FMyOtherField: Integer;")
                .appendDecl("    procedure MyOtherProc;")
                .appendDecl("    property MySecondProp: Integer read FMyField write FMyField;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testMultipleBlocksShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FMyField: Integer;")
                .appendDecl("    procedure MyProc;")
                .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
                .appendDecl("    FMyOtherField: Integer;")
                .appendDecl("    procedure MyOtherProc;")
                .appendDecl("    property MyOtherProp: Integer read FMyField write FMyField;")
                .appendDecl("  end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testMultiplePropertySectionsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    FMyField: Integer;")
                .appendDecl("    procedure MyProc;")
                .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
                .appendDecl("    FMyOtherField: Integer;")
                .appendDecl("    procedure MyOtherProc;")
                .appendDecl("    property MyOtherProp: Integer read FMyField write FMyField;")
                .appendDecl("  end;"))
        .verifyIssueOnLine(11);
  }

  @ParameterizedTest
  @CsvSource({"field", "function", "property"})
  void testSingleItemShouldNotAddIssue(String item) {
    item = item.replace("field", "");

    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("  private")
                .appendDecl("    " + item + " Bar: Integer;")
                .appendDecl("  end;"))
        .verifyNoIssues();
  }

  @Test
  void testImplicitVisibilitySectionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = class(TObject)")
                .appendDecl("    property MyProp: Integer;")
                .appendDecl("    procedure MyProc;")
                .appendDecl("  end;"))
        .verifyIssueOnLine(8);
  }

  @ParameterizedTest
  @ValueSource(strings = {"class", "record", "interface", "object"})
  void testAnyOutOfOrderStructureShouldAddIssue(String structType) {
    CheckVerifier.newVerifier()
        .withCheck(new MemberDeclarationOrderCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TFoo = " + structType)
                .appendDecl("    property MyProp: Integer;")
                .appendDecl("    procedure MyProc;")
                .appendDecl("  end;"))
        .verifyIssueOnLine(8);
  }
}
