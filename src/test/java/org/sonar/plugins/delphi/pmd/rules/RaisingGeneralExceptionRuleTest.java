package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class RaisingGeneralExceptionRuleTest extends BasePmdRuleTest {

  @Test
  public void testCustomExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  raise MyCustomException.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testGeneralExceptionShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  raise Exception.Create;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("RaisingGeneralExceptionRule", builder.getOffset() + 3));
  }

  @Test
  public void testRaisingInvalidExpressionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  raise (1 + 2);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
