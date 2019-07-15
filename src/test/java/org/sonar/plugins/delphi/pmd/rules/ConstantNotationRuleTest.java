package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class ConstantNotationRuleTest extends BasePmdRuleTest {

  @Test
  public void testConstantWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_MyConstant = 'Value';");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testTypedConstantWithPrefixShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_MyConstant: String = 'Value';");

    execute(builder);

    assertIssues(empty());
  }


  @Test
  public void testBadPrefixShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  CMyConstant = 'Value';");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2)));
  }

  @Test
  public void testBadPascalCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("const");
    builder.appendDecl("  C_myConstant = 'Value';");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("ConstantNotationRule", builder.getOffsetDecl() + 2)));
  }
}
