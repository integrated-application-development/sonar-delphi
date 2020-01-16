package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;

public class StringLiteralRegexRuleTest extends BasePmdRuleTest {
  private static final String IDREF_PATTERN = ".*ID(\\d|[A-Z]){8}.*";
  private DelphiRuleProperty regexProperty;

  @Before
  public void setup() {
    DelphiRule rule = new DelphiRule();
    regexProperty = new DelphiRuleProperty(StringLiteralRegexRule.REGEX.name(), IDREF_PATTERN);

    rule.setName("IDRefStringLiteralRule");
    rule.setTemplateName("StringLiteralRegexRule");
    rule.setPriority(5);
    rule.addProperty(regexProperty);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.StringLiteralRegexRule");

    addRule(rule);
  }

  @Test
  public void testValidStringShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_MyConstant = 'Wow, a constant!';");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testMatchingStringShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_HardcodedIDRef = 'ID1234X6U8';");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("IDRefStringLiteralRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testInvalidRegexShouldNotAddIssue() {
    regexProperty.setValue("*");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_HardcodedIDRef = 'ID12345678';");

    execute(builder);

    assertIssues(empty());
  }
}
