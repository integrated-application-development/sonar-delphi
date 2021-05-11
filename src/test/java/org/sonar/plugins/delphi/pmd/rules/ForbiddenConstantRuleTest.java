package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ForbiddenConstantRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String FORBIDDEN_CONSTANT = "C_Foo";

  @BeforeEach
  void setup() {
    org.sonar.plugins.delphi.pmd.xml.DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(
            ForbiddenConstantRule.BLACKLISTED_CONSTANTS.name(), FORBIDDEN_CONSTANT);

    DelphiRuleProperty unitName =
        new DelphiRuleProperty(ForbiddenConstantRule.UNIT_NAME.name(), UNIT_NAME);

    rule.setName("ForbiddenConstantRuleTest");
    rule.setTemplateName("ForbiddenConstantRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.addProperty(unitName);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenConstantRule");

    addRule(rule);
  }

  @Test
  void testForbiddenConstantUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("const")
            .appendDecl("  C_Foo = 'Foo';")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: String;")
            .appendImpl("begin")
            .appendImpl("  Foo := C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenConstantRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testQualifiedForbiddenConstantUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("const")
            .appendDecl("  C_Foo = 'Foo';")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: String;")
            .appendImpl("begin")
            .appendImpl("  Foo := " + UNIT_NAME + ".C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenConstantRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testLocalConstantUsageShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("const")
            .appendDecl("  C_Foo = 'Foo';")
            .appendImpl("procedure Test;")
            .appendImpl("const")
            .appendImpl("  C_Foo = 'Foo';")
            .appendImpl("var")
            .appendImpl("  Foo: String;")
            .appendImpl("begin")
            .appendImpl("  Foo := C_Foo;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenConstantRuleTest"));
  }
}
