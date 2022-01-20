package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class UnusedGlobalVariablesRuleTest extends BasePmdRuleTest {
  @Test
  void testUsedGlobalConstantShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  Foo: Integer;")
            .appendImpl("procedure SetFoo;")
            .appendImpl("begin")
            .appendImpl("  Foo := 123;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("UnusedGlobalVariablesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUnusedGlobalConstantShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  Foo: Integer;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("UnusedGlobalVariablesRule", builder.getOffsetDecl() + 2));
  }

  @Test
  void testUnusedAutoCreateFormVarShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  Vcl.Forms;")
            .appendDecl("type")
            .appendDecl("  TFooForm = class(TForm)")
            .appendDecl("  end;")
            .appendDecl("var")
            .appendDecl("  Foo: TFooForm;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("UnusedGlobalVariablesRule", builder.getOffsetDecl() + 2));
  }
}
