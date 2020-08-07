package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class TabulationCharactersRuleTest extends BasePmdRuleTest {

  @Test
  public void testRegularFileShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testFileWithTabsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendDecl("\t");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("TabulationCharactersRule"));
  }

  @Test
  public void testFileWithMultipleTabsShouldAddOnlyOneIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("\t")
            .appendDecl("var")
            .appendDecl("\t\tGBoolean:\tBoolean;")
            .appendDecl("\t");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("TabulationCharactersRule"));
  }
}
