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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class DestructorWithoutInheritedStatementRuleTest extends BasePmdRuleTest {

  @Test
  void testValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TTestDestructor = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    destructor Destroy; override;")
            .appendDecl("  end;")
            .appendImpl("destructor TTestDestructor.Destroy;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("  WriteLn('do something');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("DestructorWithoutInheritedStatementRule"));
  }

  @Test
  void testDestructorMissingInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TTestDestructor = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    destructor Destroy; override;")
            .appendDecl("  end;")
            .appendImpl("destructor TTestDestructor.Destroy;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('do something');")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("DestructorWithoutInheritedStatementRule", builder.getOffset() + 1));
  }

  @Test
  public void testClassDestructorShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TTestDestructor = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    class destructor Destroy; override;")
            .appendDecl("  end;")
            .appendImpl("class destructor TTestDestructor.Destroy;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('do something');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("DestructorWithoutInheritedStatementRule"));
  }
}
