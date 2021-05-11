package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class CompilerHintsRuleTest extends BasePmdRuleTest {
  @Test
  void testHintsOffShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$HINTS OFF}");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CompilerHintsRule", builder.getOffset() + 1));
  }

  @Test
  void testHintsOnShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$HINTS ON}");

    execute(builder);

    assertIssues().areNot(ruleKey("CompilerHintsRule"));
  }
}
