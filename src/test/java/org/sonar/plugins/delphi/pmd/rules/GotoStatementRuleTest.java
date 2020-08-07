package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class GotoStatementRuleTest extends BasePmdRuleTest {
  @Test
  public void testGotoStatementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("  label Here;")
            .appendImpl("begin")
            .appendImpl("  goto Here;")
            .appendImpl("  Here:")
            .appendImpl("    Exit;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("GotoStatementRule", builder.getOffset() + 6));
  }
}
