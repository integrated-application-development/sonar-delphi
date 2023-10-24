/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

class SuperfluousSemicolonCheckTest {
  @Test
  void testRegularSemicolonsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SuperfluousSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("begin")
                .appendImpl("  SomeVar := 5;")
                .appendImpl("  if SomeVar = 5 then begin")
                .appendImpl("    SomeVar := 6;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testStraySemicolonsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new SuperfluousSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl(";")
                .appendImpl("  // Noncompliant@+3")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  ;SomeVar := 5;; ;")
                .appendImpl("  if SomeVar = 5 then begin")
                .appendImpl("    // Noncompliant@+2")
                .appendImpl("    // Noncompliant@+1")
                .appendImpl("    ;SomeVar := 6;;")
                .appendImpl("    // Noncompliant@+1")
                .appendImpl("    ;")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  end;;")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testStraySemicolonsShouldAddQuickFixes() {
    CheckVerifier.newVerifier()
        .withCheck(new SuperfluousSemicolonCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure SemicolonTest;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl(";")
                .appendImpl("  // Fix qf1@[-1:0 to -1:1] <<>>")
                .appendImpl("  // Noncompliant@+3")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  ;SomeVar := 5;; ;")
                .appendImpl("  // Fix qf2@[-1:2 to -1:3] <<>>")
                .appendImpl("  // Fix qf3@[-2:16 to -2:17] <<>>")
                .appendImpl("  // Fix qf4@[-3:18 to -3:19] <<>>")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
