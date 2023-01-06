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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class VisibilitySectionOrderRuleTest extends BasePmdRuleTest {
  @ParameterizedTest(name = "[{index}] {0} before {1}")
  @CsvSource({
    "private,strict private",
    "protected,private",
    "protected,strict protected",
    "published,public",
    "published,private",
  })
  void testOutOfOrderSectionsShouldAddIssue(String firstVisibility, String secondVisibility) {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  " + firstVisibility)
        .appendDecl("    procedure Bar;")
        .appendDecl("    property Baz: Integer;")
        .appendDecl("  " + secondVisibility)
        .appendDecl("    procedure Bar2;")
        .appendDecl("    property Baz2: Integer;")
        .appendDecl(" end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VisibilitySectionOrderRule", builder.getOffsetDecl() + 6))
        .areExactly(1, ruleKey("VisibilitySectionOrderRule"));
  }

  @ParameterizedTest(name = "[{index}] {0} before {1}")
  @CsvSource({
    "strict private,private",
    "private,protected",
    "strict protected,protected",
    "public,published",
    "private,published",
  })
  void testOrderedSectionsShouldNotAddIssue(String firstVisibility, String secondVisibility) {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  " + firstVisibility)
        .appendDecl("    procedure Bar;")
        .appendDecl("    property Baz: Integer;")
        .appendDecl("  " + secondVisibility)
        .appendDecl("    procedure Bar2;")
        .appendDecl("    property Baz2: Integer;")
        .appendDecl(" end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VisibilitySectionOrderRule"));
  }

  @Test
  void testMultipleOfTheSameSectionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    procedure Bar;")
        .appendDecl("    property Baz: Integer;")
        .appendDecl("  public")
        .appendDecl("    procedure Bar2;")
        .appendDecl("    property Baz2: Integer;")
        .appendDecl(" end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VisibilitySectionOrderRule"));
  }

  @Test
  void testMultipleOutOfOrderSectionsShouldAddMultipleIssues() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("  public")
        .appendDecl("    procedure Baz;")
        .appendDecl("  private")
        .appendDecl("    procedure Bee;")
        .appendDecl("  protected")
        .appendDecl("    procedure Bee;")
        .appendDecl("  published")
        .appendDecl("    procedure Bee;")
        .appendDecl(" end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VisibilitySectionOrderRule", builder.getOffsetDecl() + 5))
        .areExactly(1, ruleKeyAtLine("VisibilitySectionOrderRule", builder.getOffsetDecl() + 7))
        .areExactly(2, ruleKey("VisibilitySectionOrderRule"));
  }

  @Test
  void testImplicitPublishedFollowedByPrivateShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = class(TObject)")
        .appendDecl("    procedure Bar;")
        .appendDecl("  private")
        .appendDecl("    procedure Baz;")
        .appendDecl(" end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VisibilitySectionOrderRule"));
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
  void testAllStructTypesWithOrderedVisibilitySectionsShouldAddIssue(String structType) {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder
        .appendDecl("type")
        .appendDecl("  TFoo = " + structType)
        .appendDecl("  public")
        .appendDecl("    procedure Bar;")
        .appendDecl("  protected")
        .appendDecl("    procedure Baz;")
        .appendDecl(" end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("VisibilitySectionOrderRule", builder.getOffsetDecl() + 5));
  }
}
