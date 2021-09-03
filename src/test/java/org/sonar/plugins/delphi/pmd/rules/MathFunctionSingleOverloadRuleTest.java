package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class MathFunctionSingleOverloadRuleTest extends BasePmdRuleTest {

  @Test
  void testExtendedOverloadShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.Math;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  Power(1.0, Integer(1));")
            .appendImpl("  IntPower(1.0, 1);")
            .appendImpl("  IntPower(Extended(1.0), 1);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MathFunctionSingleOverloadRule"));
  }

  @Test
  void testDoubleOverloadShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.Math;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  IntPower(Double(1.0), 1);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MathFunctionSingleOverloadRule"));
  }

  @Test
  void testSingleOverloadShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.Math;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  IntPower(Single(1.0), 1);")
            .appendImpl("  IntPower(1, 1);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MathFunctionSingleOverloadRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("MathFunctionSingleOverloadRule", builder.getOffset() + 6));
  }
}
