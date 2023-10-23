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

class DigitSeparatorCheckTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "100000",
        "1000000",
        "1000000.0",
        "10000.0e5",
        "10000.0_0",
        "$ABCDEF",
        "%101010",
      })
  void testDefaultMaximumShouldAddIssue(String actual) {
    CheckVerifier.newVerifier()
        .withCheck(new DigitSeparatorCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("const A = " + actual + "; // Noncompliant"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1000000000",
        "$A000000000",
        "%1000000000",
      })
  void testGreaterMaximumShouldNotAddIssue(String actual) {
    var check = new DigitSeparatorCheck();
    check.maxDigitsWithoutUnderscores = 10;

    CheckVerifier.newVerifier()
        .withCheck(check)
        .onFile(new DelphiTestUnitBuilder().appendImpl("const A = " + actual + "; // Noncompliant"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "1000",
        "10_00",
        "10_000",
        "1_0000",
        "1000.0",
        "10000_.0",
        "1.00001",
        "1.0000_1",
        "1.0e10001",
        "$A000",
        "$A_0000",
        "%1000",
        "%1_0000",
      })
  void testDefaultMaximumShouldNotAddIssue(String actual) {
    CheckVerifier.newVerifier()
        .withCheck(new DigitSeparatorCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("const")
                .appendImpl("  A = " + actual + "; // Noncompliant"))
        .verifyNoIssues();
  }
}
