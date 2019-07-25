package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class InheritedMethodWithNoCodeRuleTest extends BasePmdRuleTest {

  @Test
  public void testShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(
        hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testNoSemicolonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited;")
            .appendImpl("  FMyField := 5;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testImplementationWithInheritedAtEndShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  FMyField := 5;")
            .appendImpl("  inherited;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFalsePositiveImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  FMyField := 5;")
            .appendImpl("  if MyBoolean then begin")
            .appendImpl("    inherited;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }
}
