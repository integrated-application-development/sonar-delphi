package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class BeginEndRequiredRuleTest extends BasePmdRuleTest {

  @Test
  void testSimpleProcedureShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  WriteLn('test');")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBareWhileLoopShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  while Int <> 0 do")
            .appendImpl("    WriteLn('test');")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 4));
  }

  @Test
  void testBareForLoopShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  for Int := 0 to 3 do")
            .appendImpl("    WriteLn('test');")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 4));
  }

  @Test
  void testBareRepeatShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  repeat")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  until Int <> 0;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBareTryExceptShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  except")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testShouldSkipAsmProcedure() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo; assembler; register;")
            .appendImpl("asm")
            .appendImpl("   MOV EAX, 1")
            .appendImpl("   ADD EAX, 2")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testElseIfShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  if False then begin")
            .appendImpl("    WriteLn('foo');")
            .appendImpl("  end")
            .appendImpl("  else if True then begin")
            .appendImpl("    WriteLn('bar');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testBareCaseItemShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Int of")
            .appendImpl("    1: WriteLn('test');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 4));
  }

  @Test
  void testCaseElseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Int of")
            .appendImpl("    1: begin")
            .appendImpl("     WriteLn('test');")
            .appendImpl("    end;")
            .appendImpl("    else WriteLn('test');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 7));
  }

  @Test
  void testBareCaseElseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Int of")
            .appendImpl("    1: begin")
            .appendImpl("     WriteLn('test');")
            .appendImpl("    end;")
            .appendImpl("    else;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 7));
  }

  @Test
  void testCaseElseBeginEndWithExtraStatementShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Int of")
            .appendImpl("    1: begin")
            .appendImpl("     WriteLn('test');")
            .appendImpl("    end;")
            .appendImpl("    else")
            .appendImpl("      begin")
            .appendImpl("        WriteLn('test');")
            .appendImpl("      end;")
            .appendImpl("      WriteLn('This is still in the else-block statement list!');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 7));
  }

  @Test
  void testCaseElseBeginEndShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Int of")
            .appendImpl("    1: begin")
            .appendImpl("     WriteLn('test');")
            .appendImpl("    end;")
            .appendImpl("    else begin")
            .appendImpl("      WriteLn('test');")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
  }
}
