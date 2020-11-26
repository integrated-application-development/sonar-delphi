package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class DestructorDestroyRuleTest extends BasePmdRuleTest {
  @Test
  void testDestructorDestroyShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TObject = class(TObject)")
            .appendDecl("    destructor Destroy; override;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testDestructorNotDestroyShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TObject = class(TObject)")
            .appendDecl("    destructor NotDestroy; override;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DestructorDestroyRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testDestructorNotOverrideShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TObject = class(TObject)")
            .appendDecl("    destructor Destroy;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DestructorDestroyRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testDestructorWithArgumentsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TObject = class(TObject)")
            .appendDecl("    destructor Destroy(Arg: Boolean); override;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("DestructorDestroyRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testClassDestructorShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TObject = class(TObject)")
            .appendDecl("    class destructor NotDestroy;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
