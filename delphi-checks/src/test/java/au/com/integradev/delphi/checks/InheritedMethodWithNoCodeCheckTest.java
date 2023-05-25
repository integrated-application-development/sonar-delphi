/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class InheritedMethodWithNoCodeCheckTest extends CheckTest {

  @Test
  void testShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testNoSemicolonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("  FMyField := 5;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InheritedMethodWithNoCodeRule"));
  }

  @Test
  void testImplementationWithInheritedAtEndShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  FMyField := 5;")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InheritedMethodWithNoCodeRule"));
  }

  @Test
  void testFalsePositiveImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  FMyField := 5;")
            .appendImpl("  if MyBoolean then begin")
            .appendImpl("    inherited;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InheritedMethodWithNoCodeRule"));
  }

  @Test
  void testExplicitInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithArgumentsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure(X, Y);")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure(X, Y);")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(X, Y);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithMismatchedArgumentSizesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure(X, Y);")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure(X, Y);")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(X);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithMismatchedArgumentsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure(X, Y);")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure(X, Y);")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(True, False);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testExplicitInheritedWithEmptyBracketsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testWrongExplicitInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    procedure MyProcedure;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited SomeOtherProcedure;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InheritedMethodWithNoCodeRule"));
  }

  @Test
  void testFunctionInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    function MyFunction: Integer;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    function MyFunction: Integer;")
            .appendDecl("  end;")
            .appendImpl("function TChild.MyFunction: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := inherited MyFunction;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testFunctionQualifiedInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("    function MyFunction: Integer;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("    function MyFunction: Integer;")
            .appendDecl("  end;")
            .appendImpl("function TChild.MyFunction: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := inherited MyFunction[0].GetValue;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InheritedMethodWithNoCodeRule"));
  }

  @Test
  void testIncreasedVisibilityShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBase = class(TObject)")
            .appendDecl("  protected")
            .appendDecl("    procedure IncreaseVisibilityProc;")
            .appendDecl("  public")
            .appendDecl("    procedure NotVirtualProc;")
            .appendDecl("    procedure VirtualProc; virtual;")
            .appendDecl("  end;")
            .appendDecl("  TChild = class(TBase)")
            .appendDecl("  public")
            .appendDecl("    procedure IncreaseVisibilityProc;")
            .appendDecl("    procedure NotVirtualProc; virtual;")
            .appendDecl("    procedure VirtualProc; reintroduce;")
            .appendDecl("  end;")
            .appendImpl("procedure TChild.IncreaseVisibilityProc;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;")
            .appendImpl("procedure TChild.NotVirtualProc;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;")
            .appendImpl("procedure TChild.VirtualProc;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InheritedMethodWithNoCodeRule"));
  }
}
