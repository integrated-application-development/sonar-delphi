package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class TooManyVariablesRuleTest extends BasePmdRuleTest {

  @Test
  void testOneVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  MyVar: Boolean;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TooManyVariablesRule"));
  }

  @Test
  void testSingleVariableDeclarationsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  MyVar1: Boolean;")
            .appendImpl("  MyVar2: Boolean;")
            .appendImpl("  MyVar3: Boolean;")
            .appendImpl("  MyVar4: Boolean;")
            .appendImpl("  MyVar5: Boolean;")
            .appendImpl("  MyVar6: Boolean;")
            .appendImpl("  MyVar7: Boolean;")
            .appendImpl("  MyVar8: Boolean;")
            .appendImpl("  MyVar9: Boolean;")
            .appendImpl("  MyVar10: Boolean;")
            .appendImpl("  MyVar11: Boolean;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TooManyVariablesRule", builder.getOffset() + 1));
  }

  @Test
  void testMultiVariableDeclarationsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  MyVar1,")
            .appendImpl("  MyVar2,")
            .appendImpl("  MyVar3,")
            .appendImpl("  MyVar4,")
            .appendImpl("  MyVar5,")
            .appendImpl("  MyVar6,")
            .appendImpl("  MyVar7,")
            .appendImpl("  MyVar8,")
            .appendImpl("  MyVar9,")
            .appendImpl("  MyVar10,")
            .appendImpl("  MyVar11: Boolean;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TooManyVariablesRule", builder.getOffset() + 1));
  }
}
