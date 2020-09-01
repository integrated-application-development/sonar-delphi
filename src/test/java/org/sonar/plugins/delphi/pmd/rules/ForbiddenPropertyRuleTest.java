package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class ForbiddenPropertyRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "TestUnits";
  private static final String FORBIDDEN_PROPERTY = "TestUnits.TFoo.Bar";

  @Before
  public void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(
            ForbiddenPropertyRule.BLACKLISTED_PROPERTIES.name(), FORBIDDEN_PROPERTY);

    rule.setName("ForbiddenPropertyRuleTest");
    rule.setTemplateName("ForbiddenPropertyRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenPropertyRule");

    addRule(rule);
  }

  @Test
  public void testForbiddenPropertyUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    FBar: TFoo;")
            .appendDecl("    property Bar: TFoo read FBar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := Local(TFoo.Create).Obj as TFoo;")
            .appendImpl("  Foo := Foo.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ForbiddenPropertyRuleTest", builder.getOffset() + 6));
  }

  @Test
  public void testNotUsingForbiddenPropertyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    FBar: TFoo;")
            .appendDecl("    property Bar: TFoo read FBar;")
            .appendDecl("    property Baz: TFoo read FBar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := Local(TFoo.Create).Obj as TFoo;")
            .appendImpl("  Foo := Foo.Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}