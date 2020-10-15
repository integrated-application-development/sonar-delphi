package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class NoSemiAfterFieldDeclarationRuleTest extends BasePmdRuleTest {

  @Test
  public void testFieldDeclarationsWithSemicolonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class(TObject)")
            .appendDecl("    Foo: TObject;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testFieldDeclarationsWithoutSemicolonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = class(TObject)")
            .appendDecl("    Foo: TObject")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(
            1, ruleKeyAtLine("NoSemiAfterFieldDeclarationRule", builder.getOffsetDecl() + 3));
  }

  @Test
  public void testFieldDeclarationsWithoutSemicolonsInRecordVariantSectionsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TType = record")
            .appendDecl("    case Tag: Integer of")
            .appendDecl("      0: (ByteField: Byte);")
            .appendDecl("      1: (ShortIntField: ShortInt);")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
