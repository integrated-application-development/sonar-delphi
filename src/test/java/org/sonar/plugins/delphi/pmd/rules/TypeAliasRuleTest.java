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

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class TypeAliasRuleTest extends BasePmdRuleTest {

  @Test
  void testTypeAliasShouldAddViolation() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyChar = Char;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TypeAliasRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testTypeAliasNewTypeShouldAddViolation() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyChar = type Char;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TypeAliasRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testFalsePositiveMetaClassIsNotTypeAlias() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("  end;")
            .appendDecl("  TMetaClass = class of TMyClass;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFalsePositiveEmptyRecordIsNotTypeAlias() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyRecord = record")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFalsePositiveEmptyClassIsNotTypeAlias() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFalsePositiveSetsAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TSetOfChar = set of Char;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFalsePositiveArraysAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TArrayOfChar = array of Char;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFalsePositiveSubRangesAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TSubRange = Lower..Upper;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFalsePositivePointerTypesAreNotTypeAlias() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  PClass = ^TClass;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
