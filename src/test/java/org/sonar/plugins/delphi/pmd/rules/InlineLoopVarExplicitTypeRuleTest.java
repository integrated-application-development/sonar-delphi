package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class InlineLoopVarExplicitTypeRuleTest extends BasePmdRuleTest {
  @Test
  void testInlineVarWithTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  for var I: Integer := 1 to 100 do Continue;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineLoopVarExplicitTypeRule"));
  }

  @Test
  void testInlineVarWithoutTypeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  for var I := 1 to 100 do Continue;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InlineLoopVarExplicitTypeRule", builder.getOffset() + 3));
  }
}
