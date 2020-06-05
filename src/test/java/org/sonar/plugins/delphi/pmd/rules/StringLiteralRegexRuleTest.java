package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

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

    assertIssues().isEmpty();
  }

  @Test
  public void testMatchingStringShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_HardcodedIDRef = 'ID1234X6U8';");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("IDRefStringLiteralRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testInvalidRegexShouldNotAddIssue() {
    regexProperty.setValue("*");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_HardcodedIDRef = 'ID12345678';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMatchingStringInTestMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("Tests")
            .appendDecl("type")
            .appendDecl("  TTestSuite = class(TObject)")
            .appendDecl("    procedure TestWithHardcodedIDRef;")
            .appendDecl("  end;")
            .appendImpl("procedure TTestSuite.TestWithHardcodedIDRef;")
            .appendImpl("const")
            .appendImpl("  C_HardcodedIDRef = 'ID1234X6U8';")
            .appendImpl("begin")
            .appendImpl("  Assert(Assigned(C_HardcodedIDRef), 'The sky is falling!');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMatchingStringInTestClassDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName("Tests")
            .appendImpl("type")
            .appendImpl("  TTestSuite = class(TObject)")
            .appendImpl("  private const")
            .appendImpl("    C_HardcodedIDRef = 'ID1234X6U8';")
            .appendImpl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
