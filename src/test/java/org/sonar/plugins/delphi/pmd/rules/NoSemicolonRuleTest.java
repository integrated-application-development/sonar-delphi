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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class NoSemicolonRuleTest extends BasePmdRuleTest {

  @Test
  public void testRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("begin")
            .appendImpl("  SomeVar := 5")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 3));
  }

  @Test
  public void testInsideWhile() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("var")
            .appendImpl("  SomeNumber: Integer;")
            .appendImpl("begin")
            .appendImpl("  while SomeNumber <> 0 do")
            .appendImpl("    WriteLn('test')")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testInsideFor() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("var")
            .appendImpl("  SomeNumber: Integer;")
            .appendImpl("begin")
            .appendImpl("  for SomeNumber := 0 to 3 do")
            .appendImpl("    WriteLn('test')")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testInsideRepeat() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("var")
            .appendImpl("  SomeNumber: Integer;")
            .appendImpl("begin")
            .appendImpl("  repeat")
            .appendImpl("    WriteLn('test')")
            .appendImpl("  until SomeNumber <> 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testInsideTryExcept() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendImpl("procedure SemicolonTest;")
        .appendImpl("var")
        .appendImpl("  SomeNumber: Integer;")
        .appendImpl("begin")
        .appendImpl("  try")
        .appendImpl("    WriteLn('test')")
        .appendImpl("  except")
        .appendImpl("    WriteLn('test')")
        .appendImpl("  end;")
        .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 8));
  }

  @Test
  public void testInsideExceptionHandler() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("var")
            .appendImpl("  SomeNumber: Integer;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do")
            .appendImpl("      WriteLn('test')")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 9));
  }

  @Test
  public void testInsideTryFinally() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("var")
            .appendImpl("  SomeNumber: Integer;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    WriteLn('test')")
            .appendImpl("  finally")
            .appendImpl("    WriteLn('test')")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 8));
  }

  @Test
  public void testOnEndOfWhile() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("var")
            .appendImpl("  SomeVar: Integer;")
            .appendImpl("begin")
            .appendImpl("  while SomeVar <> 0 do")
            .appendImpl("  begin")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  end")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 8));
  }

  @Test
  public void testOnCaseItem() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("var")
            .appendImpl("  SomeVar: Integer;")
            .appendImpl("begin")
            .appendImpl("  case SomeVar of")
            .appendImpl("    1: WriteLn('test')")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testShouldSkipEndFollowedByElse() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest(Val: Boolean);")
            .appendImpl("begin")
            .appendImpl("  if Val then")
            .appendImpl("  begin")
            .appendImpl("    writeln('test');")
            .appendImpl("  end")
            .appendImpl("  else")
            .appendImpl("  begin")
            .appendImpl("    writeln('test');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipRecordDeclarationOnImplementationSection() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("type")
            .appendImpl("  TDummyRec = record")
            .appendImpl("    FData : Integer;")
            .appendImpl("    constructor Create(Data: Integer);")
            .appendImpl("  end;")
            .appendImpl("  ")
            .appendImpl("constructor TDummyRec.Create(Data: Integer);")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("  FData := Data;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipClassDeclarationOnImplementationSection() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("type")
            .appendImpl("  TDummyClass = class(TObject)")
            .appendImpl("    FData : Integer;")
            .appendImpl("    constructor Create(Data : Integer);")
            .appendImpl("  end;")
            .appendImpl("  ")
            .appendImpl("constructor TDummyClass.Create(Data : Integer);")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("  FData := Data;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipInterfaceDeclarationOnImplementationSection() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("type")
            .appendImpl("  IDummyInterface = interface")
            .appendImpl("  ['{FBDFC204-9986-48D5-BBBC-ED5A99834A9F}']")
            .appendImpl("    procedure Dummy;")
            .appendImpl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipAsmProcedure() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test; assembler; register;")
            .appendImpl("asm")
            .appendImpl("   MOV EAX, 1")
            .appendImpl("   ADD EAX, 2")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipInlineAsm() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInlineAsmWithoutSemicolonAfterEndShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test; assembler; register;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("  asm")
            .appendImpl("    MOV EAX, 1")
            .appendImpl("    ADD EAX, 2")
            .appendImpl("  end")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 9));
  }
}
