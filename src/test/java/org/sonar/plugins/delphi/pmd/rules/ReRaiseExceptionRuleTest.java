package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

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

    assertIssues().isEmpty();
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
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      raise;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
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

    assertIssues().areNot(ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 3));
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

    assertIssues().areNot(ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 6));
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
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      raise E;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 7));
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

    assertIssues().areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 6));
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
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      if SomeCondition then begin")
            .appendImpl("        raise E;")
            .appendImpl("      end;")
            .appendImpl("      raise E;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(2)
        .areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 8))
        .areExactly(1, ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 10));
  }

  @Test
  public void testRaiseDifferentExceptionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on E: MyException do begin")
            .appendImpl("      raise SomeOtherException.Create;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testRaiseDifferentExceptionWithoutIdentifierShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("begin")
            .appendImpl("  try")
            .appendImpl("    ThrowException;")
            .appendImpl("  except")
            .appendImpl("    on Exception do begin")
            .appendImpl("      raise Exception.Create;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKeyAtLine("ReRaiseExceptionRule", builder.getOffset() + 7));
  }
}
