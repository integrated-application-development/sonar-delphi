package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class PascalStyleResultRuleTest extends BasePmdRuleTest {
  @Test
  public void testDelphiStyleResultShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Result := nil;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("PascalStyleResultRule"));
  }

  @Test
  public void testPascalStyleResultShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("begin")
            .appendImpl("  Foo := nil;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("PascalStyleResultRule", builder.getOffset() + 3));
  }

  @Test
  public void testNestedPascalStyleResultShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("function Foo: TObject;")
            .appendImpl("  procedure Bar;")
            .appendImpl("  begin")
            .appendImpl("    Foo := nil;")
            .appendImpl("  end;")
            .appendImpl("begin")
            .appendImpl("  Bar;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("PascalStyleResultRule", builder.getOffset() + 4));
  }
}
