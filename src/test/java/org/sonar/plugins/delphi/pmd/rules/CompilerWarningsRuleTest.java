package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class CompilerWarningsRuleTest extends BasePmdRuleTest {
  @Test
  void testWarningsOffShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$WARNINGS OFF}");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CompilerWarningsRule", builder.getOffset() + 1));
  }

  @Test
  void testWarningsOnShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$WARNINGS ON}");

    execute(builder);

    assertIssues().areNot(ruleKey("CompilerWarningsRule"));
  }

  @Test
  void testWarnOffShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("{$WARN SYMBOL_DEPRECATED OFF}");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CompilerWarningsRule", builder.getOffset() + 1));
  }

  @Test
  void testWarnUnknownShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("{$WARN SYMBOL_DEPRECATED FOO}");

    execute(builder);

    assertIssues().areNot(ruleKey("CompilerWarningsRule"));
  }

  @Test
  void testHintsOffShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$HINTS OFF}");

    execute(builder);

    assertIssues().areNot(ruleKey("CompilerWarningsRule"));
  }
}
