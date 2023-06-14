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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class StringListDuplicatesCheckTest {
  @Test
  void testSortedOnNextLineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  List.Sorted := True;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSortedOnPreviousLineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Sorted := True;")
                .appendImpl("  List.Duplicates := dupError;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSortedEarlierInBlockLineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function MyFunc: Boolean;")
                .appendImpl("begin")
                .appendImpl("  List.Sorted := True;")
                .appendImpl("  Result := True;")
                .appendImpl("  List.Duplicates := dupError;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnsortedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testUnsortedWithoutSemicolonShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testSortedFalseOnPreviousLineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Sorted := False;")
                .appendImpl("  List.Duplicates := dupError;")
                .appendImpl("end;"))
        .verifyIssueOnLine(10);
  }

  @Test
  void testSortedFalseOnNextLineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  List.Sorted := False;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testSortedDifferentListOnPreviousLineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  OtherList.Sorted := False;")
                .appendImpl("  List.Duplicates := dupError;")
                .appendImpl("end;"))
        .verifyIssueOnLine(10);
  }

  @Test
  void testSortedDifferentListOnNextLineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  OtherList.Duplicates := dupIgnore;")
                .appendImpl("  List.Sorted := False;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testDupAcceptShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupAccept;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnassignedDuplicatesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnassignedSortedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  List.Sorted")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testStandaloneSortedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  Sorted;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testProcedureOnNextLineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  List.SomeProcedure")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testQualifiedSortedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  SomeClass.List.Duplicates := dupIgnore;")
                .appendImpl("  SomeClass.List.Sorted := True;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testQualifiedNotSortedShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  SomeClass.List.Duplicates := dupIgnore;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testQualifiedTrueShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  List.Sorted := System.True;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testQualifiedFalseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  List.Sorted := System.False;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testSettingWrongPropertyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new StringListDuplicatesCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("begin")
                .appendImpl("  List.Duplicates := dupIgnore;")
                .appendImpl("  List.X := True;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }
}
