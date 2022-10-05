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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ExplicitDefaultPropertyReferenceRuleTest extends BasePmdRuleTest {
  @Test
  void testImplicitDefaultPropertyAccessShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    function GetBar: TObject;")
            .appendDecl("    property Bar: TObject read GetBar; default;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Bar: TObject;")
            .appendImpl("begin")
            .appendImpl("  Bar := Foo[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ExplicitDefaultPropertyReferenceRule"));
  }

  @Test
  void testExplicitDefaultPropertyAccessShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    function GetBar: TObject;")
            .appendDecl("    property Bar[Index: Integer]: TObject read GetBar; default;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Bar: TObject;")
            .appendImpl("begin")
            .appendImpl("  Bar := Foo.Bar[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("ExplicitDefaultPropertyReferenceRule", builder.getOffset() + 5));
  }

  @Test
  void testExplicitDefaultPropertyAccessOnSelfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class")
            .appendDecl("    function GetBar: TObject;")
            .appendDecl("    procedure Test(Foo: TFoo);")
            .appendDecl("    property Bar[Index: Integer]: TObject read GetBar; default;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Obj: TObject;")
            .appendImpl("begin")
            .appendImpl("  Obj := Bar[0];")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ExplicitDefaultPropertyReferenceRule"));
  }
}
