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

import org.junit.Test;
import org.sonar.api.issue.Issue;
import org.sonar.plugins.delphi.debug.DebugSensorContext;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UnusedArgumentsRuleTest extends BasePmdRuleTest {

  @Test
  public void testRule() {
    configureTest(ROOT_DIR_NAME + "/UnusedArgumentRule.pas");

    DebugSensorContext debugContext = new DebugSensorContext();
    sensor.analyse(project, debugContext);

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
  }

  @Test
  public void validRuleNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("procedure TestNestedParams(const aValue : string);");

    builder.appendImpl("procedure TestNestedParams(const aValue : string);");
    builder.appendImpl("const");
    builder.appendImpl("  DEFAULT_VALUE = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  lData : string;");
    builder.appendImpl("  function Update(const aParam : string) : string;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := aParam + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("begin");
    builder.appendImpl("  lData := Update(aValue);");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(toString(issues), issues, is(empty()));
  }

  @Test
  public void validRuleManyNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("procedure TestNestedParams(const aValue : string);");

    builder.appendImpl("procedure TestNestedParams(const aValue : string);");
    builder.appendImpl("const");
    builder.appendImpl("  DEFAULT_VALUE = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  lData : string;");
    builder.appendImpl("  function Update(const aParam : string) : string;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := aParam + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("  function Insert(const aParam : string) : string;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := aParam + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("  function Retrieve(const aParam : string) : string;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := aParam + ' dummy';");
    builder.appendImpl("  end;");
    builder.appendImpl("begin");
    builder.appendImpl("  lData := Update(aValue);");
    builder.appendImpl("  lData := Insert(aValue);");
    builder.appendImpl("  lData := Retrieve(aValue);");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void validRuleMultipleNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("procedure TestNestedParams(const aValue : string);");

    builder.appendImpl("procedure TestNestedParams(const aValue : string);");
    builder.appendImpl("const");
    builder.appendImpl("  DEFAULT_VALUE = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  lData : string;");
    builder.appendImpl("  function Update1(const aParam : string) : string;");
    builder.appendImpl("    function Update2(const aParam : string) : string;");
    builder.appendImpl("      function Update3(const aParam : string) : string;");
    builder.appendImpl("      begin");
    builder.appendImpl("        Result := aParam + ' dummy';");
    builder.appendImpl("      end;");
    builder.appendImpl("    begin");
    builder.appendImpl("      Result := Update3(aParam + '3');");
    builder.appendImpl("    end;");
    builder.appendImpl("  begin");
    builder.appendImpl("    Result := Update2(aParam + '2');");
    builder.appendImpl("  end;");
    builder.appendImpl("begin");
    builder.appendImpl("  lData := Update1(aValue);");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void issuesMultipleNestedFunction() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();

    builder.appendDecl("procedure TestNestedParams(const aValue : string);");

    builder.appendImpl("procedure TestNestedParams(const aValue : string);");
    builder.appendImpl("const");
    builder.appendImpl("  DEFAULT_VALUE = 'VALUE';");
    builder.appendImpl("var");
    builder.appendImpl("  lData : string;");
    builder.appendImpl("  function Update1(const aParam : string) : string;");
    builder.appendImpl("    function Update2(const aParam : string) : string;");
    builder.appendImpl("      function Update3(const aParam : string) : string;");
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
    builder.appendImpl("  lData := Update1('1');");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(toString(issues), issues, hasSize(4));
  }
}
