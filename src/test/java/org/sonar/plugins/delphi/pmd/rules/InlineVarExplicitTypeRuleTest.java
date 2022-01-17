package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class InlineVarExplicitTypeRuleTest extends BasePmdRuleTest {
  @Test
  void testInlineVarWithTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo: Integer := 123;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("InlineVarExplicitTypeRule"));
  }

  @Test
  void testInlineVarWithoutTypeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  var Foo := 123;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("InlineVarExplicitTypeRule", builder.getOffset() + 3));
  }
}
