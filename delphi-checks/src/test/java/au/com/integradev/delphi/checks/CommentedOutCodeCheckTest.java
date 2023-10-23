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

class CommentedOutCodeCheckTest {
  @Test
  void testRegularCommentsShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// huh?")
                .appendImpl("{This implementation sure is spooky.}")
                .appendImpl("(*if the precondition is met, then we do this;*)")
                .appendImpl("//for every row in our dataset, donate one dollar to charity")
                .appendImpl("//ButtonTriggerFunction() is triggered by a button,")
                .appendImpl("// Copyright;       © 1999-2019 Foo Bar")
                .appendImpl("// if do some thing then return false and exit;")
                .appendImpl("// if we couldn't find X, then we assume it's Y and")
                .appendImpl("// if current record is spooky, just exit;"))
        .verifyNoIssues();
  }

  @Test
  void testCommentedOutTypeDeclarationsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// type")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("{MyClass = class")
                .appendDecl("end;}")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("{MyInterface = interface;}")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("(*MyCustomString = type String;*)"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutVariableDeclarationsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// Noncompliant@+1")
                .appendDecl("// var")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("//MyVariable: Integer;")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("{MyOtherVariable: String;}")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("{MyFirstVariable, MySecondVariable: String;}")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("(*MyFinalVariable: Boolean;*)"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutConstantDeclarationsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// Noncompliant@+1")
                .appendDecl("// const")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("//C_Const1 = C_SomeOtherConstant;")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("{C_Const2 = 123;}")
                .appendDecl("")
                .appendDecl("// Noncompliant@+1")
                .appendDecl("(*C_Const3 = 'MyString';*)"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutPropertiesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("    // Noncompliant@+1")
                .appendDecl("    //property MyProperty: String default;")
                .appendDecl("  end;"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutMethodsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("// Noncompliant@+2")
                .appendImpl("{")
                .appendImpl("  procedure Foo;")
                .appendImpl("  begin")
                .appendImpl("    Bar.ClearCommandQueue;")
                .appendImpl("    Bar.IndexExecutors;")
                .appendImpl("    Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority);")
                .appendImpl("    FreeAndNil(Bar);")
                .appendImpl("  end;")
                .appendImpl("}")
                .appendImpl("// Noncompliant@+2")
                .appendImpl("{")
                .appendImpl("  function Add(Fmt: String; Values: array of const): String;")
                .appendImpl("  begin")
                .appendImpl("    Result := Format(Fmt, Values);")
                .appendImpl("  end;")
                .appendImpl("}"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutPrimaryExpressionsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  // inherited Foo(False);")
                .appendImpl("  inherited Foo(True);")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  //Bar.ClearCommandQueue;")
                .appendImpl("  Bar.IndexExecutors;")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  //Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority);")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutPrimaryExpressionsWithCommentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  // inherited Foo(False) {comment} ; //comment")
                .appendImpl("  inherited Foo(True);")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  //Bar.ClearCommandQueue; (* comment *)")
                .appendImpl("  Bar.IndexExecutors;")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl(
                    "  //Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority){};//;")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutAssignmentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  //Bar := TBar.Create;")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutIfStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  {")
                .appendImpl("    if Assigned(Bar) then begin")
                .appendImpl("      Bar.ClearCommandQueue;")
                .appendImpl("    end;")
                .appendImpl("  }")
                .appendImpl("// Noncompliant@+2")
                .appendImpl("  {")
                .appendImpl(
                    "    if Assigned(Bar) then Bar.ClearCommandQueue else Bar := TBar.Create;")
                .appendImpl("  }")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutWithStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  {")
                .appendImpl("    with Bar do begin")
                .appendImpl("      ClearCommandQueue;")
                .appendImpl("    end;")
                .appendImpl("  }")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutCompilerDirectivesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  // {$IFDEF SOME_DIRECTIVE}")
                .appendImpl("  Bar.ClearCommandQueue;")
                .appendImpl("  // Noncompliant@+1")
                .appendImpl("  // {$ENDIF}")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssues();
  }

  @Test
  void testCommentedOutForStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  {")
                .appendImpl("    for Executor in Bar.Executors do begin")
                .appendImpl("      Executor.ExecuteCommand(cpLowPriority);")
                .appendImpl("    end;")
                .appendImpl("  }")
                .appendImpl("  Bar.ClearCommandQueue;")
                .appendImpl("  // Noncompliant@+2")
                .appendImpl("  (*")
                .appendImpl("    for Index := 0 to Bar.Executors.Length do begin")
                .appendImpl("      Bar.Executors[Index].ExecuteCommand(cpMediumPriority);")
                .appendImpl("    end;")
                .appendImpl("  *)")
                .appendImpl("end;"))
        .verifyIssues();
  }
}
