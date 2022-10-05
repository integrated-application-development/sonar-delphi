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

class TooLongLineRuleTest extends BasePmdRuleTest {
  @Test
  void testShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TClass.Test;")
            .appendImpl("begin")
            .appendImpl("  FMessage := 'This line is not too long.';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TooLongLineRule"));
  }

  @Test
  void testTooLongLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TClass.Test;")
            .appendImpl("begin")
            .appendImpl(
                "  FMessage := 'This line is too long. Look, it''s running right off the screen!"
                    + " Who would do such a thing? I am horrified by the audacity of this line!';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TooLongLineRule", builder.getOffset() + 3));
  }

  @Test
  void testTrailingWhitespaceLineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TClass.Test;")
            .appendImpl("begin")
            .appendImpl(
                "  FMessage := 'This line is not too long, but there is trailing whitespace...';  "
                    + "                           ")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TooLongLineRule"));
  }
}
