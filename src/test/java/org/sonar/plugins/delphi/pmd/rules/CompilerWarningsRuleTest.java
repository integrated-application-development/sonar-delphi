package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CompilerWarningsRuleTest extends BasePmdRuleTest {
  @Test
  public void testWarningsOffShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$WARNINGS OFF}");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CompilerWarningsRule", builder.getOffset() + 1));
  }

  @Test
  public void testWarningsOnShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$WARNINGS ON}");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testWarnOffShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("{$WARN SYMBOL_DEPRECATED OFF}");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("CompilerWarningsRule", builder.getOffset() + 1));
  }

  @Test
  public void testWarnUnknownShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("{$WARN SYMBOL_DEPRECATED FOO}");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testHintsOffShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("{$HINTS OFF}");

    execute(builder);

    assertIssues().areNot(ruleKey("CompilerWarningsRule"));
  }
}
