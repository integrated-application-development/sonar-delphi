package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class AvoidWithRuleTest extends BasePmdRuleTest {

  @Test
  public void testWithStatementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure MyProcedure;")
            .appendDecl("begin")
            .appendDecl("  with FMyField do begin")
            .appendDecl("    Value := True;")
            .appendDecl("  end;")
            .appendDecl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("AvoidWithRule", builder.getOffsetDecl() + 3)));
  }
}
