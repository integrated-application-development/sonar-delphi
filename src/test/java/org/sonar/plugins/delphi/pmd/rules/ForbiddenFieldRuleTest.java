package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ForbiddenFieldRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String DECLARING_TYPE = "TestUnit.TFoo";
  private static final String FORBIDDEN_FIELD = "Bar";

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(ForbiddenFieldRule.BLACKLISTED_FIELDS.name(), FORBIDDEN_FIELD);

    DelphiRuleProperty declaringType =
        new DelphiRuleProperty(ForbiddenFieldRule.DECLARING_TYPE.name(), DECLARING_TYPE);

    rule.setName("ForbiddenFieldRuleTest");
    rule.setTemplateName("ForbiddenFieldRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.addProperty(declaringType);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenFieldRule");

    addRule(rule);
  }

  @Test
  void testForbiddenFieldUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = record")
            .appendDecl("    Bar: String;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Foo: TFoo);")
            .appendImpl("var")
            .appendImpl("  Baz: String;")
            .appendImpl("begin")
            .appendImpl("  Baz := Foo.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("ForbiddenFieldRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testForbiddenFieldNameDeclaredByDifferentTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TBoop = record")
            .appendDecl("    Bar: String;")
            .appendDecl("  end;")
            .appendImpl("procedure Test(Boop: TBoop);")
            .appendImpl("var")
            .appendImpl("  Baz: String;")
            .appendImpl("begin")
            .appendImpl("  Baz := Boop.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ForbiddenFieldRuleTest"));
  }
}
