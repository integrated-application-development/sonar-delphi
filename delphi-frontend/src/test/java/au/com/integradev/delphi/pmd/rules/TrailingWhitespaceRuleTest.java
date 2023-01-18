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

class TrailingWhitespaceRuleTest extends BasePmdRuleTest {
  @Test
  void testTrailingSpaceShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("var Foo: TObject; ");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TrailingWhitespaceRule", builder.getOffset() + 1));
  }

  @Test
  void testTrailingTabShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;\t");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TrailingWhitespaceRule", builder.getOffset() + 1));
  }

  @Test
  void testTrailingMixedWhitespaceShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;\t   \t\t \t  ");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TrailingWhitespaceRule", builder.getOffset() + 1));
  }

  @Test
  void testNoTrailingWhitespaceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;");

    execute(builder);

    assertIssues().areNot(ruleKey("TrailingWhitespaceRule"));
  }

  @Test
  void testLeadingWhitespaceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("\t   \t \t var Foo: TObject;");

    execute(builder);

    assertIssues().areNot(ruleKey("TrailingWhitespaceRule"));
  }
}
