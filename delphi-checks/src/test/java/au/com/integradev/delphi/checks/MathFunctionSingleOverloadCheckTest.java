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

class MathFunctionSingleOverloadCheckTest {
  @Test
  void testExtendedOverloadShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MathFunctionSingleOverloadCheck())
        .withSearchPathUnit(createSystemMath())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Math;")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  Power(1.0, Integer(1));")
                .appendImpl("  IntPower(1.0, 1);")
                .appendImpl("  IntPower(Extended(1.0), 1);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testDoubleOverloadShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MathFunctionSingleOverloadCheck())
        .withSearchPathUnit(createSystemMath())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Math;")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  IntPower(Double(1.0), 1);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testSingleOverloadShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new MathFunctionSingleOverloadCheck())
        .withSearchPathUnit(createSystemMath())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  System.Math;")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  IntPower(Single(1.0), 1); // Noncompliant")
                .appendImpl("  IntPower(1, 1); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  private static DelphiTestUnitBuilder createSystemMath() {
    return new DelphiTestUnitBuilder()
        .unitName("System.Math")
        .appendDecl(
            "function IntPower(const Base: Single; const Exponent: Integer): Single; overload;")
        .appendDecl(
            "function IntPower(const Base: Double; const Exponent: Integer): Double; overload;")
        .appendDecl(
            "function IntPower(const Base: Extended; const Exponent: Integer): Extended; overload;")
        .appendDecl("function Power(const Base, Exponent: Extended): Extended; overload;")
        .appendDecl("function Power(const Base, Exponent: Double): Double; overload;")
        .appendDecl("function Power(const Base, Exponent: Single): Single; overload;");
  }
}
