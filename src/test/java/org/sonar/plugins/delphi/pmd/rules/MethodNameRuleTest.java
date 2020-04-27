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

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class MethodNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testInterfaceMethodValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IMyInterface = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure Foo;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInterfaceMethodNameStartWithLowerCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IMyInterface = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure foo;")
            .appendDecl("    function bar: Integer;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 4))
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 5));
  }

  @Test
  public void testStandaloneMethodNameStartWithLowerCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure foo;")
            .appendDecl("function bar: Integer;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 1))
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testPublishedMethodsShouldBeSkipped() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyForm = class(TForm)")
            .appendDecl("    procedure buttonOnClick(Sender: TNotifyEvent);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMethodsImplementingInterfacesWithMatchingNameShouldBeSkipped() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IFoo = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure invalidName;")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TObject, IFoo)")
            .appendDecl("    public")
            .appendDecl("      procedure invalidName;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 4))
        .areNot(ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 8));
  }

  @Test
  public void testMethodOverridesWithMatchingNameShouldBeSkipped() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    public")
            .appendDecl("      procedure invalidName;")
            .appendDecl("  end;")
            .appendDecl("  TBar = class(TFoo)")
            .appendDecl("    public")
            .appendDecl("      procedure invalidName; override;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 4))
        .areNot(ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 8));
  }

  @Test
  public void testMethodsImplementingInterfacesWithoutMatchingNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  IFoo = interface")
            .appendDecl("    ['{ACCD0A8C-A60F-464A-8152-52DD36F86356}']")
            .appendDecl("    procedure invalidname;")
            .appendDecl("  end;")
            .appendDecl("  TFoo = class(TObject, IFoo)")
            .appendDecl("    public")
            .appendDecl("      procedure invalidName;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 4))
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 8));
  }

  @Test
  public void testMethodOverridesWithoutMatchingNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    public")
            .appendDecl("      procedure invalidname;")
            .appendDecl("  end;")
            .appendDecl("  TBar = class(TFoo)")
            .appendDecl("    public")
            .appendDecl("      procedure invalidName; override;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 4))
        .areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 8));
  }

  @Test
  public void testMethodOverridesWithUnknownTypeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(UNKNOWN_TYPE)")
            .appendDecl("    public")
            .appendDecl("      procedure invalidname; override;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MethodNameRule", builder.getOffsetDecl() + 4));
  }
}
