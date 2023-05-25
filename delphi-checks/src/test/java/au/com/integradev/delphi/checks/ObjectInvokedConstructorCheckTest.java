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

class ObjectInvokedConstructorCheckTest extends CheckTest {

  @Test
  void testConstructorInvokedOnObjectShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ObjectInvokedConstructorRule", builder.getOffset() + 5));
  }

  @Test
  void testConstructorInvokedOnTypeIdentifierShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := TObject.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testConstructorInvokedOnClassReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("  Clazz: TClass;")
            .appendImpl("begin")
            .appendImpl("  Obj := Clazz.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testConstructorInvokedOnSelfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("    procedure Test;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Test;")
            .appendImpl("begin")
            .appendImpl("  Self.Create;")
            .appendImpl("  Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testBareInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testNamedInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  inherited Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }

  @Test
  void testQualifiedInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;")
            .appendImpl("constructor TFoo.Create;")
            .appendImpl("begin")
            .appendImpl("  inherited.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ObjectInvokedConstructorRule"));
  }
}
