package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class RedundantParenthesesRuleTest extends BasePmdRuleTest {

  @Test
  public void testNoParenthesesShouldNotAddIssue() {
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
  public void testParenthesesShouldNotAddIssue() {
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
  public void testRedundantParenthesesShouldAddIssue() {
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
