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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class NoSemicolonRuleTest extends BasePmdRuleTest {

  @Test
  public void testRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("procedure NoSemicolonsAfterLastInstruction;");
    builder.appendImpl("begin");
    builder.appendImpl("  x := 5");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues, not(empty()));
    List<Issue> matchIssues = new ArrayList<Issue>();
    for (Issue issue : issues) {
      if (issue.ruleKey().rule().equals("NoSemicolonRule")) {
        matchIssues.add(issue);
      }
    }
    assertThat(matchIssues, hasSize(1));
    assertThat(matchIssues.get(0).line(), is(builder.getOffSet() + 3));
  }

  @Test
  public void testInsideWhile() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("procedure NoSemicolonsAfterLastInstruction;");
    builder.appendImpl("var");
    builder.appendImpl("  x: integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  while x <> 0 do");
    builder.appendImpl("  begin");
    builder.appendImpl("    writeln('test')");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues, not(empty()));
    List<Issue> matchIssues = new ArrayList<Issue>();
    for (Issue issue : issues) {
      if (issue.ruleKey().rule().equals("NoSemicolonRule")) {
        matchIssues.add(issue);
      }
    }
    assertThat(matchIssues, hasSize(1));
    assertThat(matchIssues.get(0).line(), is(builder.getOffSet() + 7));
  }

  @Test
  public void testOnEndOfWhile() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("procedure NoSemicolonsAfterLastInstruction;");
    builder.appendImpl("var");
    builder.appendImpl("  x: integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  while x <> 0 do");
    builder.appendImpl("  begin");
    builder.appendImpl("    writeln('test');");
    builder.appendImpl("  end");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(issues, not(empty()));
    List<Issue> matchIssues = new ArrayList<Issue>();
    for (Issue issue : issues) {
      if (issue.ruleKey().rule().equals("NoSemicolonRule")) {
        matchIssues.add(issue);
      }
    }
    assertThat(matchIssues, hasSize(1));
    // TODO The correct line is 15
    assertThat(matchIssues.get(0).line(), is(builder.getOffSet() + 9));
  }

  @Test
  public void shouldSkipEndFollowedByElse() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
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

    analyse(builder);

    assertThat(sensor.getErrors(), empty());
    assertThat(issues, empty());
  }

  @Test
  public void shouldSkipRecordDeclarationOnImplementationSection() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("type");
    builder.appendImpl("  TDummyRec = record");
    builder.appendImpl("    FData : Integer;");
    builder.appendImpl("    constructor Create(aData : Integer);");
    builder.appendImpl("  end;");
    builder.appendImpl("  ");
    builder.appendImpl("constructor TDummyRec.Create(aData : Integer);");
    builder.appendImpl("begin");
    builder.appendImpl("  inherited;");
    builder.appendImpl("  FData := aData;");
    builder.appendImpl("end;");

    analyse(builder);

    assertThat(sensor.getErrors(), empty());
    assertThat(issues, empty());
  }

  @Test
  public void shouldSkipClassDeclarationOnImplementationSection() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
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

    analyse(builder);

    assertThat(sensor.getErrors(), empty());
    assertThat(issues, empty());
  }

  @Test
  public void shouldSkipInterfaceDeclarationOnImplementationSection() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendImpl("type");
    builder.appendImpl("  IDummyInterface = interface");
    builder.appendImpl("  ['{FBDFC204-9986-48D5-BBBC-ED5A99834A9F}']");
    builder.appendImpl("    procedure Dummy;");
    builder.appendImpl("  end;");

    analyse(builder);

    assertThat(sensor.getErrors(), empty());
    assertThat(issues, empty());
  }

}
