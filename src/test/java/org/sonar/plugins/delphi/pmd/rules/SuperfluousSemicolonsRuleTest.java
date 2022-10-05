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

class SuperfluousSemicolonsRuleTest extends BasePmdRuleTest {

  @Test
  void testRegularSemicolonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("begin")
            .appendImpl("  SomeVar := 5;")
            .appendImpl("  if SomeVar = 5 then begin")
            .appendImpl("    SomeVar := 6;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SuperfluousSemicolonsRule"));
  }

  @Test
  void testStraySemicolonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("begin")
            .appendImpl(";")
            .appendImpl("  ;SomeVar := 5;; ;")
            .appendImpl("  if SomeVar = 5 then begin")
            .appendImpl("    ;SomeVar := 6;;")
            .appendImpl("    ;")
            .appendImpl("  end;;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 3))
        .areExactly(3, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 4))
        .areExactly(2, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 7))
        .areExactly(1, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 8));
  }
}
