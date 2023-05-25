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

class VariableNameCheckTest extends CheckTest {
  @Test
  void testValidGlobalNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  GMyChar: Char;")
            .appendDecl("  GAnotherChar: Char;")
            .appendDecl("  GThirdChar: Char;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testInvalidGlobalNamesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  G_My_Char: Char;")
            .appendDecl("  gAnotherChar: Char;")
            .appendDecl("  GlobalChar: Char;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 2))
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 3))
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testValidNameInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  SomeVar: Integer;")
            .appendImpl("begin")
            .appendImpl("  SomeVar := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testBadPascalCaseInMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  someVar: Integer;")
            .appendImpl("begin")
            .appendImpl("  someVar := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 3));
  }

  @Test
  void testAutoCreateFormVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  Vcl.Forms;")
            .appendDecl("type")
            .appendDecl("  TFooForm = class(TForm)")
            .appendDecl("  end;")
            .appendDecl("var")
            .appendDecl("  omForm: TFooForm;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testValidArgumentNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  Arg := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testBadPascalCaseInArgumentNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  arg := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 1));
  }

  @Test
  void testValidInlineVariableNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var SomeVar: Integer;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testBadPascalCaseInlineVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var someVar: Integer;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 3));
  }

  @Test
  void testValidLoopVariableNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  for var SomeVar := 1 to 100 do;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testBadPascalCaseInLoopVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  for var someVar := 1 to 100 do;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 3));
  }

  @Test
  void testBadPascalCaseInMethodImplementingGoodPascalCaseInterfaceShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IFoo = interface")
            .appendDecl("    procedure Proc(MyStr: string; MyInt: Integer);")
            .appendDecl("  end;")
            .appendDecl("  TBar = class(TObject, IFoo)")
            .appendDecl("    procedure Proc(myStr: string; myInt: Integer);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(2, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 6));
  }

  @Test
  void testBadPascalCaseInMethodImplementingBadPascalCaseInterfaceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IFoo = interface")
            .appendDecl("    procedure Proc(myStr: string; myInt: Integer);")
            .appendDecl("  end;")
            .appendDecl("  TBar = class(TObject, IFoo)")
            .appendDecl("    procedure Proc(myStr: string; myInt: Integer);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 6));
  }
}
