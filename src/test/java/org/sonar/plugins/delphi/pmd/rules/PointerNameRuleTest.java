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

import static org.sonar.plugins.delphi.utils.conditions.AtLine.atLine;
import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class PointerNameRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  PInteger = ^Integer;")
            .appendDecl("  PFooInteger = ^TFooInteger;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInvalidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  pMyPointer = ^Integer;")
            .appendDecl("  PInteger = ^TFooInteger;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .are(ruleKey("PointerNameRule"))
        .areExactly(1, atLine(builder.getOffsetDecl() + 2))
        .areExactly(1, atLine(builder.getOffsetDecl() + 3));
  }

  @Test
  public void testBadCase() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  Pinteger = ^Integer;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("PointerNameRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testShouldIgnorePointerAssignment() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Foo;");
    builder.appendImpl("var");
    builder.appendImpl("  MyInteger: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  MyInteger := PInteger(1)^;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
