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

class CognitiveComplexityMethodCheckTest {

  @Test
  void testSimpleMethod() {
    CheckVerifier.newVerifier()
        .withCheck(new CognitiveComplexityMethodCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: Integer;")
                .appendImpl("begin")
                .appendImpl("  if Foo then Bar;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testTooComplexMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder() //
            .appendImpl("function Foo: Integer; // Noncompliant")
            .appendImpl("begin");

    for (int i = 1; i <= 16; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 16
    }

    builder.appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new CognitiveComplexityMethodCheck())
        .onFile(builder)
        .verifyIssues();
  }

  @Test
  void testTooComplexSubProcedureShouldOnlyAddIssueForSubProcedure() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("  function Bar: Integer; // Noncompliant")
            .appendImpl("  begin");

    for (int i = 1; i <= 16; ++i) {
      builder.appendImpl("    if Foo then Bar;"); // 16
    }

    builder
        .appendImpl("  end;")
        .appendImpl("begin")
        .appendImpl("Result := Bar;")
        .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new CognitiveComplexityMethodCheck())
        .onFile(builder)
        .verifyIssues();
  }
}
