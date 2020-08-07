package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CompilerHintsRuleTest extends BasePmdRuleTest {
  @Test
  public void testHintsOffShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$HINTS OFF}");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CompilerHintsRule", builder.getOffset() + 1));
  }

  @Test
  public void testHintsOnShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$HINTS ON}");

    execute(builder);

    assertIssues().isEmpty();
  }
}
