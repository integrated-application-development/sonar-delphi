package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.AtLine.atLine;
import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class AssignedNilCheckRuleTest extends BasePmdRuleTest {

  @Test
  public void testAssignedCheckShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(Foo) then begin")
            .appendImpl("    Exit;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testRegularComparisonsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TObject;")
            .appendImpl("  Bar: TObject;")
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
  public void testNilComparisonsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TObject;")
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
        .hasSize(4)
        .are(ruleKey("AssignedNilCheckRule"))
        .areExactly(1, atLine(builder.getOffset() + 5))
        .areExactly(1, atLine(builder.getOffset() + 8))
        .areExactly(1, atLine(builder.getOffset() + 11))
        .areExactly(1, atLine(builder.getOffset() + 14));
  }
}
