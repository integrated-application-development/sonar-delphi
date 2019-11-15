/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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
package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class TooLargeMethodRuleTest extends BasePmdRuleTest {

  @Test
  public void testSmallMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl(" Result := 1;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testAlmostTooLargeMethod() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("function Foo: Integer;");
    builder.appendImpl("begin");

    for (int i = 1; i <= 100; i++) {
      builder.appendImpl(" Result := Result + 1;");
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testWhitespaceMethod() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("function Foo: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("  Result := 1;");

    for (int i = 1; i <= 500; i++) {
      builder.appendImpl("");
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testEmptyMethod() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("function Foo: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("TooLargeMethodRule", builder.getOffset() + 1));
  }

  @Test
  public void testTooLargeMethod() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("function Foo: Integer;");
    builder.appendImpl("begin");

    for (int i = 1; i <= 101; i++) {
      builder.appendImpl(" Result := Result + 1;");
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("TooLargeMethodRule", builder.getOffset() + 1));
  }

  @Test
  public void testComplexTooLargeMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  if X then begin") // 1
            .appendImpl("    Bar;") // 2
            .appendImpl("  end;")
            .appendImpl("  if X then MyProcedure;") // 3 4
            .appendImpl("  if X then ") // 5
            .appendImpl("    Bar") // 6
            .appendImpl("  else")
            .appendImpl("    Baz(1, 2, 3);") // 7
            .appendImpl("  if X then begin") // 8
            .appendImpl("    Bar") // 9
            .appendImpl("  end;")
            .appendImpl("  case MyProperty of") // 10
            .appendImpl("    1: begin") // 11
            .appendImpl("       Bar;") // 12
            .appendImpl("    end;")
            .appendImpl("    2: Bar;") // 13 14
            .appendImpl("    3: Bar") // 15 16
            .appendImpl("  end;")
            .appendImpl("  repeat") // 17
            .appendImpl("    Bar;") // 18
            .appendImpl("    Baz(3, 2, 1)") // 19
            .appendImpl("  until ConditionMet;")
            .appendImpl("  asm") // 20
            .appendImpl("    push eax")
            .appendImpl("  end;")
            .appendImpl("  try") // 21
            .appendImpl("    Bar;") // 22
            .appendImpl("    Xyzzy") // 23
            .appendImpl("  except")
            .appendImpl("    on E : MyException do;") // 24
            .appendImpl("    on Exception do begin") // 25
            .appendImpl("      HandleException;") // 26
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("  try") // 27
            .appendImpl("    Xyzzy") // 28
            .appendImpl("  finally")
            .appendImpl("  end;")
            .appendImpl("  while MyCondition do") // 29
            .appendImpl("    Bar;") // 30
            .appendImpl("  if X then begin") // 31
            .appendImpl("    Bar") // 32
            .appendImpl("  end;");

    for (int i = 1; i <= 69; i++) {
      builder.appendImpl(" Result := 1;"); // 101
    }

    builder.appendImpl("end;");

    execute(builder);
    assertIssues().areExactly(1, ruleKeyAtLine("TooLargeMethodRule", builder.getOffset() + 1));
  }
}
