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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class NoSemicolonRuleTest extends BasePmdRuleTest {

  @Test
  public void testRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure NoSemicolonsAfterLastInstruction;");
    builder.appendImpl("begin");
    builder.appendImpl("  SomeVar := 5");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("NoSemicolonRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testInsideWhile() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure NoSemicolonsAfterLastInstruction;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  while x <> 0 do");
    builder.appendImpl("  begin");
    builder.appendImpl("    WriteLn('test')");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("NoSemicolonRule", builder.getOffSet() + 7)));
  }

  @Test
  public void testOnEndOfWhile() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure NoSemicolonsAfterLastInstruction;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeVar: integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  while SomeVar <> 0 do");
    builder.appendImpl("  begin");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  end");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("NoSemicolonRule", builder.getOffSet() + 8)));
  }

  @Test
  public void testShouldSkipEndFollowedByElse() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure NoSemicolonsAfterLastInstruction(val: Boolean);");
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

    assertIssues(empty());
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
    builder.appendImpl("  FData := aData;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
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

    assertIssues(empty());
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

    assertIssues(empty());
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

    assertIssues(empty());
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

    assertIssues(empty());
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("NoSemicolonRule", builder.getOffSet() + 9)));
  }
}
