package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class TooManyArgumentsRuleTest extends BasePmdRuleTest {
  @Test
  void testOneVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(MyVar: Boolean);")
            .appendImpl("begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("TooManyArgumentsRule"));
  }

  @Test
  void testTooManyVariablesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(")
            .appendImpl("  MyVar1: Boolean;")
            .appendImpl("  MyVar2: Boolean;")
            .appendImpl("  MyVar3: Boolean;")
            .appendImpl("  MyVar4: Boolean;")
            .appendImpl("  MyVar5: Boolean;")
            .appendImpl("  MyVar6: Boolean;")
            .appendImpl("  MyVar7: Boolean")
            .appendImpl(");")
            .appendImpl("begin")
            .appendImpl("  MyVar1 := MyVar2;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TooManyArgumentsRule", builder.getOffset() + 1));
  }
}
