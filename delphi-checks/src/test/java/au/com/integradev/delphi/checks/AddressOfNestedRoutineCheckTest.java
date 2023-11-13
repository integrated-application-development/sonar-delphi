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

class AddressOfNestedRoutineCheckTest {
  @Test
  void testAddressOfRegularRoutineShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfNestedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  ProcVar: Pointer;")
                .appendDecl("function ProcRoutine(ProcVar: Pointer);")
                .appendImpl("procedure RegularRoutine(Str: String);")
                .appendImpl("begin")
                .appendImpl("  Exit;")
                .appendImpl("end;")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  ProcVar := @RegularRoutine;")
                .appendImpl("  ProcRoutine(@RegularRoutine);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAddressOfVariableShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfNestedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  ProcVar: Pointer;")
                .appendDecl("function ProcRoutine(Ptr: Pointer);")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Str: String;")
                .appendImpl("begin")
                .appendImpl("  ProcVar := @Str;")
                .appendImpl("  ProcRoutine(@Str);")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAddressOfLiteralShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfNestedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("function ProcRoutine(Ptr: Pointer);")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  ProcVar := @'foo';")
                .appendImpl("  ProcRoutine(@'bar');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAddressOfNestedRoutineResultShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfNestedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  ProcVar: Pointer;")
                .appendDecl("function ProcRoutine(ProcVar: Pointer);")
                .appendImpl("procedure Test;")
                .appendImpl("  function Nested(Str: String): String;")
                .appendImpl("  begin")
                .appendImpl("    Result := Str;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  ProcVar := @Nested('');")
                .appendImpl("  ProcRoutine(@Nested(''));")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAddressOfNestedRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new AddressOfNestedRoutineCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  ProcVar: Pointer;")
                .appendDecl("function ProcRoutine(ProcVar: Pointer);")
                .appendImpl("procedure Test;")
                .appendImpl("  procedure Nested(Str: String);")
                .appendImpl("  begin")
                .appendImpl("    Exit;")
                .appendImpl("  end;")
                .appendImpl("begin")
                .appendImpl("  ProcVar := @Nested; // Noncompliant")
                .appendImpl("  ProcRoutine(@Nested); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
