package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CognitiveComplexityRuleTest extends BasePmdRuleTest {

  @Test
  public void testSimpleMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  if Foo then Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffSet() + 1))));
  }

  @Test
  public void testTooComplexMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("function Foo: Integer;").appendImpl("begin");

    for (int i = 1; i <= 16; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 16
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues(
        hasItem(hasRuleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffSet() + 1)));
  }

  @Test
  public void testTooComplexSubProcedureShouldOnlyAddIssueForSubProcedure() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("  function Bar: Integer;")
            .appendImpl("  begin");

    for (int i = 1; i <= 16; ++i) {
      builder.appendImpl("    if Foo then Bar;"); // 16
    }

    builder
        .appendImpl("  end;")
        .appendImpl("begin")
        .appendImpl("Result := Bar;")
        .appendImpl("end;");

    execute(builder);

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffSet() + 1))));
    assertIssues(
        hasItem(hasRuleKeyAtLine("MethodCognitiveComplexityRule", builder.getOffSet() + 2)));
  }
}
