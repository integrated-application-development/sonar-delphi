package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class VariableNameRuleTest extends BasePmdRuleTest {
  @Test
  public void testValidNamesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("var");
    builder.appendDecl("  MyChar: Char;");
    builder.appendDecl("  AnotherChar: Char;");
    builder.appendDecl("  ThirdChar: Char;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testValidNameInMethodShouldNotAddIssue() {
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

    builder.appendDecl("var");
    builder.appendDecl("  MyChar: Char;");
    builder.appendDecl("  AnotherChar: Char;");
    builder.appendDecl("  thirdChar: Char;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 4)));
  }

  @Test
  public void testBadPascalCaseInMethodShouldAddIssue() {
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

  @Test
  public void testAutoCreateFormVariableShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("var");
    builder.appendDecl("  MyChar: Char;");
    builder.appendDecl("  AnotherChar: Char;");
    builder.appendDecl("  thirdChar: Char;");

    builder.appendDecl("var");
    builder.appendDecl("  omForm: TForm;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 4)));
    assertIssues(not(hasItem(hasRuleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 6))));
  }
}
