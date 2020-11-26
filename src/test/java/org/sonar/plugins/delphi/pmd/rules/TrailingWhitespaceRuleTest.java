package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class TrailingWhitespaceRuleTest extends BasePmdRuleTest {
  @Test
  void testTrailingSpaceShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("var Foo: TObject; ");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TrailingWhitespaceRule", builder.getOffset() + 1));
  }

  @Test
  void testTrailingTabShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;\t");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TrailingWhitespaceRule", builder.getOffset() + 1));
  }

  @Test
  void testTrailingMixedWhitespaceShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;\t   \t\t \t  ");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TrailingWhitespaceRule", builder.getOffset() + 1));
  }

  @Test
  void testNoTrailingWhitespaceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("var Foo: TObject;");

    execute(builder);

    assertIssues().areNot(ruleKey("TrailingWhitespaceRule"));
  }

  @Test
  void testLeadingWhitespaceShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendImpl("\t   \t \t var Foo: TObject;");

    execute(builder);

    assertIssues().areNot(ruleKey("TrailingWhitespaceRule"));
  }
}
