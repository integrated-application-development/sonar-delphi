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

class ExplicitBitwiseNotCheckTest {
  @Test
  void testSimpleBitwiseNotShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := not 1;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testOtherIntegerUnaryOperatorsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := -1 in [2];")
                .appendImpl("  Result := +1 in [2];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBitwiseNotInSetContainmentCheckShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+2:5 to +2:5] <<(>>")
                .appendImpl("  // Fix@[+1:10 to +1:10] <<)>>")
                .appendImpl("  if not 3 in [3, 252] then // Noncompliant")
                .appendImpl("    Writeln('Foo');")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testBitwiseNotInBinaryExpressionShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := not 1 in [2]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testParenthesizedBitwiseNotInBinaryExpressionShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := (not 1) in [2];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBooleanNotShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := not (1 in [2]);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnresolvedNotShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := not UnresolvedIdentifier in [2];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIntegerProceduralTypeShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetMyInt: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := 1;")
                .appendImpl("end;")
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := not GetMyInt in [2]; // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testNonIntegerProceduralTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetMyStr: string;")
                .appendImpl("begin")
                .appendImpl("  Result := 'Hello world';")
                .appendImpl("end;")
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := not GetMyStr in [2];")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testParenthesizedIntegerProceduralTypeShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ExplicitBitwiseNotCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function GetMyInt: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := 1;")
                .appendImpl("end;")
                .appendImpl("function Test: Integer;")
                .appendImpl("begin")
                .appendImpl("  Result := not (GetMyInt in [2]);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
