package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class VariableNameRuleTest extends BasePmdRuleTest {
  @Test
  void testValidGlobalNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  GMyChar: Char;")
            .appendDecl("  GAnotherChar: Char;")
            .appendDecl("  GThirdChar: Char;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testInvalidGlobalNamesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  G_My_Char: Char;")
            .appendDecl("  gAnotherChar: Char;")
            .appendDecl("  GlobalChar: Char;");

    execute(builder);

    assertIssues()
        .hasSize(3)
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 2))
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 3))
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testValidNameInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  SomeVar: Integer;")
            .appendImpl("begin")
            .appendImpl("  if SomeVar <> 0 then begin")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBadPascalCaseInMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  someVar: Integer;")
            .appendImpl("begin")
            .appendImpl("  if someVar <> 0 then begin")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 3));
  }

  @Test
  void testAutoCreateFormVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  GMyChar: Char;")
            .appendDecl("  GAnotherChar: Char;")
            .appendDecl("  GlobalChar: Char;")
            .appendDecl("var")
            .appendDecl("  omForm: TForm;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 4))
        .areNot(ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 6));
  }

  @Test
  void testValidArgumentNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(Arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  Arg := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBadPascalCaseInArgumentNameShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(arg: Integer);")
            .appendImpl("begin")
            .appendImpl("  arg := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 1));
  }

  @Test
  void testValidInlineVariableNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var SomeVar: Integer;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testBadPascalCaseInlineVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var someVar: Integer;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 3));
  }

  @Test
  void testValidLoopVariableNameShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  for var SomeVar := 1 to 100 do;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("VariableNameRule"));
  }

  @Test
  void testBadPascalCaseInLoopVariableShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  for var someVar := 1 to 100 do;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 3));
  }
}
