package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class RedundantAssignmentRuleTest extends BasePmdRuleTest {
  @Test
  void testRedundantAssignmentShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Bar: String;")
            .appendImpl("begin")
            .appendImpl("  Bar := Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("RedundantAssignmentRule", builder.getOffset() + 5));
  }

  @Test
  void testRedundantAssignmentWithNestingShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Bar: String;")
            .appendImpl("begin")
            .appendImpl("  Bar := (Bar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("RedundantAssignmentRule", builder.getOffset() + 5));
  }

  @Test
  void testRegularAssignmentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("var")
            .appendImpl("  Bar: String;")
            .appendImpl("begin")
            .appendImpl("  Bar := 'Baz';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
