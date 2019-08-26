package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

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

  @Test
  public void testExplicitInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testExplicitInheritedWithArgumentsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(X, Y);")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testExplicitInheritedWithMismatchedArgumentSizesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(X);")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3))));
  }

  @Test
  public void testExplicitInheritedWithMismatchedArgumentsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure(X, Y);")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure(True, False);")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        not(hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3))));
  }

  @Test
  public void testExplicitInheritedWithEmptyBracketsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testWrongExplicitInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  inherited SomeOtherProcedure;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFunctionInheritedShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function MyFunction: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := inherited MyFunction;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(
        hasItem(hasRuleKeyAtLine("InheritedMethodWithNoCodeRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testFunctionQualifiedInheritedShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function MyFunction: Integer;")
            .appendImpl("begin")
            .appendImpl("  Result := inherited MyFunction.GetValue;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }
}
