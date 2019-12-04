package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class BeginEndRequiredRuleTest extends BasePmdRuleTest {

  @Test
  public void testSimpleProcedureShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Foo;");
    builder.appendImpl("begin");
    builder.appendImpl("  SomeVar := 5;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testBareWhileLoopShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Foo;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  while SomeNumber <> 0 do");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 6));
  }

  @Test
  public void testBareForLoopShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Foo;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  for SomeNumber := 0 to 3 do");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 6));
  }

  @Test
  public void testBareRepeatShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  repeat");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  until SomeNumber <> 0;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testBareTryExceptShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure SemicolonTest;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeNumber: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  try");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  except");
    builder.appendImpl("    WriteLn('test');");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testShouldSkipAsmProcedure() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Foo; assembler; register;");
    builder.appendImpl("asm");
    builder.appendImpl("   MOV EAX, 1");
    builder.appendImpl("   ADD EAX, 2");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testElseIfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure Foo;");
    builder.appendImpl("begin");
    builder.appendImpl("  if SomeCondition then begin");
    builder.appendImpl("    DoSomething;");
    builder.appendImpl("  end");
    builder.appendImpl("  else if SomeOtherCondition then begin");
    builder.appendImpl("    DoSomethingElse;");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testBareCaseItemShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("procedure foo;");
    builder.appendImpl("var");
    builder.appendImpl("  SomeVar: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  case SomeVar of");
    builder.appendImpl("    1: WriteLn('test');");
    builder.appendImpl("  end;");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 6));
  }
}
