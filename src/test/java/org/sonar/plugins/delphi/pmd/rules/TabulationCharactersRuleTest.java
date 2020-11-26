package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class TabulationCharactersRuleTest extends BasePmdRuleTest {

  @Test
  void testRegularFileShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFileWithTabsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendDecl("\t");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("TabulationCharactersRule"));
  }

  @Test
  void testFileWithMultipleTabsShouldAddOnlyOneIssue() {
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
