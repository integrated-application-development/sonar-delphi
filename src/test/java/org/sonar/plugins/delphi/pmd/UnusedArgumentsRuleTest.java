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
package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class UnusedArgumentsRuleTest extends BasePmdRuleTest {

/*  @Test
  public void testRule() {
    configureTest(ROOT_DIR_NAME + "/UnusedArgumentRule.pas");

    DebugSensorContext debugContext = new DebugSensorContext();
    sensor.testAnalyse(project, debugContext);

    // all expected rule violations and their lines
    RuleData ruleData[] = {
      new RuleData("UnusedArgumentsRule", 31)
    };

    // Sort the violations by line number, so we don't have to add
    // violations order
    Arrays.sort(ruleData, RuleData.getComparator());

    assertThat(toString(issues), issues, hasSize(1));

    for (int i = 0; i < issues.size(); ++i) {
      Issue issue = issues.get(i);

      assertThat("rule " + ruleData[i].toString(), ruleData[i].getName(), is(issue.ruleKey().rule()));
      assertThat("rule " + ruleData[i].toString() + "line ", ruleData[i].getLine(), is(issue.line()));
    }
  }*/

  @Test
  public void testValidRuleNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

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

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testValidRuleManyNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

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

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testValidRuleMultipleNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

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

    assertThat(stringifyIssues(), issues, is(empty()));
  }

  @Test
  public void testIssuesMultipleNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

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

    assertThat(stringifyIssues(), issues, hasSize(4));
  }
}
