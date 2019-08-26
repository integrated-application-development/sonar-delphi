package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

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

    assertIssues(empty());
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("RaisingGeneralExceptionRule", builder.getOffSet() + 3)));
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

    assertIssues(empty());
  }
}
