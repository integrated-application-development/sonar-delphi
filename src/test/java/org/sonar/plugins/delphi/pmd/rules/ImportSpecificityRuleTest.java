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

class ImportSpecificityRuleTest extends BasePmdRuleTest {

  @Test
  void testImportUsedInImplementationShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.UITypes;")
            .appendImpl("type")
            .appendImpl("  Alias = System.UITypes.TMsgDlgType;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ImportSpecificityRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testImportUsedInInterfaceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  System.UITypes;")
            .appendDecl("type")
            .appendDecl("  Alias = System.UITypes.TMsgDlgType;");

    execute(builder);

    assertIssues().areNot(ruleKey("ImportSpecificityRule"));
  }

  @Test
  void testUnusedImportShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("uses System.UITypes;");

    execute(builder);

    assertIssues().areNot(ruleKey("ImportSpecificityRule"));
  }
}
