package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedFieldsRuleTest extends BasePmdRuleTest {
  @Test
  void testUnusedPublicFieldShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("public")
            .appendDecl("  Bar: Integer;")
            .appendDecl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("UnusedFieldsRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testUsedInMethodShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("public")
            .appendDecl("  Bar: Integer;")
            .appendDecl("end;")
            .appendImpl("procedure Baz(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  Foo.Bar := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedFieldsRule"));
  }

  @Test
  void testUnusedPublishedFieldShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type TFoo = class")
            .appendDecl("  Bar: Integer;")
            .appendDecl("end;")
            .appendImpl("procedure Baz(Foo: TFoo);")
            .appendImpl("begin")
            .appendImpl("  Foo.Bar := 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("UnusedFieldsRule"));
  }
}
