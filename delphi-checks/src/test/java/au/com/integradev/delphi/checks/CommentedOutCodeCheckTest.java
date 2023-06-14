/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
                .appendDecl("{MyClass = class")
                .appendDecl("end;}")
                .appendDecl("")
                .appendDecl("{MyInterface = interface;}")
                .appendDecl("")
                .appendDecl("(*MyCustomString = type String;*)"))
        .verifyIssueOnLine(6, 9, 11);
  }

  @Test
  void testCommentedOutVariableDeclarationsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// var")
                .appendDecl("")
                .appendDecl("//MyVariable: Integer;")
                .appendDecl("")
                .appendDecl("{MyOtherVariable: String;}")
                .appendDecl("")
                .appendDecl("{MyFirstVariable, MySecondVariable: String;}")
                .appendDecl("")
                .appendDecl("(*MyFinalVariable: Boolean;*)"))
        .verifyIssueOnLine(5, 7, 9, 11, 13);
  }

  @Test
  void testCommentedOutConstantDeclarationsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("// const")
                .appendDecl("")
                .appendDecl("//C_Const1 = C_SomeOtherConstant;")
                .appendDecl("")
                .appendDecl("{C_Const2 = 123;}")
                .appendDecl("")
                .appendDecl("(*C_Const3 = 'MyString';*)"))
        .verifyIssueOnLine(5, 7, 9, 11);
  }

  @Test
  void testCommentedOutPropertiesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("type")
                .appendDecl("  TType = class(TObject)")
                .appendDecl("    //property MyProperty: String default;")
                .appendDecl("  end;"))
        .verifyIssueOnLine(7);
  }

  @Test
  void testCommentedOutMethodsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("{")
                .appendImpl("  procedure Foo;")
                .appendImpl("  begin")
                .appendImpl("    Bar.ClearCommandQueue;")
                .appendImpl("    Bar.IndexExecutors;")
                .appendImpl("    Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority);")
                .appendImpl("    FreeAndNil(Bar);")
                .appendImpl("  end;")
                .appendImpl("}")
                .appendImpl("{")
                .appendImpl("  function Add(Fmt: String; Values: array of const): String;")
                .appendImpl("  begin")
                .appendImpl("    Result := Format(Fmt, Values);")
                .appendImpl("  end;")
                .appendImpl("}"))
        .verifyIssueOnLine(8, 17);
  }

  @Test
  void testCommentedOutPrimaryExpressionsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // inherited Foo(False);")
                .appendImpl("  inherited Foo(True);")
                .appendImpl("  //Bar.ClearCommandQueue;")
                .appendImpl("  Bar.IndexExecutors;")
                .appendImpl("  //Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority);")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssueOnLine(9, 11, 13);
  }

  @Test
  void testCommentedOutPrimaryExpressionsWithCommentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // inherited Foo(False) {comment} ; //comment")
                .appendImpl("  inherited Foo(True);")
                .appendImpl("  //Bar.ClearCommandQueue; (* comment *)")
                .appendImpl("  Bar.IndexExecutors;")
                .appendImpl(
                    "  //Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority){};//;")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssueOnLine(9, 11, 13);
  }

  @Test
  void testCommentedOutAssignmentsShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  //Bar := TBar.Create;")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssueOnLine(9);
  }

  @Test
  void testCommentedOutIfStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  {")
                .appendImpl("    if Assigned(Bar) then begin")
                .appendImpl("      Bar.ClearCommandQueue;")
                .appendImpl("    end;")
                .appendImpl("  }")
                .appendImpl("")
                .appendImpl("  {")
                .appendImpl(
                    "    if Assigned(Bar) then Bar.ClearCommandQueue else Bar := TBar.Create;")
                .appendImpl("  }")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssueOnLine(10, 16);
  }

  @Test
  void testCommentedOutWithStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  {")
                .appendImpl("    with Bar do begin")
                .appendImpl("      ClearCommandQueue;")
                .appendImpl("    end;")
                .appendImpl("  }")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssueOnLine(10);
  }

  @Test
  void testCommentedOutCompilerDirectivesShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  // {$IFDEF SOME_DIRECTIVE}")
                .appendImpl("  Bar.ClearCommandQueue;")
                .appendImpl("  // {$ENDIF}")
                .appendImpl("  FreeAndNil(Bar);")
                .appendImpl("end;"))
        .verifyIssueOnLine(9, 11);
  }

  @Test
  void testCommentedOutForStatementShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new CommentedOutCodeCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  {")
                .appendImpl("    for Executor in Bar.Executors do begin")
                .appendImpl("      Executor.ExecuteCommand(cpLowPriority);")
                .appendImpl("    end;")
                .appendImpl("  }")
                .appendImpl("  Bar.ClearCommandQueue;")
                .appendImpl("  (*")
                .appendImpl("    for Index := 0 to Bar.Executors.Length do begin")
                .appendImpl("      Bar.Executors[Index].ExecuteCommand(cpMediumPriority);")
                .appendImpl("    end;")
                .appendImpl("  *)")
                .appendImpl("end;"))
        .verifyIssueOnLine(10, 16);
  }
}
