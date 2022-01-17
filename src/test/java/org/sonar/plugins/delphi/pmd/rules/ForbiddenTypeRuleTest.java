package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.xml.DelphiRule;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleProperty;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ForbiddenTypeRuleTest extends BasePmdRuleTest {
  private static final String UNIT_NAME = "TestUnit";
  private static final String FORBIDDEN_TYPES = "TestUnit.TFoo|TestUnit.TFoo.TBar";

  @BeforeEach
  void setup() {
    DelphiRule rule = new DelphiRule();
    DelphiRuleProperty blacklist =
        new DelphiRuleProperty(ForbiddenTypeRule.BLACKLISTED_TYPES.name(), FORBIDDEN_TYPES);

    rule.setName("ForbiddenTypeRuleTest");
    rule.setTemplateName("ForbiddenTypeRule");
    rule.setPriority(5);
    rule.addProperty(blacklist);
    rule.setClazz("org.sonar.plugins.delphi.pmd.rules.ForbiddenTypeRule");

    addRule(rule);
  }

  @Test
  void testForbiddenTypeUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("    class procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("begin")
            .appendImpl("  TFoo.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenTypeRuleTest", builder.getOffset() + 3))
        .areExactly(1, ruleKeyAtLine("ForbiddenTypeRuleTest", builder.getOffset() + 5));
  }

  @Test
  void testForbiddenNestedTypeUsageShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(UNIT_NAME)
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  type")
            .appendDecl("    TNested = class(TObject)")
            .appendDecl("      class procedure Bar;")
            .appendDecl("    end;")
            .appendDecl("    class procedure Bar;")
            .appendDecl("  end;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TFoo;")
            .appendImpl("  Nested: TFoo.TNested;")
            .appendImpl("begin")
            .appendImpl("  TFoo.Bar;")
            .appendImpl("  TFoo.TNested.Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ForbiddenTypeRuleTest", builder.getOffset() + 3))
        .areExactly(1, ruleKeyAtLine("ForbiddenTypeRuleTest", builder.getOffset() + 4))
        .areExactly(1, ruleKeyAtLine("ForbiddenTypeRuleTest", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("ForbiddenTypeRuleTest", builder.getOffset() + 7));
  }

  @Test
  void testMethodImplementationShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("ForbiddenTypeRuleTest"));
  }
}
