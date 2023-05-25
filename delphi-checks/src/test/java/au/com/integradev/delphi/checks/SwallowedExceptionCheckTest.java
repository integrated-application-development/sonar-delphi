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
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class SwallowedExceptionCheckTest extends CheckTest {
  @Test
  void testExceptBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    Log.Debug('except block');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SwallowedExceptionsRule"));
  }

  @Test
  void testHandlerShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      Log.Debug('exception handler');")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SwallowedExceptionsRule"));
  }

  @Test
  void testEmptyElseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      Log.Debug('exception handler');")
            .appendImpl("    end;")
            .appendImpl("    else begin")
            .appendImpl("      // Do nothing")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 9));
  }

  @Test
  void testBareElseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      Log.Debug('exception handler');")
            .appendImpl("    end;")
            .appendImpl("    else")
            .appendImpl("      // Do nothing")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 9));
  }

  @Test
  void testElseWithSingleStatementShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      Log.Debug('exception handler');")
            .appendImpl("    end;")
            .appendImpl("    else")
            .appendImpl("      Log.Debug('Unexpected exception!');")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SwallowedExceptionsRule"));
  }

  @Test
  void testElseWithMultipleStatementsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      Log.Debug('exception handler');")
            .appendImpl("    end;")
            .appendImpl("    else")
            .appendImpl("      Log.Debug('Unexpected exception!');")
            .appendImpl("      Cleanup;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SwallowedExceptionsRule"));
  }

  @Test
  void testEmptyExceptBlockShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    // Do nothing")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 5));
  }

  @Test
  void testEmptyHandlerShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      // Do nothing")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 6));
  }

  @Test
  void testEmptyHandlerWithoutBeginShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 6));
  }

  @Test
  void testEmptyHandlerInTestCodeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(TEST_UNIT)
            .appendDecl("type")
            .appendDecl("  TTestSuite = class(TObject)")
            .appendDecl("    procedure Test;")
            .appendDecl("  end;")
            .appendImpl("procedure TTestSuite.Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SwallowedExceptionsRule"));
  }

  @Test
  void testNestedEmptyHandlerInTestCodeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .unitName(TEST_UNIT)
            .appendDecl("type")
            .appendDecl("  TTestSuite = class(TObject)")
            .appendDecl("    procedure Test;")
            .appendDecl("  end;")
            .appendImpl("procedure TTestSuite.Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: TClass;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: ESpookyError do begin")
            .appendImpl("      // Do nothing")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("  for Status := Low(SomeEnum) to High(SomeEnum) do begin")
            .appendImpl("    try")
            .appendImpl("      ThrowException;")
            .appendImpl("    except")
            .appendImpl("      on E: ESpookyError do begin")
            .appendImpl("        // Do nothing")
            .appendImpl("      end;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("SwallowedExceptionsRule"));
  }
}
