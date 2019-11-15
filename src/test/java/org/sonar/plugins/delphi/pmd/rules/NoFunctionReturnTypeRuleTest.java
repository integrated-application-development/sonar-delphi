package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class NoFunctionReturnTypeRuleTest extends BasePmdRuleTest {
  @Test
  public void testFunctionWithReturnTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function TClass.MyFunction: String;")
            .appendImpl("begin")
            .appendImpl("  Result := 'MyString';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testFunctionWithoutReturnTypeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function TClass.MyFunction;")
            .appendImpl("begin")
            .appendImpl("  Result := 'MyString';")
            .appendImpl("end;");
    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("NoFunctionReturnTypeRule", builder.getOffset() + 1));
  }
}
