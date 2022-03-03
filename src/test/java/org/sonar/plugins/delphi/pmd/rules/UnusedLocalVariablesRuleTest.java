package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedLocalVariablesRuleTest extends BasePmdRuleTest {
  @Test
  void testUsedInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure UseFoo(Foo: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  UseFoo(Foo)")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }

  @Test
  void testUnusedAssignedInMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  Foo := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedLocalVariablesRule", builder.getOffset() + 3));
  }

  @Test
  void testUsedAssignedInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure UseFoo(Foo: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  Foo := 0;")
            .appendImpl("  UseFoo(Foo);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }

  @Test
  void testUnusedInMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedLocalVariablesRule", builder.getOffset() + 3));
  }

  @Test
  void testUsedInlineVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure UseFoo(Foo: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("  UseFoo(Foo);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }

  @Test
  void testUnusedInlineVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedLocalVariablesRule", builder.getOffset() + 3));
  }

  @Test
  void testUnusedAssignedInlineVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("  Foo := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedLocalVariablesRule", builder.getOffset() + 3));
  }

  @Test
  void testUsedAssignedInlineVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure UseFoo(Foo: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer;")
            .appendImpl("  Foo := 0;")
            .appendImpl("  UseFoo(Foo);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }

  @Test
  void testVariableWithFieldAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    Bar: Boolean;")
            .appendDecl("  end;")
            .appendDecl("procedure UseFoo(Foo: Integer);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := TFoo.Create;")
            .appendImpl("  Foo.Bar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }

  @Test
  void testProcVariablePointerShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("    procedure Baz;")
            .appendDecl("  end;")
            .appendDecl("  TObjProc = procedure of object;")
            .appendDecl("procedure UsePtr(Ptr: Pointer);")
            .appendImpl("procedure TFoo.Bar;")
            .appendImpl("var")
            .appendImpl("  Proc: TObjProc;")
            .appendImpl("begin")
            .appendImpl("  Proc := Baz;")
            .appendImpl("  UsePtr(@Proc)")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }

  @Test
  void testUnusedGlobalVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  G_Foo: Integer;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }

  @Test
  void testUnusedArgumentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo: Integer);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedLocalVariablesRule"));
  }
}
