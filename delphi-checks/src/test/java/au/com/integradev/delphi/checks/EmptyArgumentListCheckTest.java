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

class EmptyArgumentListCheckTest {

  @Test
  void testRoutineParametersEmptyBracketsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyArgumentListCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TMyForm = class(TObject)")
                .appendDecl("    // Fix@[+1:25 to +1:27] <<>>")
                .appendDecl("    procedure MyProcedure(); // Noncompliant")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testInvocationOfUnknownRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyArgumentListCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  // Fix@[+1:13 to +1:15] <<>>")
                .appendImpl("  MyProcedure(); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testInvocationOfKnownRoutineShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyArgumentListCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("procedure MyProcedure;")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  MyProcedure(); // Noncompliant")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testExplicitArrayConstructorShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyArgumentListCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TIntArray = array of Integer;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  Foo: TIntArray;")
                .appendImpl("begin")
                .appendDecl("  // Fix@[+1:25 to +1:27] <<>>")
                .appendImpl("  Foo := TIntArray.Create();")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInvocationOfProcVarShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyArgumentListCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TProc = procedure;")
                .appendImpl("procedure Test(ProcVar: TProc);")
                .appendImpl("begin")
                .appendImpl("  ProcVar();")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInvocationOfProcVarArrayShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyArgumentListCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TProc = procedure;")
                .appendDecl("  TProcArray = array of TProc;")
                .appendImpl("procedure Test(ProcArray: TProcArray);")
                .appendImpl("begin")
                .appendImpl("  ProcArray[0]();")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAssignedArgumentShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new EmptyArgumentListCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("function Foo: TObject;")
                .appendImpl("begin")
                .appendImpl("  Result := TObject.Create;")
                .appendImpl("end;")
                .appendImpl("")
                .appendImpl("procedure Test;")
                .appendImpl("begin")
                .appendImpl("  Result := Assigned(Foo());")
                .appendImpl("  Result := System.Assigned(Foo());")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
