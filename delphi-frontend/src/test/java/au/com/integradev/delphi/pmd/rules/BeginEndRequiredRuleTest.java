/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
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

    assertIssues().areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 4));
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

    assertIssues().areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 4));
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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
  }

  @Test
  void testBareExceptShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
  }

  @Test
  void testBareExceptElseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  except")
            .appendImpl("    on Exception do begin")
            .appendImpl("      WriteLn('Foo');")
            .appendImpl("    end;")
            .appendImpl("    else WriteLn('Bar');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("BeginEndRequiredRule", builder.getOffset() + 9));
  }

  @Test
  void testExceptElseWithBeginEndShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    WriteLn('test');")
            .appendImpl("  except")
            .appendImpl("    on Exception do begin")
            .appendImpl("      WriteLn('Foo');")
            .appendImpl("    end;")
            .appendImpl("    else begin")
            .appendImpl("      WriteLn('Bar');")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
  }

  @Test
  void testBareCaseItemShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo(Int: Integer);")
            .appendImpl("begin")
            .appendImpl("  case Int of")
            .appendImpl("    1: WriteLn('test');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
  }

  @Test
  void testCaseElseShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
  }

  @Test
  void testBareCaseElseShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
  }

  @Test
  void testCaseElseBeginEndWithExtraStatementShouldNotAddIssue() {
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

    assertIssues().areNot(ruleKey("BeginEndRequiredRule"));
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
