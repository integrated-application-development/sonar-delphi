package org.sonar.plugins.delphi.pmd;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;

public class PublicFieldsTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("     FPublishedField: Integer;");
    builder.appendDecl("    private");
    builder.appendDecl("     FPrivateField: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     FProtectedField: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testInvalidRule() {
    DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
    builder.appendDecl("type");
    builder.appendDecl("  TMyClass = class");
    builder.appendDecl("     FPublishedField: Integer;");
    builder.appendDecl("    private");
    builder.appendDecl("     FPrivateField: Integer;");
    builder.appendDecl("    protected");
    builder.appendDecl("     FProtectedField: String;");
    builder.appendDecl("    public");
    builder.appendDecl("     FPublicField: String;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("PublicFieldsRule", builder.getOffsetDecl() + 9)));
  }
}