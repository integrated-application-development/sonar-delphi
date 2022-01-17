package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class AssertMessageRuleTest extends BasePmdRuleTest {
  @Test
  void testAssertWithoutErrorMessageShouldAddIssue() {
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
  void testAssertWithErrorMessageShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  Assert(False, 'This always fails.');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AssertMessageRule"));
  }

  @Test
  void testAssertMethodReferenceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Proc: reference to procedure(Expr: Boolean; Message: String);")
            .appendImpl("begin")
            .appendImpl("  Proc := System.Assert;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AssertMessageRule"));
  }
}
