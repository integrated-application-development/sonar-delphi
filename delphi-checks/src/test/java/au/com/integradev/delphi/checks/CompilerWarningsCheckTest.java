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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class CompilerWarningsCheckTest {
  @Test
  void testWarningsOffShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CompilerWarningsCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("{$WARNINGS OFF}"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testWarningsOnShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CompilerWarningsCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("{$WARNINGS ON}"))
        .verifyNoIssues();
  }

  @Test
  void testWarnOffShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CompilerWarningsCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("{$WARN SYMBOL_DEPRECATED OFF}"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testWarnUnknownShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CompilerWarningsCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("{$WARN SYMBOL_DEPRECATED FOO}"))
        .verifyNoIssues();
  }

  @Test
  void testHintsOffShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CompilerWarningsCheck())
        .onFile(new DelphiTestUnitBuilder().appendImpl("{HINTS OFF}"))
        .verifyNoIssues();
  }
}
