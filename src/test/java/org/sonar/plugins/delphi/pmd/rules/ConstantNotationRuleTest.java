package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ConstantNotationRuleTest extends BasePmdRuleTest {

  @BeforeEach
  void setup() {
    DelphiRuleProperty property =
        Objects.requireNonNull(
            getRule(ConstantNotationRule.class).getProperty(ConstantNotationRule.PREFIXES.name()));
    property.setValue("C_");
  }

  @Test
  void testConstantWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_MyConstant = 'Value';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testFirstCharacterIsNumberShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_85Constant = 'Value';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testTypedConstantWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_MyConstant: String = 'Value';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBadPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  CMyConstant = 'Value';");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testBadPascalCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_myConstant = 'Value';");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2));
  }
}
