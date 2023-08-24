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

class RaisingRawExceptionCheckTest {
  @Test
  void testCustomExceptionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RaisingRawExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  raise MyCustomException.Create;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRawExceptionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RaisingRawExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  raise Exception.Create;")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testRaisingInvalidExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RaisingRawExceptionCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  raise (1 + 2);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
