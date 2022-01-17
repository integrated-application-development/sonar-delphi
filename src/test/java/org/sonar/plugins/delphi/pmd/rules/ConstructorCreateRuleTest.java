package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class ConstructorCreateRuleTest extends BasePmdRuleTest {

  @Test
  void testConstructorWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyForm = class(TForm)")
            .appendDecl("    constructor CreateClass;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ConstructorCreateRule"));
  }

  @Test
  void testConstructorIsPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyForm = class(TForm)")
            .appendDecl("    constructor Create;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ConstructorCreateRule"));
  }

  @Test
  void testConstructorWithoutPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyForm = class(TForm)")
            .appendDecl("    constructor NotCreate;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ConstructorCreateRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testBadPascalCaseAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyForm = class(TForm)")
            .appendDecl("    constructor Createclass;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("ConstructorCreateRule", builder.getOffsetDecl() + 3));
  }

  @Test
  void testClassConstructorShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TMyForm = class(TForm)")
            .appendDecl("    class constructor NotCreate;")
            .appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("ConstructorCreateRule"));
  }
}
