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
package org.sonar.plugins.communitydelphi.pmd.rules;

import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.communitydelphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.communitydelphi.utils.builders.DelphiTestUnitBuilder;

class VisibilityKeywordIndentationRuleTest extends BasePmdRuleTest {
  @ParameterizedTest
  @ValueSource(
      strings = {
        "class",
        "record",
        "object",
        "class helper for TObject",
        "record helper for string"
      })
  void testTooIndentedVisibilitySpecifierShouldRaiseIssue(String structType) {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = " + structType)
        .appendDecl("    public")
        .appendDecl("    procedure Proc;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("VisibilityKeywordIndentationRule", builder.getOffsetDecl() + 3));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "class",
        "record",
        "object",
        "class helper for TObject",
        "record helper for string"
      })
  void testCorrectlyIndentedVisibilitySpecifierShouldNotRaiseIssue(String structType) {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = " + structType)
        .appendDecl("  protected")
        .appendDecl("    procedure Proc;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VisibilityKeywordIndentationRule"));
  }

  @Test
  void testImplicitPublishedVisibilitySectionShouldNotRaiseIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("    procedure Bar;")
        .appendDecl("    procedure Baz;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VisibilityKeywordIndentationRule"));
  }

  @Test
  void testUnindentedVisibilitySpecifierShouldRaiseIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("strict private")
        .appendDecl("    procedure Proc;")
        .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("VisibilityKeywordIndentationRule", builder.getOffsetDecl() + 3));
  }
}
