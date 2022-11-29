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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder.ResourceBuilder;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnitLevelKeywordIndentationRuleTest extends BasePmdRuleTest {
  private static final String RESOURCE_PATH =
      "/org/sonar/plugins/delphi/pmd/rules/UnitLevelKeywordIndentationRule";
  private static final String CORRECT_PAS = RESOURCE_PATH + "/Correct.pas";
  private static final String INDENTED_PAS = RESOURCE_PATH + "/Indented.pas";
  private static final String CORRECT_DPR = RESOURCE_PATH + "/Correct.dpr";
  private static final String INDENTED_DPR = RESOURCE_PATH + "/Indented.dpr";

  @Test
  void testIndentedProgramShouldAddIssue() {
    DelphiTestFileBuilder<ResourceBuilder> builder =
        DelphiTestFileBuilder.fromResource(INDENTED_DPR);

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 1))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 3))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 8));
  }

  @Test
  void testIndentedHeadingsShouldAddIssue() {
    DelphiTestFileBuilder<ResourceBuilder> builder =
        DelphiTestFileBuilder.fromResource(INDENTED_PAS);

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 1))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 3))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 13))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 23))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 25));
  }

  @Test
  void testIndentedFinalEndShouldAddIssue() {
    DelphiTestFileBuilder<ResourceBuilder> builder =
        DelphiTestFileBuilder.fromResource(INDENTED_PAS);

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 27));
  }

  @Test
  void testIndentedUsesShouldAddIssue() {
    DelphiTestFileBuilder<ResourceBuilder> builder =
        DelphiTestFileBuilder.fromResource(INDENTED_PAS);

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 5))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 15));
  }

  @Test
  void testIndentedTypeShouldAddIssue() {
    DelphiTestFileBuilder<ResourceBuilder> builder =
        DelphiTestFileBuilder.fromResource(INDENTED_PAS);

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 9))
        .areExactly(1, ruleKeyAtLine("UnitLevelKeywordIndentationRule", 19));
  }

  @Test
  void testIndentedInnerTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TObject = class(TObject)")
        .appendDecl("    type TInnerObject = class(TObject)")
        .appendDecl("    end;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("UnitLevelKeywordIndentationRule", builder.getOffsetDecl() + 3));
  }

  @ParameterizedTest
  @ValueSource(strings = {CORRECT_PAS, CORRECT_DPR})
  void testUnindentedKeywordsShouldNotAddIssue(String correctFile) {
    DelphiTestFileBuilder<ResourceBuilder> builder =
        DelphiTestFileBuilder.fromResource(correctFile);

    execute(builder);

    assertIssues().areNot(ruleKey("UnitLevelKeywordIndentationRule"));
  }
}
