package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class RedundantBooleanRuleTest extends BasePmdRuleTest {

  @Test
  public void testRedundantBooleanComparisonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  X: Boolean;")
            .appendImpl("begin")
            .appendImpl("  if X = True then begin")
            .appendImpl("    DoSomething;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("RedundantBooleanRule", builder.getOffset() + 5));
  }

  @Test
  public void testBooleanComparisonImplicitConversionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  X: Variant;")
            .appendImpl("begin")
            .appendImpl("  if X = True then begin")
            .appendImpl("    DoSomething;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testRedundantBooleanNegativeComparisonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  X: Boolean;")
            .appendImpl("begin")
            .appendImpl("  if X <> False then begin")
            .appendImpl("    DoSomething;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("RedundantBooleanRule", builder.getOffset() + 5));
  }

  @Test
  public void testRedundantNestedBooleanComparisonShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  X: Boolean;")
            .appendImpl("begin")
            .appendImpl("  if ((((X))) = (((True)))) then begin")
            .appendImpl("    DoSomething;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("RedundantBooleanRule", builder.getOffset() + 5));
  }

  @Test
  public void testNeedlesslyInvertedBooleanShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure Foo(Bar: Boolean);")
            .appendImpl("procedure Baz;")
            .appendImpl("begin")
            .appendImpl("  Foo(not True);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("RedundantBooleanRule", builder.getOffset() + 3));
  }
}
