package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class EmptyFieldSectionRuleTest extends BasePmdRuleTest {

  @Test
  void testRegularFieldSectionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TClass = class(TObject)")
            .appendDecl("  var")
            .appendDecl("    FObject: TObject;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testEmptyFieldSectionShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TClass = class(TObject)")
            .appendDecl("  var")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("EmptyFieldSectionRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testEmptyClassVarFieldSectionShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TClass = class(TObject)")
            .appendDecl("  class var")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("EmptyFieldSectionRule", builder.getOffsetDecl() + 3));
  }
}
