package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestProgramBuilder;

public class DprVariableRuleTest extends BasePmdRuleTest {
  @Test
  public void testValidDprFile() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder().programName("ValidProgram").appendImpl("Exit;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testVariablesInDprFileShouldAddIssue() {
    DelphiTestProgramBuilder builder =
        new DelphiTestProgramBuilder()
            .appendDecl("var")
            .appendDecl("  MyString: String;")
            .appendDecl("  MyBool: Boolean;")
            .appendDecl("  MyInt: Integer;")
            .appendImpl("Exit;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(
        hasItem(hasRuleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 1)));
  }

  @Test
  public void testVarsInsideMethodsShouldNotAddIssue() {
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

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 2))));

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("ProjectFileNoVariablesRule", builder.getOffsetDecl() + 6))));
  }
}
