package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.pmd.rules.ForbiddenIdentifierRule.BLACKLISTED_NAMES;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class ForbiddenIdentifierRuleTest extends BasePmdRuleTest {
  @Before
  public void setup() {
    org.sonar.plugins.delphi.pmd.xml.DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist = new DelphiRuleProperty(BLACKLISTED_NAMES.name(), "BadName");

    rule.setName("ForbiddenNameRuleTest");
    rule.setTemplateName("ForbiddenIdentifierRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenIdentifierRule");

    addRule(rule);
  }

  @Test
  public void testAllowedIdentifierShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  GoodName: TObject;");
    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testForbiddenIdentifierShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  BadName: TObject;");
    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ForbiddenNameRuleTest", builder.getOffsetDecl() + 2));
  }
}
