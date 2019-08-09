package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonar.plugins.delphi.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.pmd.DelphiTestUnitBuilder;

public class ReRaiseExceptionRuleTest extends BasePmdRuleTest {

  @Test
  public void testRaiseInExceptShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    raise;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testRaiseInExceptionHandlerShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do begin")
            .appendImpl("      raise;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testRaisingNormalExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure ThrowException;")
            .appendImpl("begin")
            .appendImpl("  raise Exception.Create('Spooky scary skeletons!');")
            .appendImpl("end;");

    execute(builder);

    assertIssues(not(hasItem(hasRuleKeyAtLine("ReRaiseExceptionRule", builder.getOffSet() + 3))));
  }

  @Test
  public void testRaiseInExceptionHandlerWithNoSemicolonOrBeginEndShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do raise")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(not(hasItem(hasRuleKeyAtLine("ReRaiseExceptionRule", builder.getOffSet() + 6))));
  }

  @Test
  public void testBadRaiseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do begin")
            .appendImpl("      raise E;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("ReRaiseExceptionRule", builder.getOffSet() + 7)));
  }

  @Test
  public void testBadRaiseWithNoSemicolonOrBeginEndShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do raise E")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasItem(hasRuleKeyAtLine("ReRaiseExceptionRule", builder.getOffSet() + 6)));
  }

  @Test
  public void testMultipleBadRaisesShouldAddIssues() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: Exception do begin")
            .appendImpl("      if SomeCondition then begin")
            .appendImpl("        raise E;")
            .appendImpl("      end;")
            .appendImpl("      raise E;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(2));
    assertIssues(hasItem(hasRuleKeyAtLine("ReRaiseExceptionRule", builder.getOffSet() + 8)));
    assertIssues(hasItem(hasRuleKeyAtLine("ReRaiseExceptionRule", builder.getOffSet() + 10)));
  }
}
