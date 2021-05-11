package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.pmd.FilePosition;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class EmptyUnitRuleTest extends BasePmdRuleTest {
  @Test
  void testEmptyUnitShouldAddIssue() {
    execute(new DelphiTestUnitBuilder());
    assertIssues().areExactly(1, ruleKeyAtLine("EmptyUnitRule", FilePosition.UNDEFINED_LINE));
  }

  @Test
  void testEmptyUnitWithImportsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("    Foo")
            .appendDecl("   , Bar")
            .appendDecl("   , Baz")
            .appendDecl("   ;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("EmptyUnitRule", FilePosition.UNDEFINED_LINE));
  }

  @Test
  void testMethodDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder().appendDecl("procedure Foo;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testMethodImplementationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  // do nothing")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("EmptyUnitRule"));
  }

  @Test
  void testVariableDeclarationsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("var")
            .appendDecl("  GFoo: TObject;")
            .appendDecl("  GBar: TObject;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testConstantDeclarationsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("const")
            .appendDecl("  C_Foo = 123;")
            .appendDecl("  C_Bar = 456;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testTypeDeclarationShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TFoo = class(TObject)")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
