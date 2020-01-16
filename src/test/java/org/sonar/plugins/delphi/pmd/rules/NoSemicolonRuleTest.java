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
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("begin");
    builder.appendImpl("  SomeVar := 5");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 3));
  }

  @Test
  public void testInsideWhile() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  while SomeNumber <> 0 do");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testInsideFor() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  for SomeNumber := 0 to 3 do");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testInsideRepeat() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  repeat");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("  until SomeNumber <> 0;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testInsideTryExcept() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  try");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("  except");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 8));
  }

  @Test
  public void testInsideExceptionHandler() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  try");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  except");
    builder.appendImpl("    on E: Exception do");
    builder.appendImpl("      WriteLn('test')");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 9));
  }

  @Test
  public void testInsideTryFinally() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  try");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("  finally");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 8));
  }

  @Test
  public void testOnEndOfWhile() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeVar: integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  while SomeVar <> 0 do");
    builder.appendImpl("  begin");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  end");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 8));
  }

  @Test
  public void testOnCaseItem() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeVar: integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  case SomeVar of");
    builder.appendImpl("    1: WriteLn('test')");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 6));
  }

  @Test
  public void testShouldSkipEndFollowedByElse() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest(val: Boolean);");
    builder.appendImpl("begin");
    builder.appendImpl("  if val then");
    builder.appendImpl("  begin");
    builder.appendImpl("    writeln('test');");
    builder.appendImpl("  end");
    builder.appendImpl("  else");
    builder.appendImpl("  begin");
    builder.appendImpl("    writeln('test');");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipRecordDeclarationOnImplementationSection() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("type");
    builder.appendImpl("  TDummyRec = record");
    builder.appendImpl("    FData : Integer;");
    builder.appendImpl("    constructor Create(Data: Integer);");
    builder.appendImpl("  end;");
    builder.appendImpl("  ");
    builder.appendImpl("constructor TDummyRec.Create(Data: Integer);");
    builder.appendImpl("begin");
    builder.appendImpl("  inherited;");
    builder.appendImpl("  FData := Data;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipClassDeclarationOnImplementationSection() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("type");
    builder.appendImpl("  TDummyClass = class");
    builder.appendImpl("    FData : Integer;");
    builder.appendImpl("    constructor Create(aData : Integer);");
    builder.appendImpl("  end;");
    builder.appendImpl("  ");
    builder.appendImpl("constructor TDummyClass.Create(aData : Integer);");
    builder.appendImpl("begin");
    builder.appendImpl("  inherited;");
    builder.appendImpl("  FData := aData;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipInterfaceDeclarationOnImplementationSection() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("type");
    builder.appendImpl("  IDummyInterface = interface");
    builder.appendImpl("  ['{FBDFC204-9986-48D5-BBBC-ED5A99834A9F}']");
    builder.appendImpl("    procedure Dummy;");
    builder.appendImpl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipAsmProcedure() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Test; assembler; register;");
    builder.appendImpl("asm");
    builder.appendImpl("   MOV EAX, 1");
    builder.appendImpl("   ADD EAX, 2");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipInlineAsm() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Test; assembler; register;");
    builder.appendImpl("var");
    builder.appendImpl("  MyVar: Boolean;");
    builder.appendImpl("begin");
    builder.appendImpl("  MyVar := True;");
    builder.appendImpl("  asm");
    builder.appendImpl("    MOV EAX, 1");
    builder.appendImpl("    ADD EAX, 2");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInlineAsmWithoutSemicolonAfterEndShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Test; assembler; register;");
    builder.appendImpl("var");
    builder.appendImpl("  MyVar: Boolean;");
    builder.appendImpl("begin");
    builder.appendImpl("  MyVar := True;");
    builder.appendImpl("  asm");
    builder.appendImpl("    MOV EAX, 1");
    builder.appendImpl("    ADD EAX, 2");
    builder.appendImpl("  end");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoSemicolonRule", builder.getOffset() + 9));
  }
}
