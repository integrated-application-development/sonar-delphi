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
package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class MethodNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testInterfaceMethodValidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  IMyInterface = interface");
    builder.appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']");
    builder.appendDecl("    procedure Foo;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testInterfaceMethodNameStartWithLowerCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  IMyInterface = interface");
    builder.appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']");
    builder.appendDecl("    procedure foo;");
    builder.appendDecl("    function bar: Integer;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(2));
    assertIssues(hasItem(hasRuleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 4)));
    assertIssues(hasItem(hasRuleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 5)));
  }

  @Test
  public void testPublishedMethodsShouldBeSkipped() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TForm)");
    builder.appendDecl("    procedure buttonOnClick(Sender: TNotifyEvent);");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }
}
