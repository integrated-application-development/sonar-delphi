package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CyclomaticComplexityRuleTest extends BasePmdRuleTest {

  @Test
  public void testSimpleMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin")
            .appendImpl("  if Foo then Bar;") // 2
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffSet() + 1))));
  }

  @Test
  public void testAlmostTooComplexMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin");

    for (int i = 1; i <= 19; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 20
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffSet() + 1))));
  }

  @Test
  public void testTooComplexMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("begin");

    for (int i = 1; i <= 20; ++i) {
      builder.appendImpl("  if Foo then Bar;"); // 21
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues(
        hasItem(hasRuleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffSet() + 1)));
  }

  @Test
  public void testTooComplexSubProcedureShouldOnlyAddIssueForSubProcedure() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;") // 1
            .appendImpl("  function Bar: Integer;") // 1
            .appendImpl("  begin");

    for (int i = 1; i <= 20; ++i) {
      builder.appendImpl("    if Foo then Bar;"); // 21
    }

    builder
        .appendImpl("  end;")
        .appendImpl("begin")
        .appendImpl("Result := Bar;")
        .appendImpl("end;");

    execute(builder);

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffSet() + 1))));
    assertIssues(
        hasItem(hasRuleKeyAtLine("MethodCyclomaticComplexityRule", builder.getOffSet() + 2)));
  }
}
