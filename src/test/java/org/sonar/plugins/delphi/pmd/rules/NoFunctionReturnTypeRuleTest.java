package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class NoFunctionReturnTypeRuleTest extends BasePmdRuleTest {
  @Test
  public void testFunctionWithReturnTypeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder()
        .appendImpl("function TClass.MyFunction: String;")
        .appendImpl("begin")
        .appendImpl("  Result := 'MyString';")
        .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testFunctionWithoutReturnTypeShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder()
        .appendImpl("function TClass.MyFunction;")
        .appendImpl("begin")
        .appendImpl("  Result := 'MyString';")
        .appendImpl("end;");
    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("NoFunctionReturnTypeRule", builder.getOffSet() + 1)));
  }
}
