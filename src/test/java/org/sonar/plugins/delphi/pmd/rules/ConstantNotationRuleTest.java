package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
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

    assertIssues().areNot(ruleKey("ConstantNotationRule"));
  }

  @Test
  void testFirstCharacterIsNumberShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_85Constant = 'Value';");

    execute(builder);

    assertIssues().areNot(ruleKey("ConstantNotationRule"));
  }

  @Test
  void testBadPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  CMyConstant = 'Value';");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testBadPascalCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_myConstant = 'Value';");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testInlineConstantWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  const C_MyConstant = 'Value';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ConstantNotationRule"));
  }

  @Test
  void testInlineFirstCharacterIsNumberShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  const C_85Constant = 'Value';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ConstantNotationRule"));
  }

  @Test
  void testInlineBadPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  const CConstant = 'Value';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffset() + 3));
  }

  @Test
  void testInlineBadPascalCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  const C_myConstant = 'Value';")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffset() + 3));
  }
}
