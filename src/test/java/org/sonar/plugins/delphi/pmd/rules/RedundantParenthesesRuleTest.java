package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class RedundantParenthesesRuleTest extends BasePmdRuleTest {

  @Test
  void testNoParenthesesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function GetInteger: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := 123;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testParenthesesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function GetInteger: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := (123);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testRedundantParenthesesShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function GetInteger: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := ((123));")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("RedundantParenthesesRule", builder.getOffset() + 3));
  }
}
