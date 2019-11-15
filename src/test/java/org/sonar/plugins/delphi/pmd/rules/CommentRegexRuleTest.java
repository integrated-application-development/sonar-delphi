package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CommentRegexRuleTest extends BasePmdRuleTest {

  private DelphiRuleProperty regexProperty;

  @Before
  public void setup() {
    DelphiRule rule = new DelphiRule();
    regexProperty = new DelphiRuleProperty(CommentRegexRule.REGEX.name(), "(?i).*todo.*");

    rule.setName("TodoCommentsRule");
    rule.setTemplateName("CommentRegexRule");
    rule.setPriority(5);
    rule.addProperty(regexProperty);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.CommentRegexRule");

    addRule(rule);
  }

  @Test
  public void testValidCommentShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("// Wow, a comment!");
    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMatchingCommentShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("// TODO: Add comment");
    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("TodoCommentsRule", builder.getOffset() + 1));
  }

  @Test
  public void testInvalidRegexShouldNotAddIssue() {
    regexProperty.setValue("*");
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendImpl("// TODO: Add comment");
    execute(builder);

    assertIssues().isEmpty();
  }
}
