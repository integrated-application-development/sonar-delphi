/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DigitGroupingCheckTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1_0",
        "1_.0e5",
        "1_000000",
        "100_000_0",
        "100_000_",
        "$ABC_DEF",
        "$ABC_DE_F",
        "%1_001_01",
        "&1_000_00",
        "&&&1_000_00",
        "&10_00.00",
        "&&&10_00.00",
      })
  void testNonStandardGroupingShouldAddIssue(String actual) {
    CheckVerifier.newVerifier()
        .withCheck(new DigitGroupingCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("const A = " + actual + "; // Noncompliant"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1_000",
        "100_000",
        "1_000_000",
        "1_000_000.0",
        "1_000_000.0001",
        "1_000_000.0_0_01",
        "10_000.0e5",
        "10_000.0e50001",
        "$AB_CDEF",
        "$ab_cdef",
        "$AB_CD_EF",
        "%1_0101",
        "%1_01_01",
        "%10_101",
        "%10_1010",
        "&100_000",
        "&&&100_000",
        "&1_000.0_0",
        "&&&1_000.0_0",
      })
  void testStandardGroupingShouldNotAddIssue(String actual) {
    CheckVerifier.newVerifier()
        .withCheck(new DigitGroupingCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("const A = " + actual + ";"))
        .verifyNoIssues();
  }
}
