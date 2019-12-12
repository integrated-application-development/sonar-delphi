package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class MultipleVariableDeclarationRuleTest extends BasePmdRuleTest {

  @Test
  public void testSingleVariableDeclarationsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  GFoo: Integer;")
            .appendDecl("  GBar: Integer;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testSingleFieldDeclarationsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class(TObject)")
            .appendDecl("    FFoo: Integer;")
            .appendDecl("    FBar: Integer;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testSingleParameterDeclarationsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo: Integer; Bar: Integer);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MultipleVariableDeclarationRule"));
  }

  @Test
  public void testMultipleVariableDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder().appendDecl("var").appendDecl("  GFoo, GBar: Integer;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(
            1, ruleKeyAtLine("MultipleVariableDeclarationRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testMultipleFieldDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class(TObject)")
            .appendDecl("    FFoo, FBar: Integer;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(
            1, ruleKeyAtLine("MultipleVariableDeclarationRule", builder.getOffsetDecl() + 3));
  }

  @Test
  public void testMultipleParameterDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo, Bar: Integer);")
            .appendImpl("begin")
            .appendImpl("  // Do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MultipleVariableDeclarationRule", builder.getOffset() + 1));
  }
}
