package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class SwallowedExceptionsRuleTest extends BasePmdRuleTest {
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

    assertIssues().isEmpty();
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

    assertIssues().isEmpty();
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

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 9));
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

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 9));
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

    assertIssues().isEmpty();
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

    assertIssues().isEmpty();
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

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 5));
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

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("SwallowedExceptionsRule", builder.getOffset() + 6));
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

    assertIssues().isEmpty();
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

    assertIssues().isEmpty();
  }
}
