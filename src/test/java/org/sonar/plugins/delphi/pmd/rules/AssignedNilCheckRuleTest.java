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

class AssignedNilCheckRuleTest extends BasePmdRuleTest {

  @Test
  void testAssignedCheckShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo: TObject);")
            .appendImpl("begin")
            .appendImpl("  if Assigned(Foo) then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AssignedNilCheckRule"));
  }

  @Test
  void testNonVariablesShouldNotAddIssues() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBaz = class(TObject)")
            .appendDecl("    function Baz: String;")
            .appendDecl("  end;")
            .appendImpl("function Bar: TObject;")
            .appendImpl("begin")
            .appendImpl("end;")
            .appendImpl("procedure Test(BazVar: TBaz);")
            .appendImpl("begin")
            .appendImpl("  if BazVar.Baz = nil then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if Bar = nil then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AssignedNilCheckRule"));
  }

  @Test
  void testRegularComparisonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo: TObject; Bar: TObject);")
            .appendImpl("begin")
            .appendImpl("  if Foo = Bar then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if Foo <> Bar then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AssignedNilCheckRule"));
  }

  @Test
  void testNilComparisonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TBar = class(TObject)")
            .appendDecl("    class var FBar: TObject")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TObject);")
            .appendImpl("begin")
            .appendImpl("  if TBar.FBar = nil then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if Foo = nil then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if Foo <> ((nil)) then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if (nil) = Foo then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if nil <> Foo then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 3))
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 9))
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 12))
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 15));
  }
}
