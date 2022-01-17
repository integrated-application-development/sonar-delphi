package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class NoFunctionReturnTypeRuleTest extends BasePmdRuleTest {
  @Test
  void testFunctionWithReturnTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function TClass.MyFunction: String;")
            .appendImpl("begin")
            .appendImpl("  Result := 'MyString';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("NoFunctionReturnTypeRule"));
  }

  @Test
  void testFunctionWithoutReturnTypeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function TClass.MyFunction;")
            .appendImpl("begin")
            .appendImpl("  Result := 'MyString';")
            .appendImpl("end;");
    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("NoFunctionReturnTypeRule", builder.getOffset() + 1));
  }
}
