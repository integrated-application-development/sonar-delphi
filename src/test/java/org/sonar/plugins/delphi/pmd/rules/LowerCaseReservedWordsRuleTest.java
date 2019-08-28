package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class LowerCaseReservedWordsRuleTest extends BasePmdRuleTest {

  @Test
  public void testUppercaseKeywordShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Foo;")
            .appendImpl("Begin")
            .appendImpl("  MyVar := True;")
            .appendImpl("END;");

    execute(builder);

    assertIssues(hasSize(2));
    assertIssues(hasItem(hasRuleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffSet() + 2)));
    assertIssues(hasItem(hasRuleKeyAtLine("LowerCaseReservedWordsRule", builder.getOffSet() + 4)));
  }

  @Test
  public void testAsmBlockShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Bar;")
            .appendImpl("asm")
            .appendImpl("  SHR   eax, 16")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }
}
