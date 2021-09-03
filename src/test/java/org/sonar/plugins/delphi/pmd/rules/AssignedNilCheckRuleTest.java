package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class AssignedNilCheckRuleTest extends BasePmdRuleTest {

  @Test
  void testAssignedCheckShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo: TObject);")
            .appendImpl("begin")
            .appendImpl("  if Assigned(Foo) then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testRegularComparisonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo: TObject; Bar: TObject);")
            .appendImpl("begin")
            .appendImpl("  if Foo = Bar then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if Foo <> Bar then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testNilComparisonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test(Foo: TObject);")
            .appendImpl("begin")
            .appendImpl("  if Foo = nil then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if Foo <> ((nil)) then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if (nil) = Foo then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("  if nil <> Foo then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 3))
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 6))
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 9))
        .areExactly(1, ruleKeyAtLine("AssignedNilCheckRule", builder.getOffset() + 12));
  }
}
