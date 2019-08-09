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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class TooLargeMethodRuleTest extends BasePmdRuleTest {

  @Test
  public void testValidRule() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl(" Result := 1;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testAlmostTooLongMethod() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("function Foo: Integer;");
    builder.appendImpl("begin");

    for (int i = 1; i <= 100; i++) {
      builder.appendImpl(" Result := Result + 1;");
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues(empty());
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

    assertIssues(empty());
  }

  @Test
  public void testEmptyMethod() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("function Foo: Integer;");
    builder.appendImpl("begin");
    builder.appendImpl("end;");

    execute(builder);

    assertIssues(not(hasItem(hasRuleKeyAtLine("TooLargeMethodRule", builder.getOffSet() + 1))));
  }

  @Test
  public void testTooLongMethod() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendImpl("function Foo: Integer;");
    builder.appendImpl("begin");

    for (int i = 1; i <= 101; i++) {
      builder.appendImpl(" Result := Result + 1;");
    }

    builder.appendImpl("end;");

    execute(builder);

    assertIssues(hasItem(hasRuleKeyAtLine("TooLargeMethodRule", builder.getOffSet() + 1)));
  }

  @Test
  public void testComplexTooLongMethod() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: Integer;")
            .appendImpl("begin")
            .appendImpl("  if X then begin") // 1 (then)
            .appendImpl("    Bar;") // 2 (semicolon)
            .appendImpl("  end;")
            .appendImpl("  if X then MyProcedure;") // 3 (then)
            // 4 (semicolon)
            .appendImpl("  if X then ") // 5 (then)
            .appendImpl("    Bar")
            .appendImpl("  else") // 6 (else)
            .appendImpl("    Baz(1, 2, 3);") // 7 (semicolon)
            .appendImpl("  if X then begin") // 8 (then)
            .appendImpl("    Bar") // 9 (No semicolon before an end)
            .appendImpl("  end;")
            .appendImpl("  case MyProperty of") // 10 (case)
            .appendImpl("    1: begin") // 11 (case-item)
            .appendImpl("       Bar;") // 12 (semicolon)
            .appendImpl("    end;")
            .appendImpl("    2: Bar;") // 13 (case-item)
            // 14 (semicolon)
            .appendImpl("    3: Bar") // 15 (case-item)
            // 16 (No semicolon before an end)
            .appendImpl("  end;")
            .appendImpl("  repeat")
            .appendImpl("    Bar;") // 17 (semicolon)
            .appendImpl("    Baz(3, 2, 1)") // 18 (No semicolon before an until)
            .appendImpl("  until ConditionMet;") // 19 (semicolon)
            .appendImpl("  asm")
            .appendImpl("    push eax")
            .appendImpl("  end;") // 20 (semicolon after asm statement)
            .appendImpl("  try") // 21 (try)
            .appendImpl("    Bar;") // 22 (semicolon)
            .appendImpl("    Xyzzy") // 23 (No semicolon before an except)
            .appendImpl("  except") // 24 (except)
            .appendImpl("  end;")
            .appendImpl("  try") // 25 (try)
            .appendImpl("    Xyzzy") // 26 (No semicolon before a finally)
            .appendImpl("  finally") // 27 (finally)
            .appendImpl("  end;")
            .appendImpl("  while MyCondition do") // 28 (do)
            .appendImpl("    Bar;") // 29 (semicolon)
            .appendImpl("  if X then begin") // 30 (then)
            .appendImpl("    Bar") // 31 (No semicolon before an end)
            .appendImpl("  end;");

    for (int i = 1; i <= 70; i++) {
      builder.appendImpl(" Result := 1;"); // 101 (semicolon)
    }

    builder.appendImpl("end;");

    execute(builder);
    assertIssues(hasItem(hasRuleKeyAtLine("TooLargeMethodRule", builder.getOffSet() + 1)));
  }
}
