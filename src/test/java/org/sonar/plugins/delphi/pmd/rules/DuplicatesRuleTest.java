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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class DuplicatesRuleTest extends BasePmdRuleTest {

  @Test
  public void testSortedOnNextLineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testSortedOnPreviousLineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Sorted := True;")
            .appendImpl("  List.Duplicates := dupError;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testUnsortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testUnsortedWithoutSemicolonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testSortedFalseOnPreviousLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Sorted := True;")
            .appendImpl("  List.Sorted := False;")
            .appendImpl("  List.Duplicates := dupError;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 5)));
  }

  @Test
  public void testSortedFalseOnNextLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := False;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testSortedDifferentListOnPreviousLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  OtherList.Sorted := False;")
            .appendImpl("  List.Duplicates := dupError;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 4)));
  }

  @Test
  public void testSortedDifferentListOnNextLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  OtherList.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted := False;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testDupAcceptShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupAccept;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testUnassignedDuplicatesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testUnassignedSortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.Sorted")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testStandaloneSortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  Sorted;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testProcedureOnNextLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  List.Duplicates := dupIgnore;")
            .appendImpl("  List.SomeProcedure")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testQualifiedSortedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  SomeClass.List.Duplicates := dupIgnore;")
            .appendImpl("  SomeClass.List.Sorted := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testQualifiedNotSortedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  SomeClass.List.Duplicates := dupIgnore;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("DuplicatesRule", builder.getOffSet() + 3)));
  }
}
