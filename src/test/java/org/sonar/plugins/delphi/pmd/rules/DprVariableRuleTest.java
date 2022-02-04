package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestProgramBuilder;

class DprVariableRuleTest extends BasePmdRuleTest {
  @Test
  void testValidDprFile() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder().programName("ValidProgram").appendImpl("Exit;");

    execute(builder);

    assertIssues().areNot(ruleKey("ProjectFileNoVariablesRule"));
  }

  @Test
  void testVariablesInDprFileShouldAddIssue() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder()
            .appendDecl("var")
            .appendDecl("  GMyString: String;")
            .appendDecl("  GMyBool: Boolean;")
            .appendDecl("  GMyInt: Integer;")
            .appendImpl("Exit;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 2))
        .areExactly(1, ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 3))
        .areExactly(1, ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 4));
  }

  @Test
  void testVarsInsideMethodsShouldNotAddIssue() {
    DelphiTestProgramBuilder builder =
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
            .appendImpl("MyVar := MyFunction;");

    execute(builder);

    assertIssues()
        .areNot(ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 2))
        .areNot(ruleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 6));
  }
}
