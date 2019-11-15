package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class LowerCaseReservedWordsRuleTest extends BasePmdRuleTest {

  @Test
  public void testUppercaseKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("Begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("END;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffset() + 2))
        .areExactly(1, ruleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffset() + 4));
  }

  @Test
  public void testAsmBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Bar;")
            .appendImpl("asm")
            .appendImpl("  SHR   eax, 16")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
