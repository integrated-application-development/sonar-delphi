package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class LowerCaseReservedWordsRuleTest extends BasePmdRuleTest {

  @Test
  void testUppercaseKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("Begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("END;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffset() + 2))
        .areExactly(1, ruleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffset() + 4));
  }

  @Test
  void testAsmBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Bar;")
            .appendImpl("asm")
            .appendImpl("  SHR   eax, 16")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("LowerCaseReservedWordsRule"));
  }
}
