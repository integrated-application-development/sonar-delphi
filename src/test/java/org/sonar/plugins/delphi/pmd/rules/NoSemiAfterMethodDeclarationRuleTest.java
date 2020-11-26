package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class NoSemiAfterMethodDeclarationRuleTest extends BasePmdRuleTest {

  @Test
  void testMethodDeclarationsWithSemicolonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    constructor Create; override;")
            .appendDecl("    destructor Destroy; override;")
            .appendDecl("    procedure MyProcedure; overload;")
            .appendDecl("    function MyFunction: String; overload;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testMethodDeclarationsWithoutSemicolonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class(TObject)")
            .appendDecl("  public")
            .appendDecl("    constructor Create; override")
            .appendDecl("    destructor Destroy; override")
            .appendDecl("    procedure MyProcedure; overload")
            .appendDecl("    function MyFunction: String; overload")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().hasSize(4).are(ruleKey("NoSemiAfterMethodDeclarationRule"));
  }
}
