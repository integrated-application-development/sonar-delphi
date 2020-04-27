package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class PublicFieldsRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("     FPublishedField: Integer;");
    builder.appendDecl("    private");
    builder.appendDecl("     FPrivateField: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     FProtectedField: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInvalidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class(TObject)");
    builder.appendDecl("     FPublishedField: Integer;");
    builder.appendDecl("    private");
    builder.appendDecl("     FPrivateField: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     FProtectedField: String;");
    builder.appendDecl("    public");
    builder.appendDecl("     FPublicField: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("PublicFieldsRule", builder.getOffsetDecl() + 9));
  }

  @Test
  public void testRecordsAreExcluded() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyRecord = record");
    builder.appendDecl("     FPublishedField: Integer;");
    builder.appendDecl("    private");
    builder.appendDecl("     FPrivateField: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     FProtectedField: String;");
    builder.appendDecl("    public");
    builder.appendDecl("     FPublicField: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
