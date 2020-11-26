package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class MethodNestingDepthRuleTest extends BasePmdRuleTest {
  @Test
  void testShallowNestedMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Outer;")
            .appendImpl("  procedure Inner;")
            .appendImpl("  begin")
            .appendImpl("    // Nesting level: 1")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  // Nesting level: 0")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MethodNestingDepthRule"));
  }

  @Test
  void testDeeplyNestedMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Outer;")
            .appendImpl("  procedure Inner;")
            .appendImpl("    procedure Innerest;")
            .appendImpl("    begin")
            .appendImpl("      // Nesting level: 2")
            .appendImpl("    end;")
            .appendImpl("  begin")
            .appendImpl("    // Nesting level: 1")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  // Nesting level: 0")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("MethodNestingDepthRule", builder.getOffset() + 3));
  }
}
