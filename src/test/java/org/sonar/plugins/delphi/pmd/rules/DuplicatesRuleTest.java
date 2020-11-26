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

class DuplicatesRuleTest extends BasePmdRuleTest {

  @Test
  void testSortedOnNextLineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testSortedOnPreviousLineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Sorted := True;")
            .appendImpl("  List.Duplicates := dupError;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testSortedEarlierInBlockLineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function MyFunc: Boolean;")
            .appendImpl("begin")
            .appendImpl("  List.Sorted := True;")
            .appendImpl("  Result := True;")
            .appendImpl("  List.Duplicates := dupError;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testUnsortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testUnsortedWithoutSemicolonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testSortedFalseOnPreviousLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Sorted := False;")
            .appendImpl("  List.Duplicates := dupError;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 4));
  }

  @Test
  void testSortedFalseOnNextLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := False;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testSortedDifferentListOnPreviousLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  OtherList.Sorted := False;")
            .appendImpl("  List.Duplicates := dupError;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 4));
  }

  @Test
  void testSortedDifferentListOnNextLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  OtherList.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := False;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testDupAcceptShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupAccept;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testUnassignedDuplicatesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testUnassignedSortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testStandaloneSortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  Sorted;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testProcedureOnNextLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.SomeProcedure")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testQualifiedSortedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  SomeClass.List.Duplicates := dupIgnore;")
            .appendImpl("  SomeClass.List.Sorted := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testQualifiedNotSortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  SomeClass.List.Duplicates := dupIgnore;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testQualifiedTrueShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := System.True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testQualifiedFalseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := System.False;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }

  @Test
  void testSettingWrongPropertyShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.X := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DuplicatesRule", builder.getOffset() + 3));
  }
}
