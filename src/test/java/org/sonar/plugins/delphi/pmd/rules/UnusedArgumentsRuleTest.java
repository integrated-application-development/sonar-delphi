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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class UnusedArgumentsRuleTest extends BasePmdRuleTest {

  @Test
  public void testUnusedArgumentShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TCustomComponent = class(TComponent)");
    builder.appendDecl("  protected");
    builder.appendDecl("    procedure OnUnusedArg(Arg: Integer);");
    builder.appendDecl("  public");
    builder.appendDecl("    procedure CaseInsensitive(Arg: Integer);");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TCustomComponent.OnUnusedArg(Arg: Integer);");
    builder.appendImpl("begin");
    builder.appendImpl("  WriteLn('dummy');");
    builder.appendImpl("end;");

    builder.appendImpl("procedure TCustomComponent.CaseInsensitive(Arg: Integer);");
    builder.appendImpl("begin");
    builder.appendImpl("  Arg := Arg + 1;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("UnusedArgumentsRule", builder.getOffSet() + 1)));
  }

  @Test
  public void testValidRuleExcludedArguments() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("type");
    builder.appendDecl("  TCustomComponent = class(TComponent)");
    builder.appendDecl("  protected");
    builder.appendDecl("    procedure OnEvent(Sender: TObject);");
    builder.appendDecl("    procedure OnEventB(ASender: TObject);");
    builder.appendDecl("  end;");

    builder.appendImpl("procedure TCustomComponent.OnEvent(Sender: TObject);");
    builder.appendImpl("begin");
    builder.appendImpl("  WriteLn('dummy');");
    builder.appendImpl("end;");

    builder.appendImpl("procedure TCustomComponent.OnEventB(ASender: TObject);");
    builder.appendImpl("begin");
    builder.appendImpl("  WriteLn('dummy');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testValidRuleNestedFunction() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("procedure TestNestedParams(const Value : String);");

    builder.appendImpl("procedure TestNestedParams(const Value : String);");
    builder.appendImpl("const");
    builder.appendImpl("  C_MyConstant = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  Data : string;");
    builder.appendImpl("  function Update(const Param : String) : String;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := Param + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("begin");
    builder.appendImpl("  Data := Update(Value);");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testValidRuleManyNestedFunction() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("procedure TestNestedParams(const Value : String);");

    builder.appendImpl("procedure TestNestedParams(const Value : String);");
    builder.appendImpl("const");
    builder.appendImpl("  C_MyConstant = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  Data : String;");
    builder.appendImpl("  function Update(const Param : String) : String;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := Param + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("  function Insert(const Param : String) : String;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := Param + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("  function Retrieve(const Param : String) : String;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := Param + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("begin");
    builder.appendImpl("  Data := Update(Value);");
    builder.appendImpl("  Data := Insert(Value);");
    builder.appendImpl("  Data := Retrieve(Value);");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testValidRuleMultipleNestedFunction() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("procedure TestNestedParams(const Value : string);");

    builder.appendImpl("procedure TestNestedParams(const Value : string);");
    builder.appendImpl("const");
    builder.appendImpl("  C_MyConstant = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  Data : String;");
    builder.appendImpl("  function Update1(const Param : String) : String;");
    builder.appendImpl("    function Update2(const Param : String) : String;");
    builder.appendImpl("      function Update3(const Param : String) : String;");
    builder.appendImpl("      begin");
    builder.appendImpl("        Result := Param + ' dummy';");
    builder.appendImpl("      end;");
    builder.appendImpl("    begin");
    builder.appendImpl("      Result := Update3(Param + '3');");
    builder.appendImpl("    end;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := Update2(Param + '2');");
    builder.appendImpl("  end;");
    builder.appendImpl("begin");
    builder.appendImpl("  lData := Update1(Value);");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testIssuesMultipleNestedFunction() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("procedure TestNestedParams(const Value : String);");

    builder.appendImpl("procedure TestNestedParams(const Value : String);");
    builder.appendImpl("const");
    builder.appendImpl("  C_MyConstant = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  Data : string;");
    builder.appendImpl("  function Update1(const Param : String) : String;");
    builder.appendImpl("    function Update2(const Param : String) : String;");
    builder.appendImpl("      function Update3(const Param : String) : String;");
    builder.appendImpl("      begin");
    builder.appendImpl("        Result := 'dummy';");
    builder.appendImpl("      end;");
    builder.appendImpl("    begin");
    builder.appendImpl("      Result := Update3('3');");
    builder.appendImpl("    end;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := Update2('2');");
    builder.appendImpl("  end;");
    builder.appendImpl("begin");
    builder.appendImpl("  Data := Update1('1');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(4));
    assertIssues(hasItem(hasRuleKeyAtLine("UnusedArgumentsRule", builder.getOffSet() + 1)));
    assertIssues(hasItem(hasRuleKeyAtLine("UnusedArgumentsRule", builder.getOffSet() + 6)));
    assertIssues(hasItem(hasRuleKeyAtLine("UnusedArgumentsRule", builder.getOffSet() + 7)));
    assertIssues(hasItem(hasRuleKeyAtLine("UnusedArgumentsRule", builder.getOffSet() + 8)));
  }
}
