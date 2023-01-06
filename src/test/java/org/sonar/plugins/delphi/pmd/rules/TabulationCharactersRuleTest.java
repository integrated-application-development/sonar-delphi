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

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class TabulationCharactersRuleTest extends BasePmdRuleTest {

  @Test
  void testRegularFileShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    execute(builder);

    assertIssues().areNot(ruleKey("TabulationCharactersRule"));
  }

  @Test
  void testFileWithTabsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendDecl("\t");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("TabulationCharactersRule"));
  }

  @Test
  void testFileWithMultipleTabsShouldAddOnlyOneIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("\t")
            .appendDecl("var")
            .appendDecl("\t\tGBoolean:\tBoolean;")
            .appendDecl("\t");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("TabulationCharactersRule"));
  }
}
