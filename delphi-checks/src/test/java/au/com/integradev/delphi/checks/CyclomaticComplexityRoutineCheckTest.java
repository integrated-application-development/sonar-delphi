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

class CyclomaticComplexityRoutineCheckTest {
  @Test
  void testSimpleRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CyclomaticComplexityRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: Integer;") // 1
                .appendImpl("begin")
                .appendImpl("  if Foo then Bar;") // 2
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAlmostTooComplexRoutineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin");

    for (int i = 1; i <= 19; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 20
    }

    builder.appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new CyclomaticComplexityRoutineCheck())
        .onFile(builder)
        .verifyNoIssues();
  }

  @Test
  void testTooComplexRoutineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer; // Noncompliant") // 1
            .appendImpl("begin");

    for (int i = 1; i <= 20; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 21
    }

    builder.appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new CyclomaticComplexityRoutineCheck())
        .onFile(builder)
        .verifyIssues();
  }

  @Test
  void testTooComplexNestedRoutineeShouldOnlyAddIssueForNestedRoutine() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("  function Bar: Integer; // Noncompliant") // 1
            .appendImpl("  begin");

    for (int i = 1; i <= 20; ++i) {
      builder.appendImpl("    if Foo then Bar;"); // 21
    }

    builder
        .appendImpl("  end;")
        .appendImpl("begin")
        .appendImpl("Result := Bar;")
        .appendImpl("end;");

    CheckVerifier.newVerifier()
        .withCheck(new CyclomaticComplexityRoutineCheck())
        .onFile(builder)
        .verifyIssues();
  }
}
