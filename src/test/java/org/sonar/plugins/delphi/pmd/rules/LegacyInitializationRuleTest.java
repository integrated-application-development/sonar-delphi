package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class LegacyInitializationRuleTest extends BasePmdRuleTest {
  @Test
  void testInitializationSectionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("initialization")
            .appendImpl("  WriteLn('This is a regular initialization section.');");

    execute(builder);

    assertIssues().areNot(ruleKey("LegacyInitializationSectionRule"));
  }

  @Test
  void testLegacyInitializationSectionShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("begin")
            .appendImpl("  WriteLn('This is a legacy initialization section.');");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("LegacyInitializationSectionRule", builder.getOffset() + 1));
  }
}
