package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.HasRuleKey.hasRuleKey;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class NoSemiAfterMethodDeclarationRuleTest extends BasePmdRuleTest {

  @Test
  public void testMethodDeclarationsWithSemicolonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class")
            .appendDecl("  public")
            .appendDecl("    constructor Create; overload;")
            .appendDecl("    destructor Destroy; overload;")
            .appendDecl("    procedure MyProcedure; overload;")
            .appendDecl("    function MyFunction: String; overload;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testMethodDeclarationsWithoutSemicolonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class")
            .appendDecl("  public")
            .appendDecl("    constructor Create; overload")
            .appendDecl("    destructor Destroy; overload")
            .appendDecl("    procedure MyProcedure; overload")
            .appendDecl("    function MyFunction: String; overload")
            .appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(4));
    assertIssues(everyItem(hasRuleKey("NoSemiAfterMethodDeclarationRule")));
  }
}
