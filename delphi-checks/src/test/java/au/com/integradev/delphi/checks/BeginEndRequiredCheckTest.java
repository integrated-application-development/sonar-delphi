/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class BeginEndRequiredCheckTest {
  @Test
  void testSimpleProcedureShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  WriteLn('test');")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBareWhileLoopShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  while Int <> 0 do")
                .appendImpl("    WriteLn('test');")
                .appendImpl("end;"))
        .verifyIssueOnLine(10);
  }

  @Test
  void testBareForLoopShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  for Int := 0 to 3 do")
                .appendImpl("    WriteLn('test');")
                .appendImpl("end;"))
        .verifyIssueOnLine(10);
  }

  @Test
  void testBareRepeatShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  repeat")
                .appendImpl("    WriteLn('test');")
                .appendImpl("  until Int <> 0;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBareExceptShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  try")
                .appendImpl("    WriteLn('test');")
                .appendImpl("  except")
                .appendImpl("    WriteLn('test');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBareExceptElseShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
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
                .appendImpl("end;"))
        .verifyIssueOnLine(15);
  }

  @Test
  void testExceptElseWithBeginEndShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
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
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testShouldSkipAsmProcedure() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo; assembler; register;")
                .appendImpl("asm")
                .appendImpl("   MOV EAX, 1")
                .appendImpl("   ADD EAX, 2")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testElseIfShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo;")
                .appendImpl("begin")
                .appendImpl("  if False then begin")
                .appendImpl("    WriteLn('foo');")
                .appendImpl("  end")
                .appendImpl("  else if True then begin")
                .appendImpl("    WriteLn('bar');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBareCaseItemShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  case Int of")
                .appendImpl("    1: WriteLn('test');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCaseElseShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  case Int of")
                .appendImpl("    1: begin")
                .appendImpl("     WriteLn('test');")
                .appendImpl("    end;")
                .appendImpl("    else WriteLn('test');")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testBareCaseElseShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("procedure Foo(Int: Integer);")
                .appendImpl("begin")
                .appendImpl("  case Int of")
                .appendImpl("    1: begin")
                .appendImpl("     WriteLn('test');")
                .appendImpl("    end;")
                .appendImpl("    else;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCaseElseBeginEndWithExtraStatementShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
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
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testCaseElseBeginEndShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new BeginEndRequiredCheck())
        .onFile(
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
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
