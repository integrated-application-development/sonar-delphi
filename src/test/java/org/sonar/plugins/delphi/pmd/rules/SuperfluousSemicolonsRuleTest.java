package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.AtLine.atLine;
import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class SuperfluousSemicolonsRuleTest extends BasePmdRuleTest {

  @Test
  public void testRegularSemicolonsShouldNotAddIssue() {
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

    assertIssues().isEmpty();
  }

  @Test
  public void testStraySemicolonsShouldAddIssue() {
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
        .are(ruleKey("SuperfluousSemicolonsRule"))
        .areExactly(1, atLine(builder.getOffset() + 3))
        .areExactly(3, atLine(builder.getOffset() + 4))
        .areExactly(2, atLine(builder.getOffset() + 6))
        .areExactly(1, atLine(builder.getOffset() + 7))
        .areExactly(1, atLine(builder.getOffset() + 8));
  }
}
