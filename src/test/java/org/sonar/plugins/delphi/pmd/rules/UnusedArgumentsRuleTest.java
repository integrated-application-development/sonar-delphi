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

public class UnusedArgumentsRuleTest extends BasePmdRuleTest {

  @Test
  public void testUnusedArgumentShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TCustomComponent = class(TComponent)")
            .appendDecl("  protected")
            .appendDecl("    procedure OnUnusedArg(Arg: Integer);")
            .appendDecl("  public")
            .appendDecl("    procedure CaseInsensitive(Arg: Integer);")
            .appendDecl("  end;")
            .appendImpl("procedure TCustomComponent.OnUnusedArg(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  WriteLn('dummy');")
            .appendImpl("end;")
            .appendImpl("procedure TCustomComponent.CaseInsensitive(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  Arg := Arg + 1;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("UnusedArgumentsRule", builder.getOffset() + 1));
  }

  @Test
  public void testPublishedMethodsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TCustomComponent = class(TComponent)")
            .appendDecl("    procedure OnEvent(Sender: TObject);")
            .appendDecl("  published")
            .appendDecl("    procedure OnEventB(ASender: TObject);")
            .appendDecl("  private")
            .appendDecl("    procedure OnEventC(ASender: TObject);")
            .appendDecl("  end;")
            .appendImpl("procedure TCustomComponent.OnEvent(Sender: TObject);")
            .appendImpl("begin")
            .appendImpl("  WriteLn('dummy');")
            .appendImpl("end;")
            .appendImpl("procedure TCustomComponent.OnEventB(Sender: TObject);")
            .appendImpl("begin")
            .appendImpl("  WriteLn('dummy');")
            .appendImpl("end;")
            .appendImpl("procedure TCustomComponent.OnEventC(Sender: TObject);")
            .appendImpl("begin")
            .appendImpl("  DoSomethingWithSender(Sender);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testOverrideMethodsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TCustomComponent = class(TComponent)")
            .appendDecl("  private")
            .appendDecl("    procedure OnEvent(Sender: TObject); override;")
            .appendDecl("  end;")
            .appendImpl("procedure TCustomComponent.OnEvent(Sender: TObject);")
            .appendImpl("begin")
            .appendImpl("  WriteLn('dummy');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testNestedOverrideMethodsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TCustomComponent = class(TComponent)")
            .appendDecl("  private")
            .appendDecl("    type")
            .appendDecl("      TNestedComponent = class(TComponent)")
            .appendDecl("        procedure OnEvent(Sender: TObject);")
            .appendDecl("    end;")
            .appendDecl("  end;")
            .appendImpl("procedure TCustomComponent.TNestedComponent.OnEvent(Sender: TObject);")
            .appendImpl("begin")
            .appendImpl("  WriteLn('dummy');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testVirtualMethodsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TCustomComponent = class(TComponent)")
            .appendDecl("  private")
            .appendDecl("    procedure OnEvent(Sender: TObject); virtual;")
            .appendDecl("  end;")
            .appendImpl("procedure TCustomComponent.OnEvent(Sender: TObject);")
            .appendImpl("begin")
            .appendImpl("  WriteLn('dummy');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMessageMethodsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TCustomComponent = class(TComponent)")
            .appendDecl("  private")
            .appendDecl("    procedure OnEvent(Sender: TObject); message WM_MESSAGE;")
            .appendDecl("  end;")
            .appendImpl("procedure TCustomComponent.OnEvent(Sender: TObject);")
            .appendImpl("begin")
            .appendImpl("  WriteLn('dummy');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testValidSubProcedureShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure TestNestedParams(const Value : String);")
            .appendImpl("procedure TestNestedParams(const Value : String);")
            .appendImpl("const")
            .appendImpl("  C_MyConstant = 'VALUE';")
            .appendImpl("var")
            .appendImpl("  Data : string;")
            .appendImpl("  function Update(const Param : String) : String;")
            .appendImpl("  begin")
            .appendImpl("    Result := Param + ' dummy';")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Data := Update(Value);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testValidSubProceduresShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure TestNestedParams(const Value : String);")
            .appendImpl("procedure TestNestedParams(const Value : String);")
            .appendImpl("const")
            .appendImpl("  C_MyConstant = 'VALUE';")
            .appendImpl("var")
            .appendImpl("  Data : String;")
            .appendImpl("  function Update(const Param : String) : String;")
            .appendImpl("  begin")
            .appendImpl("    Result := Param + ' dummy';")
            .appendImpl("  end;")
            .appendImpl("  function Insert(const Param : String) : String;")
            .appendImpl("  begin")
            .appendImpl("    Result := Param + ' dummy';")
            .appendImpl("  end;")
            .appendImpl("  function Retrieve(const Param : String) : String;")
            .appendImpl("  begin")
            .appendImpl("    Result := Param + ' dummy';")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Data := Update(Value);")
            .appendImpl("  Data := Insert(Value);")
            .appendImpl("  Data := Retrieve(Value);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testValidNestedSubProceduresShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure TestNestedParams(const Value : string);")
            .appendImpl("procedure TestNestedParams(const Value : string);")
            .appendImpl("const")
            .appendImpl("  C_MyConstant = 'VALUE';")
            .appendImpl("var")
            .appendImpl("  Data : String;")
            .appendImpl("  function Update1(const Param : String) : String;")
            .appendImpl("    function Update2(const Param : String) : String;")
            .appendImpl("      function Update3(const Param : String) : String;")
            .appendImpl("      begin")
            .appendImpl("        Result := Param + ' dummy';")
            .appendImpl("      end;")
            .appendImpl("    begin")
            .appendImpl("      Result := Update3(Param + '3');")
            .appendImpl("    end;")
            .appendImpl("  begin")
            .appendImpl("    Result := Update2(Param + '2');")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  lData := Update1(Value);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testValidSubProcedureUsingOuterArgumentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure TestNestedParams(const Value: String);")
            .appendImpl("procedure TestNestedParams(const Value: String);")
            .appendImpl("const")
            .appendImpl("  C_MyConstant = 'VALUE';")
            .appendImpl("var")
            .appendImpl("  Data : String;")
            .appendImpl("  function Update: String;")
            .appendImpl("  begin")
            .appendImpl("    Result := Value + ' dummy';")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Data := Update;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInvalidNestedSubProceduresShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure TestNestedParams(const Value : String);")
            .appendImpl("procedure TestNestedParams(const Value : String);")
            .appendImpl("const")
            .appendImpl("  C_MyConstant = 'VALUE';")
            .appendImpl("var")
            .appendImpl("  Data : string;")
            .appendImpl("  function Update1(const Param : String) : String;")
            .appendImpl("    function Update2(const Param : String) : String;")
            .appendImpl("      function Update3(const Param : String) : String;")
            .appendImpl("      begin")
            .appendImpl("        Result := 'dummy';")
            .appendImpl("      end;")
            .appendImpl("    begin")
            .appendImpl("      Result := Update3('3');")
            .appendImpl("    end;")
            .appendImpl("  begin")
            .appendImpl("    Result := Update2('2');")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Data := Update1('1');")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(4)
        .areExactly(1, ruleKeyAtLine("UnusedArgumentsRule", builder.getOffset() + 1))
        .areExactly(1, ruleKeyAtLine("UnusedArgumentsRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("UnusedArgumentsRule", builder.getOffset() + 7))
        .areExactly(1, ruleKeyAtLine("UnusedArgumentsRule", builder.getOffset() + 8));
  }

  @Test
  public void testForwardedMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl(
                "function InsertDetour(const TargetProc, Trampoline, DetourProc: Pointer): Boolean; forward;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testHandlerMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TCustomComponent = class(TComponent)")
            .appendDecl("  protected")
            .appendDecl("    procedure DummyHandler(Arg: Integer);")
            .appendDecl("  public")
            .appendDecl("    procedure OtherHandler(Arg: Integer);")
            .appendDecl("  end;")
            .appendDecl("type")
            .appendDecl("  TOtherComponent = class(TComponent)")
            .appendDecl("  protected")
            .appendDecl("    procedure NotImplemented;")
            .appendDecl("  end;")
            .appendImpl("procedure TCustomComponent.SetDummyHandler;")
            .appendImpl("  procedure NestedProcedure;")
            .appendImpl("  begin")
            .appendImpl("    FDummyHandler := DummyHandler;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  NestedProcedure;")
            .appendImpl("end;")
            .appendImpl("procedure TCustomComponent.DummyHandler(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  Log.Info('dummy');")
            .appendImpl("end;")
            .appendImpl("procedure TCustomComponent.OtherHandler(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl(" Log.Info('hello');")
            .appendImpl("end;")
            .appendImpl("procedure TCustomComponent.SetOtherHandler;")
            .appendImpl("begin")
            .appendImpl("  FOtherHandler := Otherhandler")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("UnusedArgumentsRule", builder.getOffset() + 9))
        .areNot(ruleKeyAtLine("UnusedArgumentsRule", builder.getOffset() + 13));
  }

  @Test
  public void testImplementationMethodDeclShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(MyArg: String); forward;")
            .appendImpl("procedure AnotherProcedure(SomeArg: String); overload;")
            .appendImpl("begin")
            .appendImpl("  SomeArg := 'test';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testUsageInAsmBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(MyArg: Integer); forward;")
            .appendImpl("procedure MyProcedure(MyArg: Integer);")
            .appendImpl("begin")
            .appendImpl("  asm")
            .appendImpl("    MOV EAX, MyArg")
            .appendImpl("    ADD EAX, 2")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testUsageInAsmProcShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(MyArg: Integer); forward;")
            .appendImpl("procedure MyProcedure(MyArg: Integer);")
            .appendImpl("asm")
            .appendImpl("  MOV EAX, MyArg")
            .appendImpl("  ADD EAX, 2")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
