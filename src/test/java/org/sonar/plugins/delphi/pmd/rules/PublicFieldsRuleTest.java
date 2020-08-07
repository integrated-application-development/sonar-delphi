package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class PublicFieldsRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("     FPublishedField: Integer;")
            .appendDecl("    private")
            .appendDecl("     FPrivateField: Integer;")
            .appendDecl("    protected")
            .appendDecl("     FProtectedField: String;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInvalidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyClass = class(TObject)")
            .appendDecl("     FPublishedField: Integer;")
            .appendDecl("    private")
            .appendDecl("     FPrivateField: Integer;")
            .appendDecl("    protected")
            .appendDecl("     FProtectedField: String;")
            .appendDecl("    public")
            .appendDecl("     FPublicField: String;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("PublicFieldsRule", builder.getOffsetDecl() + 9));
  }

  @Test
  public void testRecordsAreExcluded() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyRecord = record")
            .appendDecl("     FPublishedField: Integer;")
            .appendDecl("    private")
            .appendDecl("     FPrivateField: Integer;")
            .appendDecl("    protected")
            .appendDecl("     FProtectedField: String;")
            .appendDecl("    public")
            .appendDecl("     FPublicField: String;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
