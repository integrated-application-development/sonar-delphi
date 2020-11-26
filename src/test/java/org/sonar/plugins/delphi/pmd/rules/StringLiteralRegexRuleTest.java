package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class StringLiteralRegexRuleTest extends BasePmdRuleTest {
  private static final String IDREF_PATTERN = ".*ID(\\d|[A-Z]){8}.*";
  private DelphiRuleProperty regexProperty;

  @BeforeEach
  void setup() {
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
  void testValidStringShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_MyConstant = 'Wow, a constant!';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testMatchingStringShouldAddIssue() {
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
  void testInvalidRegexShouldNotAddIssue() {
    regexProperty.setValue("*");

    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_HardcodedIDRef = 'ID12345678';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testMatchingStringInTestMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(TEST_UNIT)
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
  void testMatchingStringInTestClassDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(TEST_UNIT)
            .appendImpl("type")
            .appendImpl("  TTestSuite = class(TObject)")
            .appendImpl("  private const")
            .appendImpl("    C_HardcodedIDRef = 'ID1234X6U8';")
            .appendImpl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
