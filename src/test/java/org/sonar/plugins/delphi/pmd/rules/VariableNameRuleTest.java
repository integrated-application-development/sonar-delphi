package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

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

    assertIssues().isEmpty();
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

    assertIssues().isEmpty();
  }

  @Test
  public void testBadPascalCaseShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();

    builder.appendDecl("var");
    builder.appendDecl("  MyChar: Char;");
    builder.appendDecl("  AnotherChar: Char;");
    builder.appendDecl("  thirdChar: Char;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 4));
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

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffset() + 3));
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

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 4))
        .areNot(ruleKeyAtLine("VariableNameRule", builder.getOffsetDecl() + 6));
  }
}
