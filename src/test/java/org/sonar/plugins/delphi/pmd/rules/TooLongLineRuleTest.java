package org.sonar.plugins.delphi.pmd.rules;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.sonar.plugins.delphi.utils.matchers.IssueMatchers.hasRuleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class TooLongLineRuleTest extends BasePmdRuleTest {
  @Test
  public void testShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TClass.Test;")
            .appendImpl("begin")
            .appendImpl("  FMessage := 'This line is not too long.';")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testTooLongLineShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TClass.Test;")
            .appendImpl("begin")
            .appendImpl(
                "  FMessage := 'This line is too long. Look, it''s running right off the screen! Who would do such a thing?';")
            .appendImpl("end;");

    execute(builder);

    assertIssues(hasSize(1));
    assertIssues(hasItem(hasRuleKeyAtLine("TooLongLineRule", builder.getOffSet() + 3)));
  }

  @Test
  public void testTooLongLineInTestCodeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TTestSuite_TooLongLine.Test;")
            .appendImpl("begin")
            .appendImpl(
                "  Assert(FSomeField.SomeProperty.SomeFunction, 'This assertion message is running a little long...');")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }

  @Test
  public void testTrailingWhitespaceLineShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure TClass.Test;")
            .appendImpl("begin")
            .appendImpl(
                "  FMessage := 'This line is not too long, but there is trailing whitespace...';                             ")
            .appendImpl("end;");

    execute(builder);

    assertIssues(empty());
  }
}
