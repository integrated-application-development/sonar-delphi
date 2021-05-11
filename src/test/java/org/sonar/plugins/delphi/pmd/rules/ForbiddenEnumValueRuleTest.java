package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ForbiddenEnumValueRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "Foo";
  private static final String ENUM_NAME = "Foo.TBar";
  private static final String FORBIDDEN_VALUE = "Baz";

  @BeforeEach
  void setup() {
    org.sonar.plugins.delphi.pmd.xml.DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(
            ForbiddenEnumValueRule.BLACKLISTED_ENUM_VALUES.name(), FORBIDDEN_VALUE);

    DelphiRuleProperty enumName =
        new DelphiRuleProperty(ForbiddenEnumValueRule.ENUM_NAME.name(), ENUM_NAME);

    rule.setName("ForbiddenEnumValueRuleTest");
    rule.setTemplateName("ForbiddenEnumValueRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.addProperty(enumName);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenEnumValueRule");

    addRule(rule);
  }

  @Test
  void testForbiddenEnumValueShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenEnumValueRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testQualifiedForbiddenEnumValueShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := TBar.Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenEnumValueRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testAllowedEnumValueShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBar = (Baz, Beep, Boop, Blop);")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Bar: TBar;")
            .appendImpl("begin")
            .appendImpl("  Bar := Beep;")
            .appendImpl("  Bar := Boop;")
            .appendImpl("  Bar := Blop;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenEnumValueRuleTest"));
  }
}
