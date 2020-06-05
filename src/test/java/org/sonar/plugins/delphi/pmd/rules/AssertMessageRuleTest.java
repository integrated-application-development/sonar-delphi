package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class AssertMessageRuleTest extends BasePmdRuleTest {
  @Test
  public void testAssertWithoutErrorMessageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  Assert(False);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssertMessageRule", builder.getOffset() + 3));
  }

  @Test
  public void testAssertWithErrorMessageShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  Assert(False, 'This always fails.');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testAssertMethodReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Proc: reference to procedure(Expr: Boolean; Message: String);")
            .appendImpl("begin")
            .appendImpl("  Proc := System.Assert;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
