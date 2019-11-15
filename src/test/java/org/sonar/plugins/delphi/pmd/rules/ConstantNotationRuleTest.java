package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class ConstantNotationRuleTest extends BasePmdRuleTest {

  @Test
  public void testConstantWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_MyConstant = 'Value';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testFirstCharacterIsNumberShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_85Constant = 'Value';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testTypedConstantWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_MyConstant: String = 'Value';");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testBadPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  CMyConstant = 'Value';");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2));
  }

  @Test
  public void testBadPascalCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_myConstant = 'Value';");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2));
  }
}
