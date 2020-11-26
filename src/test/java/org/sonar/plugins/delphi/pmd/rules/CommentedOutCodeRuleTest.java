package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.AtLine.atLine;
import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class CommentedOutCodeRuleTest extends BasePmdRuleTest {

  @Test
  void testRegularCommentsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("// huh?")
            .appendImpl("{This implementation sure is spooky.}")
            .appendImpl("(*if the precondition is met, then we do this;*)")
            .appendImpl("//for every row in our dataset, donate one dollar to charity")
            .appendImpl("//ButtonTriggerFunction() is triggered by a button,")
            .appendImpl("// Copyright;       © 1999-2019 Foo Bar")
            .appendImpl("// if do some thing then return false and exit;")
            .appendImpl("// if we couldn't find X, then we assume it's Y and")
            .appendImpl("// if current record is spooky, just exit;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testCommentedOutTypeDeclarationsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("// type")
            .appendDecl("{MyClass = class")
            .appendDecl("end;}")
            .appendDecl("")
            .appendDecl("{MyInterface = interface;}")
            .appendDecl("")
            .appendDecl("(*MyCustomString = type String;*)");

    execute(builder);

    assertIssues()
        .hasSize(3)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffsetDecl() + 2))
        .areExactly(1, atLine(builder.getOffsetDecl() + 5))
        .areExactly(1, atLine(builder.getOffsetDecl() + 7));
  }

  @Test
  void testCommentedOutVariableDeclarationsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("// var")
            .appendDecl("")
            .appendDecl("//MyVariable: Integer;")
            .appendDecl("")
            .appendDecl("{MyOtherVariable: String;}")
            .appendDecl("")
            .appendDecl("{MyFirstVariable, MySecondVariable: String;}")
            .appendDecl("")
            .appendDecl("(*MyFinalVariable: Boolean;*)");

    execute(builder);

    assertIssues()
        .hasSize(5)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffsetDecl() + 1))
        .areExactly(1, atLine(builder.getOffsetDecl() + 3))
        .areExactly(1, atLine(builder.getOffsetDecl() + 5))
        .areExactly(1, atLine(builder.getOffsetDecl() + 7))
        .areExactly(1, atLine(builder.getOffsetDecl() + 9));
  }

  @Test
  void testCommentedOutConstantDeclarationsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("// const")
            .appendDecl("")
            .appendDecl("//C_Const1 = C_SomeOtherConstant;")
            .appendDecl("")
            .appendDecl("{C_Const2 = 123;}")
            .appendDecl("")
            .appendDecl("(*C_Const3 = 'MyString';*)");

    execute(builder);

    assertIssues()
        .hasSize(4)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffsetDecl() + 1))
        .areExactly(1, atLine(builder.getOffsetDecl() + 3))
        .areExactly(1, atLine(builder.getOffsetDecl() + 5))
        .areExactly(1, atLine(builder.getOffsetDecl() + 7));
  }

  @Test
  void testCommentedOutPropertiesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class(TObject)")
            .appendDecl("    //property MyProperty: String default;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CommentedOutCodeRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testCommentedOutMethodsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("}");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("CommentedOutCodeRule", builder.getOffset() + 2))
        .areExactly(1, ruleKeyAtLine("CommentedOutCodeRule", builder.getOffset() + 11));
  }

  @Test
  void testCommentedOutPrimaryExpressionsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // inherited Foo(False);")
            .appendImpl("  inherited Foo(True);")
            .appendImpl("  //Bar.ClearCommandQueue;")
            .appendImpl("  Bar.IndexExecutors;")
            .appendImpl("  //Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority);")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(3)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffset() + 3))
        .areExactly(1, atLine(builder.getOffset() + 5))
        .areExactly(1, atLine(builder.getOffset() + 7));
  }

  @Test
  void testCommentedOutPrimaryExpressionsWithCommentsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // inherited Foo(False) {comment} ; //comment")
            .appendImpl("  inherited Foo(True);")
            .appendImpl("  //Bar.ClearCommandQueue; (* comment *)")
            .appendImpl("  Bar.IndexExecutors;")
            .appendImpl("  //Bar.GetExecutorPointerArray[0]^.ExecuteCommand(cpHighPriority){};//;")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(3)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffset() + 3))
        .areExactly(1, atLine(builder.getOffset() + 5))
        .areExactly(1, atLine(builder.getOffset() + 7));
  }

  @Test
  void testCommentedOutAssignmentsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  //Bar := TBar.Create;")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CommentedOutCodeRule", builder.getOffset() + 3));
  }

  @Test
  void testCommentedOutIfStatementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("    if Assigned(Bar) then Bar.ClearCommandQueue else Bar := TBar.Create;")
            .appendImpl("  }")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffset() + 4))
        .areExactly(1, atLine(builder.getOffset() + 10));
  }

  @Test
  void testCommentedOutWithStatementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  {")
            .appendImpl("    with Bar do begin")
            .appendImpl("      ClearCommandQueue;")
            .appendImpl("    end;")
            .appendImpl("  }")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CommentedOutCodeRule", builder.getOffset() + 4));
  }

  @Test
  void testCommentedOutCompilerDirectivesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // {$IFDEF SOME_DIRECTIVE}")
            .appendImpl("  Bar.ClearCommandQueue;")
            .appendImpl("  // {$ENDIF}")
            .appendImpl("  FreeAndNil(Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffset() + 3))
        .areExactly(1, atLine(builder.getOffset() + 5));
  }

  @Test
  void testCommentedOutForStatementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
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
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .are(ruleKey("CommentedOutCodeRule"))
        .areExactly(1, atLine(builder.getOffset() + 4))
        .areExactly(1, atLine(builder.getOffset() + 10));
  }
}
