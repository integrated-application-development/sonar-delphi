package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class EmptyBracketsRuleTest extends BasePmdRuleTest {

  @Test
  public void testMethodParametersEmptyBracketsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TObject)");
    builder.appendDecl("    procedure MyProcedure();");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyBracketsRule", builder.getOffsetDecl() + 3)));
  }

  @Test
  public void testMethodCallEmptyBracketsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("EmptyBracketsRule", builder.getOffSet() + 3)));
  }
}
