/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
import org.sonar.plugins.delphi.IssueMatchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MethodNameRuleTest extends BasePmdRuleTest {

  @Test
  public void interfaceMethodValidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  IMyInterface = interface");
    builder.appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']");
    builder.appendDecl("    procedure Foo;");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }

  @Test
  public void interfaceMethodNameStartWithLowerCaseShouldAddIssue() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  IMyInterface = interface");
    builder.appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']");
    builder.appendDecl("    procedure foo;");
    builder.appendDecl("    function bar: integer;");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues.toString(), issues, hasSize(2));

    assertThat(issues, hasItem(allOf(
      IssueMatchers.hasRuleKey("MethodNameRule"),
      IssueMatchers.hasRuleLine(builder.getOffsetDecl() + 4)
      )));

    assertThat(issues, hasItem(allOf(
      IssueMatchers.hasRuleKey("MethodNameRule"),
      IssueMatchers.hasRuleLine(builder.getOffsetDecl() + 5)
      )));
  }

  @Test
  public void publishedMethodsShouldBeSkipped() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TForm)");
    builder.appendDecl("    procedure buttonOnClick(Sender: TNotifyEvent);");
    builder.appendDecl("  end;");

    analyse(builder);

    assertThat(issues, is(empty()));
  }
}
