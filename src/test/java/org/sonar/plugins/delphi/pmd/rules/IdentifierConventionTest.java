package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class IdentifierConventionTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure MyProcedure;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeVar: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  if SomeVar <> 0 then begin");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testBadPascalCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure MyProcedure;");
    builder.appendImpl("var");
    builder.appendImpl("  someVar: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  if someVar <> 0 then begin");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("VariableNameRule", builder.getOffSet() + 3)));
  }
}
