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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberDeclarationOrderCheckTest extends CheckTest {
  @Test
  void testOrderedClassBodyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  private")
        .appendDecl("    FMyField: Integer;")
        .appendDecl("    FMyOtherField: Integer;")
        .appendDecl("    procedure MyProc;")
        .appendDecl("    procedure MyOtherProc;")
        .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemberDeclarationOrderRule"));
  }

  @ParameterizedTest(name = "[{index}] {0} before {1}")
  @CsvSource({"function,field", "property,field", "property,function"})
  void testForbiddenItemsBeforeFieldsShouldAddIssue(String firstItem, String secondItem) {
    firstItem = firstItem.replace("field", "");
    secondItem = secondItem.replace("field", "");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  private")
        .appendDecl("    " + firstItem + " Bar: Integer;")
        .appendDecl("    " + secondItem + " Baz: Integer;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 5));
  }

  @Test
  void testPropertiesBeforeMultipleFieldsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  private")
        .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
        .appendDecl("    FMyField: Integer;")
        .appendDecl("    FMyOtherField: Integer;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 5))
        .areNot(ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 6));
  }

  @Test
  void testMultipleOrderedVisibilitySectionsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
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
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemberDeclarationOrderRule"));
  }

  @Test
  void testMultipleBlocksShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  private")
        .appendDecl("    FMyField: Integer;")
        .appendDecl("    procedure MyProc;")
        .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
        .appendDecl("    FMyOtherField: Integer;")
        .appendDecl("    procedure MyOtherProc;")
        .appendDecl("    property MyOtherProp: Integer read FMyField write FMyField;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 7))
        .areNot(ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 8));
  }

  @Test
  void testMultiplePropertySectionsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  private")
        .appendDecl("    FMyField: Integer;")
        .appendDecl("    procedure MyProc;")
        .appendDecl("    property MyProp: Integer read FMyField write FMyField;")
        .appendDecl("    FMyOtherField: Integer;")
        .appendDecl("    procedure MyOtherProc;")
        .appendDecl("    property MyOtherProp: Integer read FMyField write FMyField;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 9));
  }

  @ParameterizedTest
  @CsvSource({"field", "function", "property"})
  void testSingleItemShouldNotAddIssue(String item) {
    item = item.replace("field", "");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  private")
        .appendDecl("    " + item + " Bar: Integer;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MemberDeclarationOrderRule"));
  }

  @Test
  void testImplicitVisibilitySectionShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("    property MyProp: Integer;")
        .appendDecl("    procedure MyProc;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 4));
  }

  @ParameterizedTest
  @ValueSource(strings = {"class", "record", "interface", "object"})
  void testAnyOutOfOrderStructureShouldAddIssue(String structType) {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = " + structType)
        .appendDecl("    property MyProp: Integer;")
        .appendDecl("    procedure MyProc;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MemberDeclarationOrderRule", builder.getOffsetDecl() + 4));
  }
}
