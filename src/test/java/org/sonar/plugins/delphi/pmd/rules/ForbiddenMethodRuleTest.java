package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class ForbiddenMethodRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "TestUnits";
  private static final String FORBIDDEN_METHOD = "TestUnits.TFoo.Bar";

  @Before
  public void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(ForbiddenMethodRule.BLACKLISTED_METHODS.name(), FORBIDDEN_METHOD);

    rule.setName("ForbiddenMethodRuleTest");
    rule.setTemplateName("ForbiddenMethodRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenMethodRule");

    addRule(rule);
  }

  @Test
  public void testForbiddenMethodUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := Local(TFoo.Create);")
            .appendImpl("  Foo.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ForbiddenMethodRuleTest", builder.getOffset() + 6));
  }

  @Test
  public void testNotUsingForbiddenMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar;")
            .appendDecl("    procedure Baz;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := Local(TFoo.Create);")
            .appendImpl("  Foo.Baz;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testMethodImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    procedure Bar; virtual;")
            .appendDecl("  end;")
            .appendImpl("procedure TFoo.Bar;")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
