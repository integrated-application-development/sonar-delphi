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

class TooLongLineCheckTest {
  @Test
  void testShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooLongLineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure TClass.Test;")
                .appendImpl("begin")
                .appendImpl("  FMessage := 'This line is not too long.';")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testTooLongLineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooLongLineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure TClass.Test;")
                .appendImpl("begin")
                .appendImpl(
                    "  FMessage := 'This line is too long. Look, it''s running right off the"
                        + " screen! Who would do such a thing? I am horrified by the audacity of"
                        + " this line!';")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testTrailingWhitespaceLineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new TooLongLineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure TClass.Test;")
                .appendImpl("begin")
                .appendImpl(
                    "  FMessage := 'This line is not too long, but there is trailing whitespace.';"
                        + "                               ")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
