package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestProgramBuilder;

class DprFunctionRuleTest extends BasePmdRuleTest {

  @Test
  void testValidDprFile() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder().programName("ValidProgram").appendImpl("Exit;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testMethodInDprFileShouldAddIssue() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder()
            .appendDecl("procedure MyProcedure;")
            .appendDecl("begin")
            .appendDecl("  DoSomething;")
            .appendDecl("end;")
            .appendDecl("function MyFunction: Integer;")
            .appendDecl("begin")
            .appendDecl("  Result := 5;")
            .appendDecl("end;")
            .appendImpl("MyProcedure;")
            .appendImpl("MyVar := MyFunction;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("ProjectFileNoFunctionsRule", builder.getOffsetDecl() + 1))
        .areExactly(1, ruleKeyAtLine("ProjectFileNoFunctionsRule", builder.getOffsetDecl() + 5));
  }
}
