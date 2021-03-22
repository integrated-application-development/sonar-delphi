package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class NoSonarRuleTest extends BasePmdRuleTest {
  @Test
  void testNoSonarShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("//NOSONAR");
    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSonarRule", builder.getOffset() + 1));
  }

  @Test
  void testNoSonarWithMessageShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("//NOSONAR foobar");
    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("NoSonarRule", builder.getOffset() + 1));
  }

  @Test
  void testNoSonarWithoutWordBoundaryShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("//NOSONARfoobar");
    execute(builder);

    assertIssues().areNot(ruleKey("NoSonarRule"));
  }
}
