/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

class FormatStringValidCheckTest {
  private DelphiTestUnitBuilder sysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("function Format(const Format: string; const Args: array of const): string;");
  }

  @ParameterizedTest
  @ValueSource(strings = {"%s %d %*.*f %0:e %*.f", "I have no specifiers", ""})
  void testValidFormatStringShouldNotAddIssue(String formatString) {
    CheckVerifier.newVerifier()
        .withCheck(new FormatStringValidCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('" + formatString + "', []);"))
        .verifyNoIssues();
  }

  @ParameterizedTest
  @ValueSource(strings = {"%z", "%.af", "%0", "%0:", "%1.%d", "%*", "%*:d"})
  void testInvalidFormatStringShouldAddIssue(String formatString) {
    CheckVerifier.newVerifier()
        .withCheck(new FormatStringValidCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format(")
                .appendImpl("    '" + formatString + "', // Noncompliant")
                .appendImpl("  []);"))
        .verifyIssues();
  }
}
