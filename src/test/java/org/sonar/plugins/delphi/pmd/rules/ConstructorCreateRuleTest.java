package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class ConstructorCreateRuleTest extends BasePmdRuleTest {

  @Test
  public void testConstructorWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TForm)");
    builder.appendDecl("    constructor CreateClass;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testConstructorIsPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TForm)");
    builder.appendDecl("    constructor Create;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testConstructorWithoutPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TForm)");
    builder.appendDecl("    constructor NotCreate;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ConstructorCreateRule", builder.getOffsetDecl() + 3));
  }

  @Test
  public void testBadPascalCaseAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TForm)");
    builder.appendDecl("    constructor Createclass;");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ConstructorCreateRule", builder.getOffsetDecl() + 3));
  }
}
