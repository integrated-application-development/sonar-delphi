package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class SuperfluousSemicolonsRuleTest extends BasePmdRuleTest {

  @Test
  void testRegularSemicolonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("begin")
            .appendImpl("  SomeVar := 5;")
            .appendImpl("  if SomeVar = 5 then begin")
            .appendImpl("    SomeVar := 6;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SuperfluousSemicolonsRule"));
  }

  @Test
  void testStraySemicolonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure SemicolonTest;")
            .appendImpl("begin")
            .appendImpl(";")
            .appendImpl("  ;SomeVar := 5;; ;")
            .appendImpl("  if SomeVar = 5 then begin")
            .appendImpl("    ;SomeVar := 6;;")
            .appendImpl("    ;")
            .appendImpl("  end;;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 3))
        .areExactly(3, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 4))
        .areExactly(2, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 7))
        .areExactly(1, ruleKeyAtLine("SuperfluousSemicolonsRule", builder.getOffset() + 8));
  }
}
