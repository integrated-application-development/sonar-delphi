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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class MethodNestingDepthRuleTest extends BasePmdRuleTest {
  @Test
  void testShallowNestedMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Outer;")
            .appendImpl("  procedure Inner;")
            .appendImpl("  begin")
            .appendImpl("    // Nesting level: 1")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  // Nesting level: 0")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodNestingDepthRule"));
  }

  @Test
  void testDeeplyNestedMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Outer;")
            .appendImpl("  procedure Inner;")
            .appendImpl("    procedure Innerest;")
            .appendImpl("    begin")
            .appendImpl("      // Nesting level: 2")
            .appendImpl("    end;")
            .appendImpl("  begin")
            .appendImpl("    // Nesting level: 1")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  // Nesting level: 0")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MethodNestingDepthRule", builder.getOffset() + 3));
  }
}
