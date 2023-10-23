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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddressOfCharacterDataCheckTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "@S[1]",
        "@(S[1])",
        "@S[(1)]",
        "@S[Integer(@S[1])]",
      })
  void testAddressOfStringIndexedWithExpressionOfOneShouldAddIssue(String expr) {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfCharacterDataCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  S: String;")
                .appendImpl("begin")
                .appendImpl("  " + expr + " // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Pointer(S)",
        "PChar(S)",
      })
  void testStringToPointerCastsShouldNotAddIssue(String expr) {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfCharacterDataCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  S: String;")
                .appendImpl("begin")
                .appendImpl("  " + expr + " // Noncompliant")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "String",
        "UnicodeString",
        "AnsiString",
        "ShortString",
      })
  void testAddressOfAllStringTypesIndexedWithOneShouldAddIssue(String type) {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfCharacterDataCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  S: " + type + ";")
                .appendImpl("begin")
                .appendImpl("  @S[1]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testAddressOfStringIndexedWithZeroWhileZeroBasedStringsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfCharacterDataCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  S: String;")
                .appendImpl("begin")
                .appendImpl("  @S[{$ZEROBASEDSTRINGS ON} 0] // Noncompliant")
                // This isn't quite correct, because the compiler actually effects the switch
                // directive at the start of the [] context. We don't model that correctly yet.
                .appendImpl("  @S[0 {$ZEROBASEDSTRINGS OFF}] // Noncompliant")
                .appendImpl("  @S[1] // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
