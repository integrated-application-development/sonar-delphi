package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class CaseStatementSizeRuleTest extends BasePmdRuleTest {

  @Test
  public void testThreeCaseItemsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  case MyNumber of")
            .appendImpl("    1: Break;")
            .appendImpl("    2: Break;")
            .appendImpl("    3: Break;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CaseStatementSizeRule"));
  }

  @Test
  public void testTwoCaseItemsWithElseShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  case MyNumber of")
            .appendImpl("    1: Break;")
            .appendImpl("    2: Break;")
            .appendImpl("    else begin")
            .appendImpl("      Break;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("CaseStatementSizeRule"));
  }

  @Test
  public void testTwoCaseItemsShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  case MyNumber of")
            .appendImpl("    1: Break;")
            .appendImpl("    2: Break;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CaseStatementSizeRule", builder.getOffset() + 3));
  }

  @Test
  public void testOneCaseItemWithElseShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  case MyNumber of")
            .appendImpl("    1: Break;")
            .appendImpl("    else begin")
            .appendImpl("      Break;")
            .appendImpl("    end;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("CaseStatementSizeRule", builder.getOffset() + 3));
  }
}
