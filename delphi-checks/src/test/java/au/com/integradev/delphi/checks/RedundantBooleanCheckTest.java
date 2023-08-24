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

class RedundantBooleanCheckTest {
  @Test
  void testRedundantBooleanComparisonShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantBooleanCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("var")
                .appendImpl("  X: Boolean;")
                .appendImpl("begin")
                .appendImpl("  if X = True then begin")
                .appendImpl("    DoSomething;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testBooleanComparisonImplicitConversionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantBooleanCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("var")
                .appendImpl("  X: Variant;")
                .appendImpl("begin")
                .appendImpl("  if X = True then begin")
                .appendImpl("    DoSomething;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testRedundantBooleanNegativeComparisonShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantBooleanCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("var")
                .appendImpl("  X: Boolean;")
                .appendImpl("begin")
                .appendImpl("  if X <> False then begin")
                .appendImpl("    DoSomething;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testRedundantNestedBooleanComparisonShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantBooleanCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure MyProcedure;")
                .appendImpl("var")
                .appendImpl("  X: Boolean;")
                .appendImpl("begin")
                .appendImpl("  if ((((X))) = (((True)))) then begin")
                .appendImpl("    DoSomething;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }

  @Test
  void testNeedlesslyInvertedBooleanShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new RedundantBooleanCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure Foo(Bar: Boolean);")
                .appendImpl("procedure Baz;")
                .appendImpl("begin")
                .appendImpl("  Foo(not True);")
                .appendImpl("end;"))
        .verifyIssueOnLine(11);
  }
}
