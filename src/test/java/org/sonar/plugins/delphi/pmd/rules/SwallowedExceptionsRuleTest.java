package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class SwallowedExceptionsRuleTest extends BasePmdRuleTest {
  @Test
  public void testExceptBlockShouldNotAddIssue() {
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

    assertIssues(empty());
  }

  @Test
  public void testHandlerShouldNotAddIssue() {
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

    assertIssues(empty());
  }

  @Test
  public void testEmptyExceptBlockShouldAddIssue() {
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("SwallowedExceptionsRule", builder.getOffSet() + 5)));
  }

  @Test
  public void testEmptyHandlerShouldAddIssue() {
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("SwallowedExceptionsRule", builder.getOffSet() + 6)));
  }

  @Test
  public void testEmptyHandlerWithoutBeginShouldAddIssue() {
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

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("SwallowedExceptionsRule", builder.getOffSet() + 6)));
  }

  @Test
  public void testEmptyHandlerInTestCodeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TTestSuite_SwallowedExceptions.Test;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testNestedEmptyHandlerInTestCodeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TTestSuite_SwallowedExceptions.Test;")
            .appendImpl("var")
            .appendImpl("  MyVar: TClass;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;;")
            .appendImpl("  except")
            .appendImpl("    on E:ESpookyError do begin")
            .appendImpl("      // Do nothing")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("  for Status := Low(SomeEnum) to High(SomeEnum) do begin")
            .appendImpl("    try")
            .appendImpl("      ThrowException;")
            .appendImpl("    except")
            .appendImpl("      on E:ESpookyError do begin")
            .appendImpl("        // Do nothing")
            .appendImpl("      end;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }
}
