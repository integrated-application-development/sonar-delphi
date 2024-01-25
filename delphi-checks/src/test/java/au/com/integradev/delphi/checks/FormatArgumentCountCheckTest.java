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
import org.junit.jupiter.api.Test;

class FormatArgumentCountCheckTest {
  private DelphiTestUnitBuilder sysUtils() {
    return new DelphiTestUnitBuilder()
        .unitName("System.SysUtils")
        .appendDecl("function Format(const Format: string; const Args: array of const): string;");
  }

  @Test
  void testCorrectArgumentsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentCountCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('%s %s %s %s', ['foo', 'bar', 'baz', 'flarp']);"))
        .verifyNoIssues();
  }

  @Test
  void testTooFewArgumentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentCountCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('%s %s %s %s', ['foo', 'bar']); // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testTooManyArgumentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentCountCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('%s %s', ['foo', 'bar', 'baz']); // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testTooFewArgumentsIncludingFloatingPointSpecifiersShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentCountCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('%s %*.*f', ['foo', 2]); // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testTooFewArgumentsIncludingIndexSpecifiersShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new FormatArgumentCountCheck())
        .withStandardLibraryUnit(sysUtils())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses System.SysUtils;")
                .appendImpl("initialization")
                .appendImpl("  Format('%s %3:d', ['foo', 2]); // Noncompliant"))
        .verifyIssues();
  }
}
