package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class RedundantBooleanRuleTest extends BasePmdRuleTest {

  @Test
  public void testRedundantBooleanComparisonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if X = True then begin")
            .appendImpl("    DoSomething;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("RedundantBooleanRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testRedundantBooleanNegativeComparisonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if X <> False then begin")
            .appendImpl("    DoSomething;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("RedundantBooleanRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testRedundantNestedBooleanComparisonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if ((((X))) = (((True)))) then begin")
            .appendImpl("    DoSomething;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("RedundantBooleanRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testNeedlesslyInvertedBooleanShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  DoSomething(not True);")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("RedundantBooleanRule", builder.getOffSet() + 3)));
  }
}
