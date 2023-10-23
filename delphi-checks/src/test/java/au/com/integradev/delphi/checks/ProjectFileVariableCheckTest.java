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

import au.com.integradev.delphi.builders.DelphiTestProgramBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class ProjectFileVariableCheckTest {
  @Test
  void testNoVariablesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ProjectFileVariableCheck())
        .onFile(
            new DelphiTestProgramBuilder() //
                .programName("ValidProgram")
                .appendImpl("Exit;"))
        .verifyNoIssues();
  }

  @Test
  void testVariablesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ProjectFileVariableCheck())
        .onFile(
            new DelphiTestProgramBuilder()
                .appendDecl("var")
                .appendDecl("  GMyString: String; // Noncompliant")
                .appendDecl("  GMyBool: Boolean; // Noncompliant")
                .appendDecl("  GMyInt: Integer; // Noncompliant")
                .appendImpl("Exit;"))
        .verifyIssues();
  }

  @Test
  void testVariablesWithinMethodsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ProjectFileVariableCheck())
        .onFile(
            new DelphiTestProgramBuilder()
                .appendDecl("procedure MyProcedure;")
                .appendDecl("var")
                .appendDecl("  MyString: String;")
                .appendDecl("  MyBool: Boolean;")
                .appendDecl("  MyInt: Integer;")
                .appendDecl("begin")
                .appendDecl("  DoSomething;")
                .appendDecl("end;")
                .appendDecl("function MyFunction;")
                .appendDecl("var")
                .appendDecl("  MyString: String;")
                .appendDecl("  MyBool: Boolean;")
                .appendDecl("  MyInt: Integer;")
                .appendDecl("begin")
                .appendDecl("  Result := 5;")
                .appendDecl("end;")
                .appendImpl("MyProcedure;")
                .appendImpl("MyVar := MyFunction;"))
        .verifyNoIssues();
  }
}
