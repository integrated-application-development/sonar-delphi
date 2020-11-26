package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class EmptyVisibilitySectionRuleTest extends BasePmdRuleTest {

  @Test
  void testRegularVisibilitySectionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TClass = class(TObject)")
            .appendDecl("  private")
            .appendDecl("    FObject: TObject;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testImplicitVisibilitySectionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TClass = class(TObject)")
            .appendDecl("    Foo: TObject;")
            .appendDecl("  end;")
            .appendDecl("  TEmptyClass = class(TObject)")
            .appendDecl("    // Implicit empty visibility section")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyVisibilitySectionRule"));
  }

  @Test
  void testEmptyVisibilitySectionShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TClass = class(TObject)")
            .appendDecl("    public")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("EmptyVisibilitySectionRule", builder.getOffsetDecl() + 3));
  }
}
