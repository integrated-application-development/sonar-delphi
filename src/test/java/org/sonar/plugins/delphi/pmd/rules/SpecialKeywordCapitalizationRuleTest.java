package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class SpecialKeywordCapitalizationRuleTest extends BasePmdRuleTest {
  @Test
  public void testCorrectCaseShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  Break;")
            .appendImpl("  Continue;")
            .appendImpl("  Exit;")
            .appendImpl("  Goto XYZ;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testBadCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  break;")
            .appendImpl("  ContINUe;")
            .appendImpl("  exiT;")
            .appendImpl("  GoTo XYZ;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(4)
        .areExactly(1, ruleKeyAtLine("SpecialKeywordCapitalizationRule", builder.getOffset() + 3))
        .areExactly(1, ruleKeyAtLine("SpecialKeywordCapitalizationRule", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("SpecialKeywordCapitalizationRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("SpecialKeywordCapitalizationRule", builder.getOffset() + 6));
  }
}
